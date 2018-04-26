package io.tabletoptools.hawthorne.modules.api;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.discord.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.modules.formhooks.DiscordUser;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONArray;
import spark.ResponseTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

@Command("api")
public class APIModule extends Module {

    public static final Guild HAWTHORNE_GUILD = HawthorneBot.instance().getClient().getGuildById(308324031478890497L);
    public static final String DISCORD_API_BASE_URL = "https://discordapp.com/api/v6";
    public static final HashMap<String, DiscordUser> AUTH_TOKEN_USER_MAP = new HashMap<>();
    private ScheduledExecutorService scheduledExecutorService;
    private ResponseTransformer responseTransformer = new ResponseTransformer() {
        private Gson gson = new Gson();
        @Override
        public String render(Object o) throws Exception {
            return gson.toJson(o);
        }
    };

    @Override
    public void onLoad() {

        Loggers.APPLICATION_LOG.info("Starting Spark Server.");
        staticFiles.location("/public");
        get("/status", (request, response) -> "Okay");
        path("/api", () -> {
            before("/*", (q, a) -> {
                a.header("Access-Control-Allow-Origin", "*");
                a.header("Access-Control-Allow-Credentials", "true");
                a.header("Access-Control-Allow-Headers", "authorization");
            });
            before("/*", (q, a) -> {
                if(q.headers("Authorization") == null && !"OPTIONS".equals(q.requestMethod())) halt(403, "Not authorised.");
            });
            get("/user", "application/json", (request, response) -> {
                String auth = request.headers("Authorization");
                return getUser(auth);
            }, responseTransformer);
            get("/in-hawthorne", (request, response) -> {
                String auth = request.headers("Authorization");
                JSONArray guilds = Unirest.get(DISCORD_API_BASE_URL + "/users/@me/guilds")
                        .header("Authorization", auth)
                        .asJson().getBody().getArray();

                return guilds.toList().stream().anyMatch(guild -> "308324031478890497".equals(((HashMap)guild).get("id")));
            });
            get("/roles", "application/json", (request, response) -> {
                        List<Role> roles = HAWTHORNE_GUILD.getMemberById(getUser(request.headers("Authorization")).getId()).getRoles();
                        Map<String, String> idNameMap = new HashMap<>();
                        roles.forEach(role -> idNameMap.put(role.getId(), role.getName()));
                        return idNameMap;
            }, responseTransformer);
            options("/*", (request, response) -> "");
        });
        /*before(((request, response) -> {
            if(!this.isEnabled()) halt(503, "API Module not enabled.");
        }));*/
        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });
    }

    private DiscordUser getUser(String authorization) throws UnirestException {
        if(AUTH_TOKEN_USER_MAP.containsKey(authorization)) return AUTH_TOKEN_USER_MAP.get(authorization);
        DiscordUser user = Unirest.get(DISCORD_API_BASE_URL + "/users/@me")
                .header("Authorization", authorization)
                .asObject(DiscordUser.class)
                .getBody();
        return AUTH_TOKEN_USER_MAP.put(authorization, user);
    }

    @Override
    public void onUnload() {
        stop();
        if (!scheduledExecutorService.isShutdown() && !scheduledExecutorService.isTerminated()) scheduledExecutorService.shutdown();
    }

    @Override
    public void onEnable() {
        if (!isEnabled()) {
            //Restart Scheduler
            if (this.scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(AUTH_TOKEN_USER_MAP::clear, 1, 1, TimeUnit.DAYS);
        }
    }

    @Override
    public void onDisable() {
        if (isEnabled()) {
            if (!scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
        }
    }
}
