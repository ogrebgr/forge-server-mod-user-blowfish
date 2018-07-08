package com.bolyartech.forge.server.module.user_blowfish.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.handler.ForgeDbEndpoint;
import com.bolyartech.forge.server.module.user.LoginType;
import com.bolyartech.forge.server.module.user.SessionVars;
import com.bolyartech.forge.server.module.user.data.RokResponseAutoregistration;
import com.bolyartech.forge.server.module.user.data.SessionInfo;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfish;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfishDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.bolyartech.forge.server.session.Session;
import com.google.gson.Gson;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;


public class AutoregistrationBfEp extends ForgeDbEndpoint {
    private final Gson gson;

    private final UserDbh userDbh;
    private final BlowfishDbh blowfishDbh;
    private final UserBlowfishDbh userBlowfishDbh;


    public AutoregistrationBfEp(DbPool dbPool, UserDbh userDbh, BlowfishDbh scramDbh, UserBlowfishDbh userBlowfishDbh) {
        super(dbPool);
        gson = new Gson();
        this.userDbh = userDbh;
        blowfishDbh = scramDbh;
        this.userBlowfishDbh = userBlowfishDbh;
    }


    @Override
    public ForgeResponse handleForge(RequestContext ctx, Connection dbc) throws ResponseException,
            SQLException {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24];
        random.nextBytes(salt);

        String username;
        String password = UUID.randomUUID().toString();
        UserBlowfish us;

        while (true) {
            // adding "g" as a prefix in order to make the username valid when UUID starts with number
            username = "g" + UUID.randomUUID().toString().replace("-", "");

            us = userBlowfishDbh.createNew(dbc, userDbh, blowfishDbh, username, password);
            if (us != null) {
                break;
            }
        }


        SessionInfo si = new SessionInfo(us.getUser().getId(), null);

        Session session = ctx.getSession();
        session.setVar(SessionVars.VAR_USER, us.getUser());
        session.setVar(SessionVars.VAR_LOGIN_TYPE, LoginType.NATIVE);

        return new OkResponse(
                gson.toJson(new RokResponseAutoregistration(username,
                        password,
                        session.getMaxInactiveInterval(),
                        si
                )));
    }
}
