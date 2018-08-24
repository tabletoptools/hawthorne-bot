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

import ch.hive.discord.bots.commands.CommandBase;
import io.tabletoptools.discord.modulizer.Modulizer;
import io.tabletoptools.hawthorne.model.ListMessageInstance;
import io.tabletoptools.hawthorne.model.LookupItem;
import io.tabletoptools.hawthorne.services.HomebrewItemService;
import io.tabletoptools.hawthorne.util.SearchUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public class MessageListener extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        CommandBase.instance().process(event);

        if ("/auditme".equals(event.getMessage().getContentRaw())) {

            event.getMessage().delete().queue();
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Audit Request")
                    .setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl())
                    .setDescription(new StringBuilder()
                            .append(event.getAuthor().getName())
                            .append(" would like to get audited.")
                    )
                    .setFooter(event.getAuthor().getName() +
                            "#" +
                            event.getAuthor().getDiscriminator() +
                            " - " +
                            event.getAuthor().getId(), null)
                    .build();

            Message message = new MessageBuilder()
                    .setEmbed(embed)
                    .build();

            event.getJDA().getTextChannelById(475357531531640835L)
                    .sendMessage(message)
                    .queue(sentMessage -> sentMessage.addReaction("✅").queue());
        }
        if (event.getMessage().getContentRaw().startsWith("/homebrew")) {

            List<LookupItem> items = HomebrewItemService.instance().getItems();

            String query = event.getMessage().getContentRaw().substring(10).trim().toLowerCase();

            List<LookupItem> selection = SearchUtils.search(query, items);

            if (selection.size() == 1) {
                event.getMessage().delete().queue();
                event.getChannel().sendMessage(selection.get(0).getMessage()).queue();
            } else if (selection.size() > 1) {
                ListMessageInstance listMessageInstance = new ListMessageInstance<>(selection);
                Message message = event.getChannel().sendMessage(listMessageInstance.getMessage()).complete();
                listMessageInstance.initialise(message.getChannel().getIdLong(), message.getIdLong(), event.getAuthor().getIdLong());
                event.getMessage().delete().queue();
            }
            event.getMessage().delete().queue();
        } else if (event.getMessage().getContentRaw().matches("^[cnp]$")) {
            if (ListMessageInstance.hasInstance(event.getChannel().getIdLong(), event.getAuthor().getIdLong())) {
                ListMessageInstance
                        .getInstances(event.getChannel().getIdLong(), event.getAuthor().getIdLong())
                        .forEach(instance -> instance.command(event, event.getMessage().getContentRaw()));
            }
        } else if (event.getMessage().getContentRaw().matches("^[1-8]$")
                && ListMessageInstance.hasInstance(event.getChannel().getIdLong(), event.getAuthor().getIdLong())) {
            ListMessageInstance
                    .getInstances(event.getChannel().getIdLong(), event.getAuthor().getIdLong())
                    .forEach(instance -> event.getChannel()
                            .sendMessage(instance.choose(Integer.parseInt(event.getMessage().getContentRaw())))
                            .queue());
            event.getMessage().delete().queue();
        }

        if (event.getAuthor().getIdLong() == 135468266695950336L) {

            Modulizer.instance().process(event);
        }
    }

}
