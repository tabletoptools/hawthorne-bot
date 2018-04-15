/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 05.02.18 14:18
 * 
 * Copyright (c) 2017 by bluesky IT-Solutions AG,
 * Kaspar-Pfeiffer-Strasse 4, 4142 Muenchenstein, Switzerland.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of bluesky IT-Solutions AG ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with bluesky IT-Solutions AG.
 */
package io.tabletoptools.hawthorne.resources;

import ch.hive.discord.bots.commands.Command;
import ch.hive.discord.bots.commands.Constraint;
import ch.hive.discord.bots.commands.Description;
import ch.hive.discord.bots.commands.Parameter;
import io.tabletoptools.hawthorne.*;
import io.tabletoptools.hawthorne.constraint.*;
import io.tabletoptools.hawthorne.exception.NoItemFoundException;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.Category;
import io.tabletoptools.hawthorne.model.Item;
import io.tabletoptools.hawthorne.model.RollSettings;
import io.tabletoptools.hawthorne.model.Tier;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import io.tabletoptools.hawthorne.services.ItemService;
import io.tabletoptools.hawthorne.services.RandomItemService;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LootCommands {

    @Command("genloot")
    @Description("Process loot! :D")
    @Constraint(value = {
            BotOwnerConstraint.class,
            HawthorneHeadOfStaffConstraint.class,
            HawthorneAdminConstraint.class,
            HawthorneDMConstraint.class,
            HawthorneTrialDMConstraint.class,
            TesterConstraint.class
    }, enforceAll = false)
    public static void loot(MessageReceivedEvent event, @Parameter("Player Count") Integer playerCount, @Parameter("APL") String aplAsString) {

        event.getMessage().delete().queue();

        BigDecimal apl;
        try {
            apl = new BigDecimal(aplAsString);
        } catch (Exception ex) {
            event.getChannel().sendMessage("Error: APL is not numeric.");
            return;
        }

        if (playerCount > 20 || playerCount < 1) {
            event.getChannel().sendMessage("Error. Please specify a player count between 1 and 20.").queue();
            return;
        }

        if (apl.compareTo(BigDecimal.valueOf(1)) < 0 || apl.compareTo(BigDecimal.valueOf(20)) > 0) {
            event.getChannel().sendMessage("Error. Please specify an APL between 1 and 20.").queue();
            return;
        }

        Loggers.APPLICATION_LOG.info("Creating a loot generation panel for user <{}> with <{}> players and apl <{}>",
                event.getMember().getEffectiveName(), playerCount, apl.toString());

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(event.getMember().getEffectiveName(), null, event.getAuthor().getAvatarUrl())
                .setTitle(event.getAuthor().getName() + " wants to roll for loot!")
                .setDescription("Click the die to roll.")
                .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                .addField("Average Party Level", apl.toString(), true)
                .addField("Player Count", playerCount.toString(), true)
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

    @Command("item")
    @Description("Get item rarity")
    @Constraint(value = {BotOwnerConstraint.class, HawthorneAdminConstraint.class, HawthorneHeadOfStaffConstraint.class, HawthorneRulesConstraint.class,
            TesterConstraint.class}, enforceAll = false)
    public static void item(MessageReceivedEvent event, @Parameter("Name") String... nameArray) {

        try {
            event.getMessage().delete().queue();
            StringBuilder nameBuilder = new StringBuilder();
            for (String s : nameArray) {
                nameBuilder.append(s)
                        .append(" ");
            }

            String name = nameBuilder.toString();

            Integer topScore = 0;
            Item topScoring = null;

            for (Item item : ItemService.instance().getItemList()) {
                Integer score = FuzzySearch.ratio(name, item.getName());
                if (score > topScore) {
                    topScoring = item;
                    topScore = score;
                }
            }


            Map<Integer, BigDecimal> chances = new HashMap<>();

            final Item item = topScoring;
            ItemService.instance().getLevelBrackets().forEach((level, weightedTierCategoryPairs) -> {
                try {
                    BigDecimal chance = RandomItemService.instance().getChanceForItem(level, item);
                    if (chance.compareTo(BigDecimal.valueOf(0)) != 0) {
                        chances.put(level, chance);
                    }
                } catch (NoItemFoundException | NotAuthenticatedException ex) {

                }
            });

            StringBuilder builder = new StringBuilder()
                    .append("```")
                    .append("\n");

            chances.forEach((level, chance) -> {
                builder.append(level < 10 ? "0" + level : level)
                        .append(": ")
                        .append(chance.setScale(2, RoundingMode.HALF_UP).toString())
                        .append("%")
                        .append(" | ")
                        .append(item.getAmountPerLevel().get(level) + "x")
                        .append("\n");
            });

            builder.append("```");

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(topScoring.getName())
                    .setDescription("Item Rarity")
                    .addField("Item Tier", topScoring.getTier().getTier(), true)
                    .addField("Item Category", topScoring.getCategory().name(), true)
                    .addField("Chance of Item per Level", builder.toString(), false)
                    .setFooter(HawthorneBot.instance().FOOTER, HawthorneBot.BOT_OWNER_ICON)
                    .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                    .build();

            event.getChannel().sendMessage(embed).queue();
        } catch (NotAuthenticatedException ex) {
            event.getChannel().sendMessage("Not authenticated. Please run the authenticate command.").queue();
        }
    }

    @Command("tier")
    @Description("Get tier rarity")
    @Constraint(
            value = {
                    BotOwnerConstraint.class,
                    HawthorneAdminConstraint.class,
                    HawthorneHeadOfStaffConstraint.class,
                    HawthorneRulesConstraint.class,
                    TesterConstraint.class
            }, enforceAll = false)
    public static void tier(MessageReceivedEvent event, @Parameter("Level") Integer level, @Parameter("Tier") Integer tier) {
        try {
            event.getMessage().delete().queue();
            String tierString = "T" + tier;

            BigDecimal consumableChance = RandomItemService.instance().getChanceForTier(level, Tier.valueOf(tierString), Category.Consumable);
            BigDecimal nonCombatChance = RandomItemService.instance().getChanceForTier(level, Tier.valueOf(tierString), Category.NonCombat);
            BigDecimal combatChance = RandomItemService.instance().getChanceForTier(level, Tier.valueOf(tierString), Category.Combat);
            BigDecimal summoningChance = RandomItemService.instance().getChanceForTier(level, Tier.valueOf(tierString), Category.Summoning);


            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Tier rarity: " + tierString)
                    .setDescription("Tier Rarity")
                    .addField("For Level", String.valueOf(level), true)
                    .addField("Consumable", consumableChance.setScale(2, RoundingMode.HALF_UP).toString() + "%", false)
                    .addField("Non-Combat", nonCombatChance.setScale(2, RoundingMode.HALF_UP).toString() + "%", false)
                    .addField("Combat", combatChance.setScale(2, RoundingMode.HALF_UP).toString() + "%", false)
                    .addField("Summoning", summoningChance.setScale(2, RoundingMode.HALF_UP).toString() + "%", false)
                    .setFooter(HawthorneBot.instance().FOOTER, HawthorneBot.BOT_OWNER_ICON)
                    .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                    .build();

            event.getChannel().sendMessage(embed).queue();

        } catch (NotAuthenticatedException ex) {
            event.getChannel().sendMessage("Not authenticated. Please run the authenticate command.").queue();
        }
    }

    @Command("bracket")
    @Description("Get bracket rarity")
    @Constraint(value = {BotOwnerConstraint.class, HawthorneAdminConstraint.class, HawthorneHeadOfStaffConstraint.class, HawthorneRulesConstraint.class,
            TesterConstraint.class}, enforceAll = false)
    public static void bracket(MessageReceivedEvent event, @Parameter("Level") Integer level) {
        try {
            event.getMessage().delete().queue();

            Map<String, BigDecimal> chances = new HashMap<>();
            ItemService.instance().getLevelBracket(level).forEach(weightedTierCategoryPair -> {
                try {
                    BigDecimal chance = RandomItemService.instance().getChanceForTier(level, weightedTierCategoryPair.getTier(), weightedTierCategoryPair.getCategory());

                    if (chance.compareTo(BigDecimal.valueOf(0)) != 0) {
                        chances.put(weightedTierCategoryPair.getTier().getTier() + " " + weightedTierCategoryPair.getCategory(), chance);
                    }

                } catch (NotAuthenticatedException ex) {
                    event.getChannel().sendMessage("Not authenticated. Please run the authenticate command.").queue();
                }
            });

            StringBuilder builder = new StringBuilder()
                    .append("```")
                    .append("\n");

            chances.forEach((pair, chance) -> {
                builder.append(pair)
                        .append(": ")
                        .append(chance.setScale(2, RoundingMode.HALF_UP).toString())
                        .append("%")
                        .append("\n");
            });

            builder.append("```");

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Level Bracket Rarity: " + level)
                    .setDescription(builder)
                    .setFooter(HawthorneBot.instance().FOOTER, HawthorneBot.BOT_OWNER_ICON)
                    .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                    .build();

            event.getChannel().sendMessage(embed).queue();

        } catch (NotAuthenticatedException ex) {
            event.getChannel().sendMessage("Not authenticated. Please run the authenticate command.").queue();
        }

    }

    @Command("lootsettings")
    @Description("Modify loot settings")
    public static void lootsettings(MessageReceivedEvent event, @Parameter("Setting") String setting, @Parameter("Value") String value) {

        event.getMessage().delete().queue();
        boolean success = false;
        if ("respectItemCount".toLowerCase().equals(setting.toLowerCase())) {
            Boolean respectItemCount = Boolean.valueOf(value);
            RandomItemService.instance().setRespectItemCount(respectItemCount);
            success = true;
        }
        if (success) event.getChannel().sendMessage("Success.").queue();
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
                    .addField("Ping", String.valueOf(HawthorneBot.instance().getClient().getPing()) + " ms", false)
                    .addField("Amount of Items", String.valueOf(ItemService.instance().getItemList().size()), false)
                    .addField("Amount of Level Brackets", String.valueOf(ItemService.instance().getLevelBrackets().size()), false)
                    .addField("Pending Loot Rolls", String.valueOf(HawthorneBot.instance().getRollMessages().size()), false)
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
