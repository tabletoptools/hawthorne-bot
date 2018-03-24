/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 28.02.18 09:59
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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Date;

public class GuildMemberListener extends ListenerAdapter {

    private final TextChannel logChannel = HawthorneBot.instance().getClient().getTextChannelById(418334116640063488L);

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(isHawthorneGuild(event)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setAuthor("Hawthorne Bot", null, "https://cdn.discordapp.com/attachments/405639224084398090/418334558727962644/token_11.png")
                    .setTitle(event.getMember().getEffectiveName() + " joined the guild!")
                    .build();

            logChannel.sendMessage(embed).queue();
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (isHawthorneGuild(event)) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor("Hawthorne Bot", null, "https://cdn.discordapp.com/attachments/405639224084398090/418334558727962644/token_11.png")
                    .setTitle(event.getMember().getEffectiveName() + " left the guild!");


            if(event.getMember().getRoles().size() > 0) {

                StringBuilder rb = new StringBuilder();

                event.getMember().getRoles().forEach(role -> {
                    rb.append(role.getName())
                            .append("\n");
                });
                eb.addField("Removed Roles", rb.toString(), false);
            }

            logChannel.sendMessage(eb.build()).queue();
        }
    }

    private boolean isHawthorneGuild(GenericGuildMemberEvent event) {
        return event.getGuild().getIdLong() == 308324031478890497L;
    }
}
