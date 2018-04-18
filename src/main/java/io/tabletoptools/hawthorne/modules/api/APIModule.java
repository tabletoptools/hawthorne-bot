package io.tabletoptools.hawthorne.modules.api;

import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONArray;
import spark.ResponseTransformer;

import java.util.HashMap;

import static spark.Spark.*;

public class APIModule extends Module {

    public static final Guild HAWTHORNE_GUILD = HawthorneBot.instance().getClient().getGuildById(308324031478890497L);
    public static final String DISCORD_API_BASE_URL = "https://discordapp.com/api/v6";

    @Override
    public void onLoad() {
        Loggers.APPLICATION_LOG.info("Starting Spark Server.");
        staticFiles.location("/public");
        get("/status", (request, response) -> "Okay");
        path("/api", () -> {
            before("/*", (q, a) -> {
                if(q.headers("Authorization") == null && !"OPTIONS".equals(q.requestMethod())) halt(403, "Not authorised.");
                if(q.session().attribute("authenticated") == null && !q.url().endsWith("/login")) halt(403, "Not authorised.");
            });
            get("/user", "application/json", (request, response) -> {
                String auth = request.headers("Authorization");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Credentials", "true");
                response.header("Access-Control-Allow-Headers", "authorization");
                HttpResponse<JsonNode> userJson = Unirest.get(DISCORD_API_BASE_URL + "/users/@me")
                        .header("Authorization", auth)
                        .asJson();
                return userJson.getBody();
            }, jsonNode -> ((JsonNode)jsonNode).toString());
            get("/login", (request, response) -> {
                String auth = request.headers("Authorization");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Credentials", "true");
                response.header("Access-Control-Allow-Headers", "authorization");
                HttpResponse<JsonNode> userJson = Unirest.get(DISCORD_API_BASE_URL + "/users/@me")
                        .header("Authorization", auth)
                        .asJson();
                if(userJson.getStatus() == 200 && request.session().attribute("authenticated") == null) {
                    request.session(true);
                    request.session().attribute("authenticated",true);
                    request.session().attribute("userId", userJson.getBody().getObject().get("id"));
                    response.status(204);
                    return response;
                }
                else if (request.session().attribute("authenticated") != null) {
                    response.status(400);
                    return "Already logged in.";
                }
                else {
                    response.status(401);
                    return "Invalid Token.";
                }
            });
            get("/in-hawthorne", (request, response) -> {
                String auth = request.headers("Authorization");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Credentials", "true");
                response.header("Access-Control-Allow-Headers", "authorization");
                JSONArray guilds = Unirest.get(DISCORD_API_BASE_URL + "/users/@me/guilds")
                        .header("Authorization", auth)
                        .asJson().getBody().getArray();

                return guilds.toList().stream().anyMatch(guild -> "308324031478890497".equals(((HashMap)guild).get("id")));
            });
            get("/roles", "application/json", (request, response) ->
                    HAWTHORNE_GUILD.getMemberById(request.session().attribute("userId")).getRoles(),
                    new ResponseTransformer() {
                Gson gson = new Gson();
                @Override
                public String render(Object o) throws Exception {
                    return gson.toJson(o);
                }
            });
            options("/*", (request, response) -> {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Credentials", "true");
                response.header("Access-Control-Allow-Headers", "authorization");
                return "";
            });
        });
        /*before(((request, response) -> {
            if(!this.isEnabled()) halt(503, "API Module not enabled.");
        }));*/
        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });
    }

    @Override
    public void onUnload() {
        stop();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
