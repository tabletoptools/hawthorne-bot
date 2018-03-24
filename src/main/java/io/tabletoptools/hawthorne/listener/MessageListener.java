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
import io.tabletoptools.hawthorne.Loggers;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Date;

public class MessageListener extends ListenerAdapter {

    private static Date lastAvalonKitten = new Date(new Date().getTime() - (1000L * 300L));

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        CommandBase.instance().process(event);

        //4valon Kitt3n B00T

        //Things to check for:
        //Is user Avalon
        //Does message contain Kitten?
        //Does message contain other creature?
        //Can bot talk

        /*if(event.getAuthor().getIdLong() == 210834183356940291L) {
            if(event.getMessage().getContentRaw().toLowerCase().contains("kitten")) {
                if(lastAvalonKitten.getTime() < (new Date().getTime() - (1000L * 300L))) {
                    lastAvalonKitten = new Date(new Date().getTime());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                event.getChannel().sendTyping().queue();
                                Thread.sleep(5000);
                                event.getChannel().sendMessage("Avalon, don't call things kittens.").queue();
                            }
                            catch(InterruptedException ex) {
                                Loggers.APPLICATION_LOG.error("Got interrupted trying to scold Avalon. REEEEE");
                            }
                        }
                    }).run();
                }
            }
        }*/

    }

}
