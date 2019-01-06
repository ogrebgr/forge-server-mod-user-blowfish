package com.bolyartech.forge.server.module.user_blowfish.data;

public class BlowfishPostAutoDbhImpl extends BlowfishDbhImpl {
    private static final String USERS_TABLE_NAME = "user_blowfish_post_auto";

    @Override
    protected String getTableName() {
        return USERS_TABLE_NAME;
    }
}
