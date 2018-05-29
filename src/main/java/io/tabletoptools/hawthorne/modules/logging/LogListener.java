package io.tabletoptools.hawthorne.modules.logging;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

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
