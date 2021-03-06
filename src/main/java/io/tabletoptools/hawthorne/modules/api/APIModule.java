package io.tabletoptools.hawthorne.modules.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.tabletoptools.hawthorne.modulizer.Module;
import io.tabletoptools.hawthorne.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.Config;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.model.AdventurerRegistration;
import io.tabletoptools.hawthorne.modules.formhooks.DiscordUser;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.json.JSONArray;
import spark.Request;
import spark.ResponseTransformer;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

@Command("api")
public class APIModule extends Module {

    private static final Guild HAWTHORNE_GUILD = HawthorneBot.instance().getClient().getGuildById(308324031478890497L);
    private static final String DISCORD_API_BASE_URL = "https://discord.com/api/v6";
    private static final HashMap<String, DiscordUser> AUTH_TOKEN_USER_MAP = new HashMap<>();
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

        port(Config.instance().getInteger("apiPort"));
        Loggers.APPLICATION_LOG.info("Starting Spark Server.");
        staticFiles.location("/public");
        before("/*", (q, a) -> {
            a.header("Access-Control-Allow-Origin", "*");
            a.header("Access-Control-Allow-Credentials", "true");
            a.header("Access-Control-Allow-Headers", "authorization,content-type");
        });
        get("/status", (request, response) -> "Okay");
        path("/api", () -> {
            before("/*", (q, a) -> {
                if (q.headers("Authorization") == null && !"OPTIONS".equals(q.requestMethod())) throw halt(403, "Not authorised.");
            });
            get("/user", "application/json", (request, response) -> {
                String auth = request.headers("Authorization");
                return getUser(auth);
            }, responseTransformer);
            get("/in-hawthorne", (request, response) -> {
                String auth = request.headers("Authorization");
                JSONArray guildsJson = Unirest.get(DISCORD_API_BASE_URL + "/users/@me/guilds")
                        .header("Authorization", auth)
                        .asJson().getBody().getArray();
                List<Object> guilds = new ArrayList<>();
                guildsJson.forEach(guilds::add);
                return guilds.stream().anyMatch(guild -> "308324031478890497".equals(((HashMap) guild).get("id")));
            });
            get("/roles", "application/json", (request, response) -> {
                List<Role> roles = HAWTHORNE_GUILD.getMemberById(getUser(request.headers("Authorization")).getId()).getRoles();
                Map<String, String> idNameMap = new HashMap<>();
                roles.forEach(role -> idNameMap.put(role.getId(), role.getName()));
                return idNameMap;
            }, responseTransformer);

            path("/request", () -> {
                post("/adventurer-registration", (request, response) -> {
                    Gson gson = new Gson();
                    AdventurerRegistration adventurerRegistration = gson.fromJson(request.body(), AdventurerRegistration.class);
                    DiscordUser user = this.getUser(request);
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New Adventurer Registration.")
                            .setAuthor(user.getUsername(), null, "https://cdn.discordapp.com/" + "avatars/" + user.getId() + "/" + user.getAvatar() + ".png?size=256")
                            .addField("Email", user.getEmail(), true)
                            .addField("Discord Handle", user.getUsername() + "#" + user.getDiscriminator(), true)
                            .addField("Birthdate", adventurerRegistration.getBirthdate().toString(), false)
                            .addField("Town Name", adventurerRegistration.getTownName(), true)
                            .addField("Rule Question", adventurerRegistration.getRuleTwo(), true)
                            .setFooter("User Id: " + user.getId(), null)
                            .setDescription("Check the Sheet for more information.")
                            .setColor(new Color(73, 98, 62))
                            .setThumbnail("https://cdn1.iconfinder.com/data/icons/ordinary-people/512/adventurer-512.png")
                            .build();
                    HAWTHORNE_GUILD.addRoleToMember(HAWTHORNE_GUILD.getMemberById(user.getId()), HawthorneBot.instance().getClient().getRoleById(445939304695595028L))
                            .queue();
                    try {
                        HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage(embed).queue();
                    } catch (IllegalArgumentException ex) {
                        HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage("Received an Adventurer Registration from "
                                + user.getUsername()
                                + "#"
                                + user.getDiscriminator()
                                + " that was too long for the bot to post.").queue();
                        throw halt(400, "Error: Registration is too long.");
                    }
                    return "";
                });
            });
        });
        options("/*", (request, response) -> "");
        get("/*", ((request, response) -> halt(404)));
        /*before(((request, response) -> {
            if(!this.isEnabled()) halt(503, "API Module not enabled.");
        }));*/
        after((request, response) -> response.header("Content-Encoding", "gzip"));
    }

    private DiscordUser getUser(Request request) throws UnirestException {
        return getUser(request.headers("Authorization"));
    }

    private DiscordUser getUser(String authorization) throws UnirestException {
        if (AUTH_TOKEN_USER_MAP.containsKey(authorization)) return AUTH_TOKEN_USER_MAP.get(authorization);
        HttpResponse<DiscordUser> response = Unirest.get(DISCORD_API_BASE_URL + "/users/@me")
                .header("Authorization", authorization)
                .asObject(DiscordUser.class);

        AUTH_TOKEN_USER_MAP.put(authorization, response.getBody());
        return response.getBody();
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
            scheduledExecutorService.scheduleAtFixedRate(AUTH_TOKEN_USER_MAP::clear, 1, 10, TimeUnit.MINUTES);
        }
    }

    @Override
    public void onDisable() {
        if (isEnabled() && !scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
    }
}
