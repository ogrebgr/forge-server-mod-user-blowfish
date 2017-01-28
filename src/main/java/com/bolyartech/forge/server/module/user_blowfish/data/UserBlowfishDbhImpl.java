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
    public UserBlowfish createNew(Connection dbc, UserDbh userDbh, BlowfishDbh blowfishDbh,
                                  String username, String passwordClearForm) throws SQLException {
        try {
            String sqlLock = "LOCK TABLES users WRITE, user_blowfish WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (!blowfishDbh.usernameExists(dbc, username)) {
                try {
                    dbc.setAutoCommit(false);
                    User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
                    Blowfish blowfish = blowfishDbh.createNew(dbc, user.getId(), username, passwordClearForm);
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
    public NewNamedResult createNewNamed(Connection dbc, UserDbh userDbh, BlowfishDbh blowfishDbh,
                                         ScreenNameDbh screenNameDbh, String username, String passwordClearForm,
                                         String screenName) throws SQLException {


        try {
            String sqlLock = "LOCK TABLES users WRITE, user_blowfish WRITE, user_screen_names WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (blowfishDbh.usernameExists(dbc, username)) {
                return new NewNamedResult(false, null, true);
            }


            if (screenNameDbh.exists(dbc, screenName)) {
                return new NewNamedResult(false, null, false);
            }


            try {
                dbc.setAutoCommit(false);

                User user = userDbh.createNew(dbc, false, UserLoginType.SCRAM);
                Blowfish blowfish = blowfishDbh.createNew(dbc, user.getId(), username, passwordClearForm);

                screenNameDbh.createNew(dbc, user.getId(), screenName);
                dbc.commit();

                return new NewNamedResult(true, new UserBlowfish(user, blowfish), false);
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


    @Override
    public boolean replaceExisting(Connection dbc, BlowfishDbh blowfishDbh, ScreenNameDbh screenNameDbh,
                                   long userId, String newUsername, String passwordClearForm, String screenName)
            throws SQLException {

        try {
            String sqlLock = "LOCK TABLES user_blowfish WRITE, user_screen_names WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (screenNameDbh.exists(dbc, screenName)) {
                return false;
            }

            try {
                dbc.setAutoCommit(false);

                replaceExistingNamed(dbc, blowfishDbh, userId, newUsername, passwordClearForm);
                screenNameDbh.createNew(dbc, userId, screenName);
                dbc.commit();

                return true;
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


    @Override
    public void replaceExistingNamed(Connection dbc, BlowfishDbh blowfishDbh, long userId, String newUsername,
                                     String passwordClearForm) throws SQLException {

        blowfishDbh.replace(dbc, userId,
                newUsername,
                passwordClearForm);
    }
}
