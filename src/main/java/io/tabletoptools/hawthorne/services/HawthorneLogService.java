/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 09.02.18 16:31
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
package io.tabletoptools.hawthorne.services;

import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class HawthorneLogService {

    private final Guild logServer = HawthorneBot.instance().getClient().getGuildById(411545283692855318L);

    private static HawthorneLogService instance;

    public static HawthorneLogService instance() {
        if (instance == null) {
            instance = new HawthorneLogService();
        }
        return instance;
    }

    public void log(MessageReceivedEvent event) {
        List<TextChannel> channels = logServer.getTextChannelsByName(event.getChannel().getName(), false);
        if(channels.size() == 0 || !channels.get(0).getName().equals(event.getChannel().getName())) {
            TextChannel channel = (TextChannel)logServer.getController().createTextChannel(event.getChannel().getName()).complete();
            channels.clear();
            channels.add(channel);
        }

        TextChannel channel = channels.get(0);

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(event.getAuthor().getName(), event.getAuthor().getAvatarUrl(), event.getAuthor().getAvatarUrl())
                .setDescription(event.getMessage().getContentRaw())
                .build();

        channel.sendMessage(embed).queue();
    }

}
