package io.tabletoptools.hawthorne.resources;

import ch.hive.discord.bots.commands.Command;
import ch.hive.discord.bots.commands.Constraint;
import ch.hive.discord.bots.commands.Description;
import ch.hive.discord.bots.commands.Parameter;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.constraint.*;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.RollSettings;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import io.tabletoptools.hawthorne.services.HomebrewItemService;
import io.tabletoptools.hawthorne.services.ItemService;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class LootCommands {

    @Command("genloot")
    @Description("Generate loot! :D")
    /*@Constraint(value = {
            BotOwnerConstraint.class,
            HawthorneHeadOfStaffConstraint.class,
            HawthorneAdminConstraint.class,
            HawthorneDMConstraint.class,
            HawthorneTrialDMConstraint.class,
            TesterConstraint.class
    }, enforceAll = false)*/
    public static void loot(MessageReceivedEvent event, @Parameter("Player Count") Integer playerCount, @Parameter("APL") String aplAsString) {

        event.getMessage().delete().queue();

        BigDecimal apl;
        try {
            apl = new BigDecimal(aplAsString);
        } catch (Exception ex) {
            event.getChannel().sendMessage("Error: APL is not numeric.").queue();
            return;
        }

        if (playerCount > 20 || playerCount < 1) {
            event.getChannel().sendMessage("Error. Please specify a player count between 1 and 20.").queue();
            return;
        }

        if (apl.compareTo(BigDecimal.valueOf(3)) < 0 || apl.compareTo(BigDecimal.valueOf(20)) > 0) {
            event.getChannel().sendMessage("Error. Please specify an APL between 3 and 20.").queue();
            return;
        }

        Loggers.APPLICATION_LOG.info("Creating a loot generation panel for user <{}> with <{}> players and apl <{}>",
                event.getMember().getEffectiveName(), playerCount, apl.toString());

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getAvatarUrl())
                .setTitle(event.getAuthor().getName() + " wants to roll for loot!")
                .setDescription("Click the die to roll.")
                .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                .addField("Player Count", playerCount.toString(), true)
                .addField("Average Party Level", apl.toString(), true)
                .setFooter(HawthorneBot.instance().FOOTER, HawthorneBot.BOT_OWNER_ICON)
                .build();

        event.getChannel().sendMessage(embed).queue(message -> {
            storeLootRequest(message, playerCount, apl, event.getMember());
            message.addReaction("\uD83C\uDFB2").queue();
        });
    }

    @Command("update")
    @Description("Update item list")
    @Constraint(value = {
            HawthorneRulesConstraint.class,
            BotOwnerConstraint.class,
            HawthorneHeadOfStaffConstraint.class,
            HawthorneAdminConstraint.class,
            TesterConstraint.class
    }, enforceAll = false)
    public static void update(MessageReceivedEvent event) {
        event.getMessage().delete().queue();
        event.getChannel().sendMessage("Updating Data...").queue(message -> {
            try {
                ItemService.instance().update();
                HomebrewItemService.instance().update();
                message.editMessage("Finished updating Data.").queue();
            } catch (NotAuthenticatedException ex) {
                message.editMessage("Not Authenticated. Please use the authenticate command.").queue();
            }
        });
    }

    @Command("authenticate")
    @Description("Log in to Google Drive. Requires name and optionally code")
    @Constraint(value = {BotOwnerConstraint.class})
    public static void authenticate(MessageReceivedEvent event, @Parameter("Arguments") String... arguments) {
        try {
            event.getMessage().delete().queue();

            if (arguments.length == 2) {
                ItemService.instance().authenticate(arguments[0], arguments[1]);
                event.getChannel().sendMessage("Authenticated.").queue();
            } else {
                event.getChannel().sendMessage(ItemService.instance().requestAuthenticationURL()).queue();
            }
        } catch (NotAuthenticatedException | IOException ex) {

        }
    }

    @Command("status")
    @Description("Get bot status")
    public static void status(MessageReceivedEvent event) {
        try {
            event.getMessage().delete().queue();

            Long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

            String uptimeString = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(uptime),
                    TimeUnit.MILLISECONDS.toMinutes(uptime) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(uptime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime)));

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Bot Status")
                    .setDescription("Current metrics of the bot in general")
                    .addField("Ping", String.valueOf(HawthorneBot.instance().getClient().getPing()) + " ms", true)
                    .addField("Amount of Items", String.valueOf(ItemService.instance().getItems().size()), true)
                    .addField("Pending Loot Rolls", String.valueOf(HawthorneBot.instance().getRollMessages().size()), true)
                    .addField("Uptime", uptimeString, true)
                    .setFooter(HawthorneBot.instance().FOOTER, HawthorneBot.BOT_OWNER_ICON)
                    .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                    .build();

            event.getChannel().sendMessage(embed).queue();

        } catch (NotAuthenticatedException ex) {
            event.getChannel().sendMessage("Not authenticated. Please run the authenticate command.").queue();
        }

    }

    @Command("clear")
    @Description("Clear all pending loot rolls")
    @Constraint(value = {BotOwnerConstraint.class, HawthorneAdminConstraint.class, HawthorneHeadOfStaffConstraint.class, HawthorneRulesConstraint.class,
            TesterConstraint.class}, enforceAll = false)
    public static void clear(MessageReceivedEvent event) {
        event.getMessage().delete().queue();
        HawthorneBot.instance().getRollMessages().forEach((id, settings) -> {
            settings.getMessage().delete().queue();
        });
        HawthorneBot.instance().getRollMessages().clear();
        event.getMessage().getChannel().sendMessage("Success.").queue();
    }

    private static void storeLootRequest(Message message, Integer playerCount, BigDecimal apl, Member author) {
        RollSettings settings = new RollSettings(apl, playerCount, message, author);
        HawthorneBot.instance().addRollMessage(message.getIdLong(), settings);
    }

}
