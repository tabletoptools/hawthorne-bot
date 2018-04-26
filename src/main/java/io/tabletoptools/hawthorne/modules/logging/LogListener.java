/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 14.04.18 00:49
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
package io.tabletoptools.hawthorne.modules.logging;

import com.google.cloud.bigquery.*;
import net.dv8tion.jda.client.events.call.CallCreateEvent;
import net.dv8tion.jda.client.events.call.CallDeleteEvent;
import net.dv8tion.jda.client.events.call.GenericCallEvent;
import net.dv8tion.jda.client.events.call.update.CallUpdateRegionEvent;
import net.dv8tion.jda.client.events.call.update.CallUpdateRingingUsersEvent;
import net.dv8tion.jda.client.events.call.update.GenericCallUpdateEvent;
import net.dv8tion.jda.client.events.call.voice.*;
import net.dv8tion.jda.client.events.group.*;
import net.dv8tion.jda.client.events.group.update.GenericGroupUpdateEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateIconEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateNameEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateOwnerEvent;
import net.dv8tion.jda.client.events.message.group.*;
import net.dv8tion.jda.client.events.message.group.react.GenericGroupMessageReactionEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionAddEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveAllEvent;
import net.dv8tion.jda.client.events.message.group.react.GroupMessageReactionRemoveEvent;
import net.dv8tion.jda.client.events.relationship.*;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.category.GenericCategoryEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.category.update.GenericCategoryUpdateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.*;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.*;
import net.dv8tion.jda.core.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.emote.GenericEmoteEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.core.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.core.events.emote.update.GenericEmoteUpdateEvent;
import net.dv8tion.jda.core.events.guild.*;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.events.guild.update.*;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.events.http.HttpRequestEvent;
import net.dv8tion.jda.core.events.message.*;
import net.dv8tion.jda.core.events.message.guild.*;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.*;
import net.dv8tion.jda.core.events.message.priv.react.GenericPrivateMessageReactionEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.*;
import net.dv8tion.jda.core.events.self.*;
import net.dv8tion.jda.core.events.user.*;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

public class LogListener extends ListenerAdapter {

    private static final BigQuery BIG_QUERY = BigQueryOptions.getDefaultInstance().getService();

    @Override
    public void onReady(ReadyEvent event) {
        Loggers.APPLICATION_LOG.info("Discord Bot loaded.");
    }

    @Override
    public void onResume(ResumedEvent event) {
        Loggers.APPLICATION_LOG.info("Discord bot resumed. All Objects are still in place and events are replayed.");
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        Loggers.APPLICATION_LOG.info("Discord bot reconnected. All Objects have been replaced when this is fired and events were likely missed in the downtime.");
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        if (event.isClosedByServer()) Loggers.APPLICATION_LOG.warn("Discord bot disconnected by Server: Code {}: {}", event.getCloseCode().getCode(), event.getCloseCode().getMeaning());
        else Loggers.APPLICATION_LOG.warn("Discord bot disconnected: Code {}: {}", event.getCloseCode().getCode(), event.getCloseCode().getMeaning());
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        Loggers.APPLICATION_LOG.info("Bot shutting down.");
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        Loggers.APPLICATION_LOG.info("Changing Bot Status from <{}> to <{}>.", event.getOldStatus(), event.getStatus());
    }

    @Override
    public void onException(ExceptionEvent event) {
        Loggers.APPLICATION_LOG.warn("Exception: ", event.getCause());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        Map<String, String> content = new HashMap<>();
        content.put("messageId", event.getMessageId());
        content.put("message", event.getMessage().getContentRaw());
        content.put("authorId", event.getAuthor().getId());
        content.put("channelId", event.getMessage().getChannel().getId());
        //content.put("attachments", event.getMessage().getAttachments().stream().map(Message.Attachment::getProxyUrl).reduce("", (a, b) -> a + "," + b));

        InsertAllRequest.RowToInsert rowToInsert = InsertAllRequest.RowToInsert.of(content);


    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    }

}
