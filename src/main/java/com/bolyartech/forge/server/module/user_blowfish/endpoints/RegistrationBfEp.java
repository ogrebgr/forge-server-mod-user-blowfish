package com.bolyartech.forge.server.module.user_blowfish.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.handler.ForgeDbSecureEndpoint;
import com.bolyartech.forge.server.misc.Params;
import com.bolyartech.forge.server.module.user.LoginType;
import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.RokLogin;
import com.bolyartech.forge.server.module.user.data.SessionInfo;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfishDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.MissingParametersResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.SQLException;


public class RegistrationBfEp extends ForgeDbSecureEndpoint {
    static final String PARAM_USERNAME = "username";
    static final String PARAM_PASSWORD = "password";
    static final String PARAM_SCREEN_NAME = "screen_name";

    private final Gson mGson;

    private final UserDbh mUserDbh;
    private final BlowfishDbh mBlowfishDbh;
    private final UserBlowfishDbh mUserBlowfishDbh;
    private final ScreenNameDbh mScreenNameDbh;


    public RegistrationBfEp(DbPool dbPool, UserDbh userDbh, BlowfishDbh blowfishDbh, UserBlowfishDbh userBlowfishDbh,
                            ScreenNameDbh screenNameDbh) {

        super(dbPool);
        mUserDbh = userDbh;
        mBlowfishDbh = blowfishDbh;
        mUserBlowfishDbh = userBlowfishDbh;
        mScreenNameDbh = screenNameDbh;

        mGson = new Gson();
    }


    @Override
    public ForgeResponse handleForgeSecure(RequestContext ctx, Connection dbc) throws ResponseException,
            SQLException {

        String username = ctx.getFromPost(PARAM_USERNAME);
        String password = ctx.getFromPost(PARAM_PASSWORD);
        String screenName = ctx.getFromPost(PARAM_SCREEN_NAME);


        if (!Params.areAllPresent(username, password, screenName)) {
            return MissingParametersResponse.getInstance();
        }

        if (!User.isValidUsername(username)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_USERNAME, "Invalid username");
        }

        if (!User.isValidPasswordLength(password)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_PASSWORD, "Password too short");
        }

        if (!ScreenName.isValid(screenName)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_SCREEN_NAME, "Invalid screen name");
        }


        UserBlowfishDbh.NewNamedResult rez = mUserBlowfishDbh.createNewNamed(dbc, mUserDbh, mBlowfishDbh,
                mScreenNameDbh, username, password, screenName);

        if (rez.isOk) {
            SessionInfo si = new SessionInfo(rez.mUserBlowfish.getUser().getId(), null);

            Session session = ctx.getSession();
            session.setVar(SessionVars.VAR_USER, rez.mUserBlowfish.getUser());
            session.setVar(SessionVars.VAR_LOGIN_TYPE, LoginType.NATIVE);
            return new OkResponse(
                    mGson.toJson(new RokLogin(
                            session.getMaxInactiveInterval(),
                            si
                    )));
        } else if (rez.usernameExist) {
            return new ForgeResponse(UserResponseCodes.Errors.USERNAME_EXISTS, "Invalid Login");
        } else {
            return new ForgeResponse(UserResponseCodes.Errors.SCREEN_NAME_EXISTS, "screen name taken");
        }
    }
}
