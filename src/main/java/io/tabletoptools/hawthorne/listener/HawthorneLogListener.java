/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 09.02.18 16:30
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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class HawthorneLogListener extends ListenerAdapter {

    private final TextChannel jesseLogChannel = HawthorneBot.instance().getClient().getTextChannelById(419296624808951809L);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().getIdLong() == 169443108478648321L) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl())
                    .setFooter(event.getChannel().getName(), null)
                    .setDescription(event.getMessage().getContentRaw());
            jesseLogChannel.sendMessage(eb.build()).queue();
        }

        /*if(event.getGuild().getIdLong() == 308324031478890497L) {
            HawthorneLogService.instance().log(event);
        }*/
    }
}
