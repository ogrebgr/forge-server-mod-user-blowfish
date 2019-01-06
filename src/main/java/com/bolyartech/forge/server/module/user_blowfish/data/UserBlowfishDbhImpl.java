package com.bolyartech.forge.server.module.user_blowfish.data;

import com.bolyartech.forge.server.module.user.data.UserLoginType;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.user.User;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class UserBlowfishDbhImpl implements UserBlowfishDbh {


    @Override
    public UserBlowfish createNew(Connection dbc, UserDbh userDbh, BlowfishDbh blowfishPrimaryDbh,
                                  BlowfishDbh blowfishSecondaryDbh,
                                  String username, String passwordClearForm) throws SQLException {
        try {
            String sqlLock = "LOCK TABLES users WRITE, user_blowfish WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (!(blowfishPrimaryDbh.usernameExists(dbc, username) &&
                    blowfishSecondaryDbh.usernameExists(dbc, username))) {

                try {
                    dbc.setAutoCommit(false);
                    User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
                    Blowfish blowfish = blowfishPrimaryDbh.createNew(dbc, user.getId(), username, passwordClearForm);
                    dbc.commit();
                    return new UserBlowfish(user, blowfish);
                } catch (SQLException e) {
                    dbc.rollback();
                    throw e;
                } finally {
                    dbc.setAutoCommit(true);
                }
            } else {
                return null;
            }
        } finally {
            String sqlLock = "UNLOCK TABLES";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);
        }
    }


    @Override
    public NewNamedResult createNewNamed(Connection dbc, UserDbh userDbh, BlowfishDbh blowfishPrimaryDbh,
                                         BlowfishDbh blowfishSecondaryDbh,
                                         ScreenNameDbh screenNameDbh, String username, String passwordClearForm,
                                         String screenName) throws SQLException {


        try {
            String sqlLock = "LOCK TABLES users WRITE, user_blowfish WRITE, user_blowfish_post_auto WRITE, " +
                    "user_screen_names WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (blowfishPrimaryDbh.usernameExists(dbc, username) || blowfishSecondaryDbh.usernameExists(dbc, username)) {
                return NewNamedResult.createFailUsernameTaken();
            }


            if (screenNameDbh.exists(dbc, screenName)) {
                return NewNamedResult.createFailScreenNameTaken();
            }


            try {
                dbc.setAutoCommit(false);

                User user = userDbh.createNew(dbc, false, UserLoginType.BLOWFISH);
                blowfishPrimaryDbh.createNew(dbc, user.getId(), username, passwordClearForm);

                screenNameDbh.createNew(dbc, user.getId(), screenName);
                dbc.commit();

                return NewNamedResult.createOk(user);
            } catch (Exception e) {
                dbc.rollback();
                throw e;
            } finally {
                dbc.setAutoCommit(true);
            }
        } finally {
            String sqlLock = "UNLOCK TABLES";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);
        }
    }
}
