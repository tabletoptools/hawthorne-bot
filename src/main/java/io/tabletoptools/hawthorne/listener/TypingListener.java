/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 06.02.18 22:13
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
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TypingListener extends ListenerAdapter {

    private static TextChannel notificationChannel = HawthorneBot.instance().getClient().getTextChannelById(410544768784007188L);
    private static String mention = HawthorneBot.instance().getClient().getUserById(135468266695950336L).getAsMention();

    @Override
    public void onUserTyping(UserTypingEvent event) {
        if(event.getChannel().getIdLong() == 343396172293210112L) {

            notificationChannel.sendMessage(mention + " : " + event.getMember().getEffectiveName() + " is typing in game-ads.").queue();

        }
    }
}
