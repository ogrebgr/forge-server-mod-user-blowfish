package com.bolyartech.forge.server.module.user_blowfish;

import com.bolyartech.forge.server.db.DbPool;
import com.bolyartech.forge.server.module.HttpModule;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbh;
import com.bolyartech.forge.server.module.user.data.screen_name.ScreenNameDbhImpl;
import com.bolyartech.forge.server.module.user.data.user.UserDbh;
import com.bolyartech.forge.server.module.user.data.user.UserDbhImpl;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.BlowfishDbhImpl;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfishDbh;
import com.bolyartech.forge.server.module.user_blowfish.data.UserBlowfishDbhImpl;
import com.bolyartech.forge.server.module.user_blowfish.endpoints.AutoregistrationBfEp;
import com.bolyartech.forge.server.module.user_blowfish.endpoints.LoginBfEp;
import com.bolyartech.forge.server.module.user_blowfish.endpoints.RegistrationBfEp;
import com.bolyartech.forge.server.module.user_blowfish.endpoints.RegistrationPostAutoBfEp;
import com.bolyartech.forge.server.route.PostRoute;
import com.bolyartech.forge.server.route.Route;

import java.util.ArrayList;
import java.util.List;


public class BlowfishUserModule implements HttpModule {
    private static final String DEFAULT_PATH_PREFIX = "/api/user/bf/";

    private static final String MODULE_SYSTEM_NAME = "user_blowfish";
    private static final int MODULE_VERSION_CODE = 1;
    private static final String MODULE_VERSION_NAME = "1.0.0";

    private final String pathPrefix;
    private final DbPool dbPool;
    private final UserBlowfishDbh userBlowfishDbh;
    private final UserDbh userDbh;
    private final BlowfishDbh blowfishDbh;
    private final ScreenNameDbh screenNameDbh;


    public static BlowfishUserModule createDefault(DbPool dbPool) {
        return new BlowfishUserModule(dbPool,
                new UserBlowfishDbhImpl(),
                new UserDbhImpl(),
                new BlowfishDbhImpl(),
                new ScreenNameDbhImpl());
    }


    public BlowfishUserModule(String pathPrefix, DbPool dbPool, UserBlowfishDbh userBlowfishDbh, UserDbh userDbh,
                              BlowfishDbh blowfishDbh, ScreenNameDbh screenNameDbh) {
        this.pathPrefix = pathPrefix;
        this.dbPool = dbPool;
        this.userBlowfishDbh = userBlowfishDbh;
        this.userDbh = userDbh;
        this.blowfishDbh = blowfishDbh;
        this.screenNameDbh = screenNameDbh;
    }


    public BlowfishUserModule(DbPool dbPool, UserBlowfishDbh userBlowfishDbh, UserDbh userDbh, BlowfishDbh blowfishDbh,
                              ScreenNameDbh screenNameDbh) {


        this(DEFAULT_PATH_PREFIX, dbPool, userBlowfishDbh, userDbh, blowfishDbh, screenNameDbh);
    }


    @Override
    public List<Route> createRoutes() {
        List<Route> ret = new ArrayList<>();

        ret.add(new PostRoute(pathPrefix + "autoregister",
                new AutoregistrationBfEp(dbPool, userDbh, blowfishDbh, userBlowfishDbh)));
        ret.add(new PostRoute(pathPrefix + "login",
                new LoginBfEp(dbPool, userDbh, blowfishDbh, screenNameDbh)));
        ret.add(new PostRoute(pathPrefix + "register",
                new RegistrationBfEp(dbPool, userDbh, blowfishDbh, userBlowfishDbh, screenNameDbh)));
        ret.add(new PostRoute(pathPrefix + "register_postauto",
                new RegistrationPostAutoBfEp(dbPool, userDbh, blowfishDbh, userBlowfishDbh, screenNameDbh)));


        return ret;
    }


    @Override
    public String getSystemName() {
        return MODULE_SYSTEM_NAME;
    }


    @Override
    public String getShortDescription() {
        return "";
    }


    @Override
    public int getVersionCode() {
        return MODULE_VERSION_CODE;
    }


    @Override
    public String getVersionName() {
        return MODULE_VERSION_NAME;
    }
}
