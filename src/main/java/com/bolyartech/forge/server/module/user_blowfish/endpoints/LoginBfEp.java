package com.bolyartech.forge.server.module.user_blowfish.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.handler.ForgeDbSecureEndpoint;
import com.bolyartech.forge.server.misc.Params;
import com.bolyartech.forge.server.module.user.LoginType;
import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.SessionInfo;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.BCrypt;
import com.bolyartech.forge.server.module.user_blowfish.data.Blowfish;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.MissingParametersResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.sql.Connection;
import java.sql.SQLException;


public class LoginBfEp extends ForgeDbSecureEndpoint {
    static final String PARAM_USERNAME = "username";
    static final String PARAM_PASSWORD = "password";


    private final UserDbh mUserDbh;
    private final BlowfishDbh mBlowfishDbh;
    private final ScreenNameDbh mScreenNameDbh;

    private final Gson mGson;


    public LoginBfEp(DbPool dbPool, UserDbh userDbh, BlowfishDbh blowfishDbh, ScreenNameDbh screenNameDbh) {
        super(dbPool);
        mUserDbh = userDbh;
        mBlowfishDbh = blowfishDbh;
        mScreenNameDbh = screenNameDbh;
        mGson = new Gson();
    }


    @Override
    public ForgeResponse handleForgeSecure(RequestContext ctx, Connection dbc) throws ResponseException, SQLException {
        String username = ctx.getFromPost(PARAM_USERNAME);
        String password = ctx.getFromPost(PARAM_PASSWORD);

        if (Params.areAllPresent(username, password)) {
            Blowfish bfUser = mBlowfishDbh.loadByUsername(dbc, username);
            if (bfUser != null) {
                if ((BCrypt.checkpw(password, bfUser.getPasswordHash()))) {
                    User user = mUserDbh.loadById(dbc, bfUser.getUser());

                    Session session = ctx.getSession();
                    session.setVar(SessionVars.VAR_USER, user);
                    session.setVar(SessionVars.VAR_LOGIN_TYPE, LoginType.NATIVE);

                    SessionInfo si = createSessionInfo(dbc, bfUser.getUser());

                    return new OkResponse(mGson.toJson(new RokLogin(session.getMaxInactiveInterval(), si)));
                } else {
                    return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
                }
            } else {
                return new ForgeResponse(UserResponseCodes.Errors.INVALID_LOGIN, "Invalid login");
            }
        } else {
            return MissingParametersResponse.getInstance();
        }
    }


    private SessionInfo createSessionInfo(Connection dbc, long userId) throws SQLException {
        ScreenName sn = mScreenNameDbh.loadByUser(dbc, userId);

        SessionInfo si;
        if (sn != null) {
            si = new SessionInfo(userId, sn.getScreenName());
        } else {
            si = new SessionInfo(userId, null);
        }

        return si;
    }


    public static class RokLogin {
        @SerializedName("session_ttl")
        public final int sessionTtl;
        @SerializedName("session_info")
        public final SessionInfo sessionInfo;


        public RokLogin(int sessionTtl, SessionInfo sessionInfo) {
            this.sessionTtl = sessionTtl;
            this.sessionInfo = sessionInfo;
        }
    }
}
