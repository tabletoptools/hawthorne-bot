/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 09.02.18 15:57
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
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.constraint.BotOwnerConstraint;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.jexl3.*;

import java.util.Arrays;

public class GeneralCommands {

    @Command("echo")
    @Description("Echo something")
    @Constraint(BotOwnerConstraint.class)
    public static void echo(MessageReceivedEvent event, @Parameter("Arguments") String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg);
            builder.append(" ");
        }
        event.getChannel().sendMessage(builder).queue();
    }

    @Command("shutdown")
    @Description("Shutdown the bot.")
    @Constraint(BotOwnerConstraint.class)
    public static void shutdown(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Removing all pending loot rolls and shutting down...").queue();
        HawthorneBot.instance().shutdown();
    }

    @Command("restart")
    @Description("Shutdown the bot.")
    @Constraint(BotOwnerConstraint.class)
    public static void restart(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Removing all pending loot rolls and restarting...").queue();
        HawthorneBot.instance().restart();
    }

    @Command("eval")
    @Description("Evaluate an expression")
    @Constraint(BotOwnerConstraint.class)
    public static void eval(MessageReceivedEvent event, @Parameter("Expression") String... args) {
        event.getMessage().delete().queue();
        JexlEngine jexl = new JexlBuilder().create();

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg)
                    .append(" ");
        }
        String expr = builder.toString().trim();
        JexlExpression expression = jexl.createExpression(expr);

        JexlContext jexlContext = new MapContext();
        jexlContext.set("event", event);

        String result = String.valueOf(expression.evaluate(jexlContext));

        if(result != null) event.getChannel().sendMessage(result).queue();
    }

    @Command("game")
    @Description("Game Ad Commands")
    @Constraint(BotOwnerConstraint.class)
    public static void game(MessageReceivedEvent event, @Parameter("args") String... args) {
        event.getTextChannel().sendMessage(
                "Subscribed you to level "
                + args[1]
                + " sessions. I'll notify you if anything interesting pops up. Enable auto-session-signup with `dev!autosignup true`")
                .queue();
    }
}
