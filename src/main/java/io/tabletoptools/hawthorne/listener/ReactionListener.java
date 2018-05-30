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
import io.tabletoptools.hawthorne.exception.NoItemFoundException;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.Item;
import io.tabletoptools.hawthorne.model.RollSettings;
import io.tabletoptools.hawthorne.services.RandomItemService;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.math.RoundingMode;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        if (HawthorneBot.instance().hasSettings(event.getMessageIdLong())) {
            generateLootForMessage(event);
        }
    }

    private void generateLootForMessage(GuildMessageReactionAddEvent event) {
        RollSettings settings = HawthorneBot.instance().getSettingsForMessage(event.getMessageIdLong());

        if (!event.getMember().equals(settings.getAuthor())) {
            event.getReaction().removeReaction().queue();
            return;
        }

        StringBuilder lootFieldBuilder = new StringBuilder().append("```");

        for (int x = 0; x < settings.getPlayerCount(); x++) {
            try {
                Item item = RandomItemService.instance().getRandomItem(settings.getAPL().setScale(0, RoundingMode.HALF_UP).intValue());

                lootFieldBuilder
                        .append(item.getTier().getTier())
                        .append(" | ")
                        .append(item.getChance().setScale(2, RoundingMode.HALF_UP))
                        .append("%")
                        .append(" | ")
                        .append(item.getAmountPerLevel().get(settings.getAPL().setScale(0, RoundingMode.HALF_UP).intValue()).getAmount())
                        .append("x ")
                        .append(item.getName());
            } catch (NotAuthenticatedException ex) {
                lootFieldBuilder.append("Not Authenticated.");
            } catch (NoItemFoundException ex) {
                lootFieldBuilder
                        .append("No item found for tier <")
                        .append(ex.getTierCategoryPair().getTier().toString())
                        .append("> and category <")
                        .append(ex.getTierCategoryPair().getCategory().toString())
                        .append(">");
            }
            if (x - 1 < settings.getPlayerCount()) {
                lootFieldBuilder.append("\n");
            }
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
                .addField("Average Party Level", settings.getAPL().toString(), true)
                .addField("Player Count", settings.getPlayerCount().toString(), true)
                .addField("Loot - Items", lootField, false)
                .build();

        settings.getMessage().editMessage(embed).queue();
        settings.getMessage().clearReactions().queue();
        HawthorneBot.instance().removeMessage(event.getMessageIdLong());
    }
}
