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
import com.yubico.client.v2.VerificationResponse;
import io.tabletoptools.discord.modulizer.Modulizer;
import io.tabletoptools.hawthorne.OTPValidator;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Date;

public class MessageListener extends ListenerAdapter {

    private static Date lastAvalonKitten = new Date(new Date().getTime() - (1000L * 300L));

    private boolean order66init = false;
    private boolean alpha = false;
    private boolean beta = false;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        CommandBase.instance().process(event);
        if(event.getAuthor().getIdLong() == 135468266695950336L) {
            Modulizer.instance().process(event);
        }
        /*if("execute order 66".equals(event.getMessage().getContentRaw())) {
            if(event.getAuthor().getIdLong() == 135468266695950336L && !order66init) {
                event.getChannel().sendMessage("Confirmed Authenticity of user " + event.getAuthor().getName() + ".").queue();
                event.getChannel().sendMessage("Enter authorization code Alpha.").queue();
                order66init = true;
            }
            else {
                event.getChannel().sendMessage("Could not authenticate user " + event.getAuthor().getName() + ".").queue();
            }
        }
        else if(order66init && event.getAuthor().getIdLong() == 135468266695950336L) {
            if(!alpha) {
                if("cancel".equals(event.getMessage().getContentRaw())) {
                    alpha = false;
                    order66init = false;
                    event.getChannel().sendMessage("Cancelled operation.").queue();
                }
                else {
                    try {
                        VerificationResponse response = OTPValidator.instance().verify(event.getMessage().getContentRaw());
                        if (response.isOk() && response.getPublicId().equals("ccccccflkcdg")) {
                            alpha = true;
                            event.getChannel().sendMessage("Verified authorization code Alpha. Enter authorization code Beta to confirm and reset Server Object.").queue();
                        }
                    } catch (Exception ex) {
                        beta = false;
                        alpha = false;
                        order66init = false;
                        event.getChannel().sendMessage("Failed to verify authorization code Alpha. Resetting parameters.").queue();
                    }
                }
            }
            else if (!beta) {
                if("cancel".equals(event.getMessage().getContentRaw())) {
                    alpha = false;
                    order66init = false;
                    event.getChannel().sendMessage("Cancelled operation.").queue();
                }
                else {
                    try {
                        VerificationResponse response = OTPValidator.instance().verify(event.getMessage().getContentRaw());
                        if (response.isOk() && response.getPublicId().equals("ccccccflkcdg")) {
                            beta = true;
                            event.getChannel().sendMessage("Verified authorization code Beta. Starting countdown.").queue();
                        }
                    } catch (Exception ex) {
                        beta = false;
                        alpha = false;
                        order66init = false;
                        event.getChannel().sendMessage("Failed to verify authorization code Beta. Resetting parameters.").queue();
                    }
                }
            }
            else {
                order66init = false;
            }
        }/*
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
