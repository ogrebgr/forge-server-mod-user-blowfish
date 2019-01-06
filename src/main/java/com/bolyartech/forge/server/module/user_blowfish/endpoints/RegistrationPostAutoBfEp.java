package com.bolyartech.forge.server.module.user_blowfish.endpoints;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.misc.Params;
import com.bolyartech.forge.server.module.user.ForgeUserDbEndpoint;
import com.bolyartech.forge.server.module.user.UserResponseCodes;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenName;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfish;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfishDbh;
import com.bolyartech.forge.server.response.ResponseException;
import com.bolyartech.forge.server.response.forge.ForgeResponse;
import com.bolyartech.forge.server.response.forge.MissingParametersResponse;
import com.bolyartech.forge.server.response.forge.OkResponse;
import com.bolyartech.forge.server.route.RequestContext;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.SQLException;


public class RegistrationPostAutoBfEp extends ForgeUserDbEndpoint {
    static final String PARAM_NEW_USERNAME = "new_username";
    static final String PARAM_NEW_PASSWORD = "new_password";
    static final String PARAM_SCREEN_NAME = "screen_name";

    private final Gson gson;

    private final UserDbh userDbh;
    private final UserBlowfishDbh userBlowfishPostAutoDbh;
    private final BlowfishDbh blowfishDbh;
    private final BlowfishDbh blowfishPostAutoDbh;

    private final ScreenNameDbh screenNameDbh;


    public RegistrationPostAutoBfEp(DbPool dbPool,
                                    UserDbh userDbh,
                                    UserBlowfishDbh userBlowfishPostAutoDbh,
                                    BlowfishDbh blowfishDbh, BlowfishDbh blowfishPostAutoDbh,
                                    ScreenNameDbh screenNameDbh) {

        super(dbPool);
        this.blowfishDbh = blowfishDbh;
        this.blowfishPostAutoDbh = blowfishPostAutoDbh;
        gson = new Gson();
        this.userDbh = userDbh;
        this.userBlowfishPostAutoDbh = userBlowfishPostAutoDbh;
        this.screenNameDbh = screenNameDbh;
    }


    @Override
    public ForgeResponse handle(RequestContext ctx,
                                Connection dbc,
                                User user) throws ResponseException, SQLException {

        String newUsername = ctx.getFromPost(PARAM_NEW_USERNAME);
        String newPassword = ctx.getFromPost(PARAM_NEW_PASSWORD);
        String screenName = ctx.getFromPost(PARAM_SCREEN_NAME);

        if (!Params.areAllPresent(newUsername, newPassword)) {
            return MissingParametersResponse.getInstance();
        }

        ScreenName existingScreenName = screenNameDbh.loadByUser(dbc, user.getId());
        if (existingScreenName == null) {
            if (Strings.isNullOrEmpty(screenName)) {
                return new MissingParametersResponse("missing screen name");
            } else if (!ScreenName.isValid(screenName)) {
                return new ForgeResponse(UserResponseCodes.Errors.INVALID_SCREEN_NAME, "Invalid screen name");
            }
        }


        if (!User.isValidUsername(newUsername)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_USERNAME, "Invalid username");
        }

        if (!User.isValidPasswordLength(newPassword)) {
            return new ForgeResponse(UserResponseCodes.Errors.INVALID_PASSWORD, "Password too short");
        }


        if (existingScreenName == null) {

            UserBlowfishDbh.NewNamedResult tmp = userBlowfishPostAutoDbh.createNewNamedPostAuto(dbc,
                    blowfishPostAutoDbh,
                    blowfishDbh,
                    screenNameDbh,
                    user.getId(),
                    newUsername,
                    newPassword,
                    screenName);
            if (tmp.isOk()) {
                return new OkResponse();
            } else if (tmp.isUsernameTaken()) {
                return new ForgeResponse(UserResponseCodes.Errors.USERNAME_EXISTS, "Invalid Login");
            } else {
                return new ForgeResponse(UserResponseCodes.Errors.SCREEN_NAME_EXISTS, "Screen name taken");
            }

        } else {
            UserBlowfish tmp = userBlowfishPostAutoDbh.createNew(dbc, userDbh,
                    blowfishPostAutoDbh,  blowfishDbh, newUsername,
                    newPassword);
            if (tmp != null) {
                return new OkResponse();
            } else {
                return new ForgeResponse(UserResponseCodes.Errors.USERNAME_EXISTS, "Invalid Login");
            }
        }
    }
}
