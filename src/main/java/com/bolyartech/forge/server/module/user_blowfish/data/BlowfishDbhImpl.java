package com.bolyartech.forge.server.module.user_blowfish.data;

import com.google.common.base.Strings;

import java.sql.*;


public class BlowfishDbhImpl implements BlowfishDbh {
    private static final String USERS_TABLE_NAME = "user_blowfish";

    private final String mTableName;


    public BlowfishDbhImpl() {
        mTableName = getTableName();
    }


    @Override
    public Blowfish loadByUser(Connection dbc, long user) throws SQLException {
        if (user <= 0) {
            throw new IllegalStateException("user <= 0");
        }

        String sql = "SELECT username, password " +
                "FROM " + mTableName +
                " WHERE user = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setLong(1, user);

            try (ResultSet rs = psLoad.executeQuery()) {
                if (rs.next()) {
                    return new Blowfish(user, rs.getString(1), rs.getString(2));
                } else {
                    return null;
                }
            }
        }
    }


    @Override
    public Blowfish loadByUsername(Connection dbc, String username) throws SQLException {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalStateException("username empty");
        }

        String sql = "SELECT user, password " +
                "FROM " + mTableName +
                " WHERE username = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setString(1, username);

            try (ResultSet rs = psLoad.executeQuery()) {
                if (rs.next()) {
                    return new Blowfish(rs.getLong(1), username, rs.getString(2));
                } else {
                    return null;
                }
            }
        }
    }


    @Override
    public boolean usernameExists(Connection dbc, String username) throws SQLException {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalStateException("username empty");
        }

        String sql = "SELECT user " +
                "FROM " + mTableName +
                " WHERE username_lc = ?";
        try (PreparedStatement psLoad = dbc.prepareStatement(sql)) {
            psLoad.setString(1, username.toLowerCase());

            try (ResultSet rs = psLoad.executeQuery()) {
                return rs.next();
            }
        }
    }


    @Override
    public Blowfish createNew(Connection dbc, long user, String username, String passwordClearForm) throws SQLException {
        try {
            String sqlLock = "LOCK TABLES " + mTableName + " WRITE";
            Statement stLock = dbc.createStatement();
            stLock.execute(sqlLock);

            if (!usernameExists(dbc, username)) {
                Blowfish ret = new Blowfish(user, username, passwordClearForm);

                String sql = "INSERT INTO " + mTableName + " " +
                        "(user, username, password, username_lc) " +
                        "VALUES (?,?,?,?)";

                try (PreparedStatement psInsert = dbc.prepareStatement(sql)) {
                    psInsert.setLong(1, user);
                    psInsert.setString(2, username);
                    psInsert.setString(3, BCrypt.hashpw(passwordClearForm, BCrypt.gensalt()));
                    psInsert.setString(4, username.toLowerCase());
                    psInsert.executeUpdate();
                }

                return ret;
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
    public boolean changePassword(Connection dbc, long userId, String passwordClearForm) throws SQLException {
        String sql = "UPDATE " + mTableName + " SET " +
                "password = ? " +
                "WHERE user = ?";

        try (PreparedStatement psUpdate = dbc.prepareStatement(sql)) {
            psUpdate.setString(1, BCrypt.hashpw(passwordClearForm, BCrypt.gensalt()));
            psUpdate.setLong(2, userId);
            int count = psUpdate.executeUpdate();

            return count == 1;
        }
    }


    protected String getTableName() {
        return USERS_TABLE_NAME;
    }
}
