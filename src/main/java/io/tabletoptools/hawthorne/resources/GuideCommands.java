/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 05.02.18 15:16
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
import ch.hive.discord.bots.commands.Description;
import ch.hive.discord.bots.commands.Parameter;
import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GuideCommands {

    final static MessageEmbed DTP_GUIDE = new EmbedBuilder()
            .setTitle("DTP and you!")
            .setColor(HawthorneBot.instance().HAWTHORNE_PURPLE)
            .setDescription("DownTime Points, or known commonly as DTP is used to do many things in your downtime while as a member of Hawthorne Guild. " +
                    "To earn DTP you must play in sessions or, if you are a DM run sessions. " +
                    "You earn 5 DTP for every hour of game time or 2 DTP if you go over an hour. " +
                    "Example, for a 3 hour and 37 minute session you earn 17 DTP. " +
                    "With DTP you can 'spend' it in one of 3 ways: working to earn Gold, crafting items or travel. \n " +
                    "When working for earn Gold you gain 1d6 gold for every DTP spent. If you have proficiency in Artisan's Tools or an Instrument then you earn 1d6+your proficiency bonus for each DTP spent. If you have expertise in the tool or instrument then you gain 1d6+double your proficiency bonus! Two examples for spending 10 DTP while you have proficiency in an instrument and are level 4: /roll 10d6+20. /rr 10 d6+2. \n To craft an item you must first work out how much DTP is required to craft it, which is the cost of the item divided by 25. An example is Plate Armor which costs 1500gp so would require 60 DTP to craft. After you have worked this out you then half the cost and that is how much gold is needed to make it. Using Plate as again, the total cost to make it would be 750gp and 60DTP. \n Finally you can travel using DTP, and the most common place people travel to is Waterdeep. The reason one would travel there is because normally in Lerwick you can only purchase items up to 200gp whereas in Waterdeep you can purchase any item in the PHB no matter the cost. To travel there it costs a total of 30 DTP, 15 DTP to make it there then 15 DTP to make it back.")
            .build();

    @Command("guide")
    @Description("Get information on a specified topic.")
    public static void guide(MessageReceivedEvent event, @Parameter("Topic") String topic) {

        event.getMessage().delete().queue();
        if("dtp".equals(topic.toLowerCase())) {
            event.getChannel().sendMessage(DTP_GUIDE).queue();
        }


    }

}
