/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 05.02.18 14:23
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
package io.tabletoptools.hawthorne.listener;

import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.*;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import io.tabletoptools.hawthorne.services.ItemService;
import io.tabletoptools.hawthorne.services.RandomWeightedObjectService;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        if (HawthorneBot.instance().hasSettings(event.getMessageIdLong())) {
            generateLootForMessage(event);
        }
        if (event.getChannel().getIdLong() == 475357531531640835L) {
            Message message = event.getJDA().getTextChannelById(event.getChannel().getIdLong()).getMessageById(event.getMessageIdLong()).complete();
            if (!message.getEmbeds().isEmpty() && message.getEmbeds().get(0).getTitle().equals("Audit Request")) {
                MessageEmbed embed = message.getEmbeds().get(0);
                message.delete().queue();
                Date date = new Date(message.getCreationTime().until(OffsetDateTime.now(), ChronoUnit.MILLIS));
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeToCompletion = formatter.format(date);

                MessageEmbed newEmbed = new EmbedBuilder()
                        .setTitle("User Audited")
                        .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
                        .setDescription(
                                new StringBuilder()
                                .append("Audited User: ")
                                .append(embed.getFooter().getText())
                        )
                        .setFooter("Time to completion: " + timeToCompletion, null)
                        .build();

                Message newMessage = new MessageBuilder()
                        .setEmbed(newEmbed)
                        .build();

                event.getChannel().sendMessage(newMessage).queue();
            }
        }
    }

    private void generateLootForMessage(GuildMessageReactionAddEvent event) {
        RollSettings settings = HawthorneBot.instance().getSettingsForMessage(event.getMessageIdLong());

        if (!event.getMember().equals(settings.getAuthor())) {
            event.getReaction().removeReaction().queue();
            return;
        }

        StringBuilder lootFieldBuilder = new StringBuilder().append("```\n");
        Integer APL = settings.getAPL().setScale(0, RoundingMode.HALF_UP).intValue();

        Loggers.APPLICATION_LOG.info("Rolled for <{}> players.", settings.getPlayerCount());

        for (int x = 0; x < settings.getPlayerCount(); x++) {
            try {
                Tier tier = RandomWeightedObjectService.getObject(ItemService.instance().getTiersPerLevel().get(APL));
                Category category = RandomWeightedObjectService.getObject(tier.getCategories());
                Item item = RandomWeightedObjectService.getObject(ItemService.instance().getItems(tier, category));

                Long amount = item.getAmountPerLevel().get(APL).getAmount();

                Loggers.APPLICATION_LOG.info("Rolled for player <{}>, <{}>x <{}>", x, amount, item.getName());
                String name = item.getName();

                String value = getItemOutput(amount, name);

                lootFieldBuilder.append(value);
            } catch (NotAuthenticatedException ex) {
                lootFieldBuilder.append("Not Authenticated.");
            }
            if (x < settings.getPlayerCount()) {
                lootFieldBuilder.append("\n");
            }
        }

        Loggers.APPLICATION_LOG.info(lootFieldBuilder.toString());

        try {
            final BigDecimal zero = BigDecimal.valueOf(0);
            Optional<Tier> maxTierOptional = ItemService.instance().getTiersPerLevel().get(APL).stream().filter(tier -> tier.getWeight().compareTo(zero) > 0).max(Comparator.comparingInt(a -> Integer.parseInt(a.getName().substring(1))));

            int maxTier = maxTierOptional.map(tier1 -> Integer.parseInt(tier1.getName().substring(1))).orElse(3);
            DynamicAmount d10 = DynamicAmount.withQuery("1d10");
            int rolls = 1;
            while(rolls > 0) {
                rolls--;
                while (d10.getAmount() == 10L) {
                    int tier = 0;
                    while (d10.getAmount() == 10L) {
                        if(tier < maxTier) tier++;
                        else {
                            rolls++;
                            break;
                        }
                    }
                    final int finalTier = tier;
                    Tier itemTier = ItemService.instance()
                            .getTiersPerLevel()
                            .get(APL)
                            .stream()
                            .filter(t -> ("T" + finalTier).equals(t.getName()))
                            .collect(Collectors.toList()).get(0);

                    Category category = RandomWeightedObjectService.getObject(itemTier.getCategories());

                    Item item = RandomWeightedObjectService.getObject(ItemService.instance().getItems(itemTier, category));

                    Long amount = item.getAmountPerLevel().get(APL).getAmount();
                    String name = item.getName();

                    String value = getItemOutput(amount, name);

                    lootFieldBuilder
                            .append("\n")
                            .append(value);

                }
            }
        }
        catch(Exception ex) {
            Loggers.APPLICATION_LOG.error("Exception creating a dynamic amount, check syntax: ", ex);
        }

        lootFieldBuilder.append("```");

        String lootField = lootFieldBuilder.toString();

        MessageEmbed.AuthorInfo originalAuthor = settings.getMessage().getEmbeds().get(0).getAuthor();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(originalAuthor.getName(), null, originalAuthor.getIconUrl())
                .setTitle(originalAuthor.getName() + " rolled for loot!")
                .setThumbnail("https://cdn.discordapp.com/attachments/386516710984777730/410033026500788225/token_1.png")
                .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
                .setFooter(HawthorneBot.instance().FOOTER, "https://cdn.discordapp.com/attachments/405639224084398090/405639389096706068/token_3.png")
                .addField("Player Count", settings.getPlayerCount().toString(), true)
                .addField("Average Party Level", settings.getAPL().toString(), true)
                .addField("Loot - Items", lootField, false)
                .build();

        settings.getMessage().editMessage(embed).queue();
        settings.getMessage().clearReactions().queue();
        HawthorneBot.instance().removeMessage(event.getMessageIdLong());
    }

    private String getItemOutput(Long amount, String name) {
        StringBuilder valueBuilder = new StringBuilder();

        if(amount != 1 && !name.contains("%amount%")) {
            valueBuilder.append(amount)
                    .append("x ");
        }

        if (name.contains("%amount%")) {
            valueBuilder.append(name.replace("%amount%", amount.toString()));
        }
        else valueBuilder.append(name);

        return valueBuilder.toString();
    }
}
