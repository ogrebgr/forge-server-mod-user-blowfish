package com.bolyartech.forge.server.module.user_blowfish.data;

import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;

import java.sql.Connection;
import java.sql.SQLException;


public interface UserBlowfishDbh {
    UserBlowfish createNew(Connection dbc,
                           UserDbh userDbh,
                           BlowfishDbh BlowfishDbh,
                           BlowfishDbh blowfishPostAutoDbh,
                           String username,
                           String passwordClearForm) throws SQLException;


    NewNamedResult createNewNamed(Connection dbc,
                                  UserDbh userDbh,
                                  BlowfishDbh BlowfishDbh,
                                  BlowfishDbh blowfishPostAutoDbh,
                                  ScreenNameDbh screenNameDbh,
                                  String username,
                                  String passwordClearForm,
                                  String screenName) throws SQLException;


    class NewNamedResult {
        private final boolean isOk;
        private final boolean isUsernameTaken;
        private final boolean isScreenNameTaken;
        private final User user;


        private NewNamedResult(boolean isOk, boolean isUsernameTaken, boolean isScreenNameTaken, User user) {
            this.isOk = isOk;
            this.isUsernameTaken = isUsernameTaken;
            this.isScreenNameTaken = isScreenNameTaken;
            this.user = user;
        }


        public static NewNamedResult createOk(User user) {
            return new NewNamedResult(true, false, false, user);
        }


        public static NewNamedResult createFailUsernameTaken() {
            return new NewNamedResult(false, true, false, null);
        }


        public static NewNamedResult createFailScreenNameTaken() {
            return new NewNamedResult(false, false, true, null);
        }


        public boolean isOk() {
            return isOk;
        }


        public boolean isUsernameTaken() {
            return isUsernameTaken;
        }


        public boolean isScreenNameTaken() {
            return isScreenNameTaken;
        }


        public User getUser() {
            return user;
        }
    }
}
