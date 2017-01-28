package com.bolyartech.forge.server.module.user_blowfish.data;

import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;

import java.sql.Connection;
import java.sql.SQLException;


public interface UserBlowfishDbh {
    UserBlowfish createNew(Connection dbc,
                           UserDbh userDbh,
                           BlowfishDbh BlowfishDbh,
                           String username,
                           String passwordClearForm) throws SQLException;


    NewNamedResult createNewNamed(Connection dbc,
                                                                                                             UserDbh userDbh,
                                                                                                             BlowfishDbh BlowfishDbh,
                                                                                                             ScreenNameDbh screenNameDbh,
                                                                                                             String username,
                                                                                                             String passwordClearForm,
                                                                                                             String screenName) throws SQLException;

    boolean replaceExisting(Connection dbc,
                            BlowfishDbh BlowfishDbh,
                            ScreenNameDbh screenNameDbh,
                            long userId,
                            String newUsername,
                            String passwordClearForm,
                            String screenName) throws SQLException;

    void replaceExistingNamed(Connection dbc,
                              BlowfishDbh BlowfishDbh,
                              long userId,
                              String newUsername,
                              String passwordClearForm) throws SQLException;


    class NewNamedResult {
        public final boolean isOk;
        public final UserBlowfish mUserBlowfish;
        public final boolean usernameExist;


        public NewNamedResult(boolean isOk, UserBlowfish userBlowfish, boolean usernameExist) {
            this.isOk = isOk;
            mUserBlowfish = userBlowfish;
            this.usernameExist = usernameExist;
        }
    }
}
