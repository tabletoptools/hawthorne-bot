package io.tabletoptools.hawthorne;

import io.tabletoptools.hawthorne.commands.CommandBase;
import com.google.gson.Gson;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import io.tabletoptools.hawthorne.modulizer.Modulizer;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.listener.*;
import io.tabletoptools.hawthorne.model.RollSettings;
import io.tabletoptools.hawthorne.model.Statistics;
import io.tabletoptools.hawthorne.modules.coffee.CoffeeModule;
import io.tabletoptools.hawthorne.modules.hawthorne.HawthorneModule;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import io.tabletoptools.hawthorne.resources.GeneralCommands;
import io.tabletoptools.hawthorne.resources.GuideCommands;
import io.tabletoptools.hawthorne.resources.LootCommands;
import io.tabletoptools.hawthorne.services.HomebrewItemService;
import io.tabletoptools.hawthorne.services.ItemService;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HawthorneBot {

    public static final String BOT_OWNER_ICON = "https://cdn.discordapp.com/attachments/405639224084398090/405639389096706068/token_3.png";
    private static final String BOT_PREFIX = Config.instance().getString("prefix");
    public final String FOOTER = Config.instance().getString("footer");
    private static HawthorneBot bot;
    private JDA client;
    private HashMap<Long, RollSettings> rollMessages = new HashMap<>();
    private String token;
    private String typeformToken;
    public final Color HAWTHORNE_PURPLE = new Color(250, 0, 255);
    private static final Object shutdownNotifier = new Object();
    private static boolean shutdown = false;
    private static boolean isShutdown = false;
    private static boolean restart = false;

    private static Message statusMessage;

    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public static HawthorneBot instance() {
        if (bot == null) {
            bot = new HawthorneBot();
        }
        return bot;
    }

    public static void main(String[] args) throws RateLimitedException, LoginException, InterruptedException {
        try {

            if (args.length == 0 && !System.getenv().containsKey("BOT_TOKEN")) {
                Loggers.APPLICATION_LOG.error("No bot token provided (argument or BOT_TOKEN env variable). Quitting.");
                return;
            }

            if (!System.getenv().containsKey("TYPEFORM_TOKEN")) {
                Loggers.APPLICATION_LOG.error("No typeform token submitted.");
            }

            String token = null;

            if (System.getenv().containsKey("BOT_TOKEN")) {
                token = System.getenv("BOT_TOKEN");
            }
            if (args.length != 0) {
                token = args[0];
            }

            Unirest.setObjectMapper(new ObjectMapper() {
                private Gson gson = new Gson();

                public <T> T readValue(String s, Class<T> aClass) {
                    try {
                        return gson.fromJson(s, aClass);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                public String writeValue(Object o) {
                    try {
                        return gson.toJson(o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            instance().start(token);
            instance().setTypeformToken(System.getenv("TYPEFORM_TOKEN"));
            Runtime.getRuntime().addShutdownHook(new Thread(HawthorneBot::performShutdown));

            scheduledExecutor.scheduleAtFixedRate(HawthorneBot::update, 30, 30, TimeUnit.MINUTES);
            scheduledExecutor.scheduleAtFixedRate(HawthorneBot::saveStatistics, 60, 60, TimeUnit.MINUTES);

            synchronized (shutdownNotifier) {
                while (!shutdown) {
                    shutdownNotifier.wait();
                }
            }
        } catch (Exception ex) {
            Loggers.APPLICATION_LOG.error("Exception: ", ex);
        } finally {
            performShutdown();
        }
    }

    private void start(String token) throws RateLimitedException, LoginException, InterruptedException {

        this.token = token;


        Loggers.APPLICATION_LOG.info("Loading data for the first time.");
        try {
            ItemService.instance();
            HomebrewItemService.instance().load();
        } catch (NotAuthenticatedException ex) {
            Loggers.APPLICATION_LOG.info("Not authenticated. Let's fix that, shall we?");
        }
        Loggers.APPLICATION_LOG.info("Starting command handler");
        CommandBase.instance().setPrefix(BOT_PREFIX)
                .setColor(HAWTHORNE_PURPLE)
                .registerCommandClass(GeneralCommands.class)
                .registerCommandClass(GuideCommands.class)
                .registerCommandClass(LootCommands.class);

        Modulizer.instance().loadModule(new CoffeeModule());
        //Modulizer.instance().loadModule(new APIModule());
        Modulizer.instance().loadModule(new HawthorneModule());

        Loggers.APPLICATION_LOG.info("Starting discord client...");
        getClient().addEventListener(new MessageListener());
        getClient().addEventListener(new ReactionListener());
        getClient().addEventListener(new GuildMemberListener());
        getClient().addEventListener(new HawthorneLogListener());
        //getClient().addEventListener(new AyaListener());
        getClient().addEventListener(new WeatherListener());
        getClient().addEventListener(new HalloweenListener());
        //getClient().addEventListener(new TypingListener());
        Loggers.APPLICATION_LOG.info("Successfully started discord client.");
        //Discord shutdown hook

    }

    public String getTypeformToken() {
        return typeformToken;
    }

    private void setTypeformToken(String typeformToken) {
        this.typeformToken = typeformToken;
    }

    public JDA getClient() {
        try {
            if (client == null) {
                client = new JDABuilder(AccountType.BOT)
                        .setToken(this.token)
                        .build();
            }
            return client;
        } catch (LoginException ex) {
            Loggers.APPLICATION_LOG.error("Error Logging in.", ex);
        }
        return null;
    }

    private static void performShutdown() {
        if (isShutdown) return;
        scheduledExecutor.shutdown();
        Loggers.APPLICATION_LOG.info("Removing all pending rolls.");
        instance().getRollMessages().forEach((id, settings) -> settings.getMessage().delete().queue());
        instance().getRollMessages().clear();
        Loggers.APPLICATION_LOG.info("Shutting down client.");

        instance().getClient().shutdown();

        Modulizer.instance().stop();

        isShutdown = true;
        if (restart) {
            System.exit(2);
        }
    }

    public void restart() {
        restart = true;
        shutdown();
    }

    public void shutdown() {
        synchronized (shutdownNotifier) {
            shutdown = true;
            shutdownNotifier.notify();
        }
    }

    private static void update() {
        try {
            Loggers.APPLICATION_LOG.debug("Updating loot generator items.");
            ItemService.instance().update();
            HomebrewItemService.instance().load();
        } catch (NotAuthenticatedException ex) {
            Loggers.APPLICATION_LOG.error("Exception: ", ex);
        }
    }

    public Boolean hasSettings(Long messageId) {
        return getRollMessages().containsKey(messageId);
    }

    public RollSettings getSettingsForMessage(Long messageId) {
        return getRollMessages().get(messageId);
    }

    public void addRollMessage(Long messageId, RollSettings settings) {
        getRollMessages().put(messageId, settings);
    }

    public void removeMessage(Long messageId) {
        getRollMessages().remove(messageId);
    }

    public HashMap<Long, RollSettings> getRollMessages() {
        return rollMessages;
    }

    public static void saveStatistics() {
        Statistics statistics = HawthorneLogListener.getStatistics();
        HawthorneLogListener.resetStatistics();
        saveStatistics(statistics);
    }

    public static void saveStatistics(Statistics statistics) {
        if (statusMessage == null) {
            statusMessage = HawthorneBot.instance().getClient().getTextChannelById(456409530851655680L).retrieveMessageById(456409584056139777L).complete();
        }
        Gson gson = new Gson();

        Statistics oldStatistics = gson.fromJson(statusMessage.getContentRaw().substring(8).split("}")[0] + "}", Statistics.class);
        statistics.setEventCount(oldStatistics.getEventCount() + statistics.getEventCount());
        statistics.setMessageCount(oldStatistics.getMessageCount() + statistics.getMessageCount());
        statistics.setPresenceUpdateCount(oldStatistics.getPresenceUpdateCount() + statistics.getPresenceUpdateCount());
        statistics.setTypingStartCount(oldStatistics.getTypingStartCount() + statistics.getTypingStartCount());
        statusMessage = statusMessage.editMessage("```json\n" + gson.toJson(statistics) + "\n```").complete();
    }

}
