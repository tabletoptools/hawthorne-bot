package io.tabletoptools.hawthorne.modules.api;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.discord.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.model.AdventurerRegistration;
import io.tabletoptools.hawthorne.modules.formhooks.DiscordUser;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONArray;
import spark.Request;
import spark.ResponseTransformer;

import java.awt.*;
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

        port(80);
        Loggers.APPLICATION_LOG.info("Starting Spark Server.");
        staticFiles.location("/public");
        get("/status", (request, response) -> "Okay");
        path("/api", () -> {
            before("/*", (q, a) -> {
                a.header("Access-Control-Allow-Origin", "*");
                a.header("Access-Control-Allow-Credentials", "true");
                a.header("Access-Control-Allow-Headers", "authorization,content-type");
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

            path("/request", () -> {
                post("/adventurer-registration", (request, response) -> {
                    Gson gson = new Gson();
                    AdventurerRegistration adventurerRegistration = gson.fromJson(request.body(), AdventurerRegistration.class);
                    DiscordUser user = this.getUser(request);
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New Adventurer Registration.")
                            .setAuthor(user.getUsername(), null, "https://cdn.discordapp.com/" + "avatars/"+ user.getId() +"/"+ user.getAvatar() +".png?size=256")
                            .addField("Email", user.getEmail(), true)
                            .addField("Discord Handle", user.getUsername()+"#"+user.getDiscriminator(), true)
                            .addField("Birthdate", adventurerRegistration.getBirthdate().toString(), false)
                            .addField("Town Name", adventurerRegistration.getTownName(), true)
                            .addField("Rule Question", adventurerRegistration.getRuleTwo(), true)
                            .setFooter("User Id: " + user.getId(), null)
                            .setDescription("Check the Sheet for more information.")
                            .setColor(new Color(73, 98, 62))
                            .setThumbnail("https://cdn1.iconfinder.com/data/icons/ordinary-people/512/adventurer-512.png")
                            .build();
                    if(!HAWTHORNE_GUILD.getMemberById(user.getId()).getRoles().stream().anyMatch(role -> "445939304695595028".equals(role.getId())))
                        HAWTHORNE_GUILD
                                .getController()
                                .addSingleRoleToMember(
                                        HAWTHORNE_GUILD
                                                .getMemberById(user.getId()),
                                        HawthorneBot
                                                .instance()
                                                .getClient()
                                                .getRoleById(445939304695595028L))
                                .queue();
                    HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage(embed).queue();
                    return "";
                });
            });

            options("/*", (request, response) -> "");
        });
        /*before(((request, response) -> {
            if(!this.isEnabled()) halt(503, "API Module not enabled.");
        }));*/
        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });
    }

    private DiscordUser getUser(Request request) throws UnirestException {
        return getUser(request.headers("Authorization"));
    }

    private DiscordUser getUser(String authorization) throws UnirestException {
        if(AUTH_TOKEN_USER_MAP.containsKey(authorization)) return AUTH_TOKEN_USER_MAP.get(authorization);
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
        if (isEnabled()) {
            if (!scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
        }
    }
}
