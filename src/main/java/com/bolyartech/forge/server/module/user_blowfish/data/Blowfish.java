package com.bolyartech.forge.server.module.user_blowfish.data;

public final class Blowfish {
    private final long mUser;
    private final String mUsername;
    private final String mPasswordHash;


    public Blowfish(long user, String username, String passwordHash) {
        mUser = user;
        mUsername = username;
        mPasswordHash = passwordHash;
    }


    public long getUser() {
        return mUser;
    }


    public String getUsername() {
        return mUsername;
    }


    public String getPasswordHash() {
        return mPasswordHash;
    }
}
