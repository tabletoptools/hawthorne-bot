package io.tabletoptools.hawthorne.model;

import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ListMessageInstance<E extends ChoosableEntity> {

    private static List<ListMessageInstance> instances = new ArrayList<>();
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private Integer pageIndex = 0;
    private Integer entitiesPerPage = 8;
    private List<E> entities;
    private Long channelId;
    private Long messageId;
    private Long requesterId;
    private ScheduledFuture future;

    private static void addInstance(ListMessageInstance instance) {
        instances.add(instance);
    }

    public static boolean hasInstance(Long channelId, Long requesterId) {
        return instances.stream().anyMatch(instance -> instance.getChannelId().equals(channelId) && instance.getRequesterId().equals(requesterId));
    }

    public static List<ListMessageInstance> getInstances(Long channelId, Long requesterId) {
        return instances.stream().filter(instance ->
                instance.getChannelId().equals(channelId) && instance.getRequesterId().equals(requesterId)).collect(Collectors.toList());
    }

    public ListMessageInstance(List<E> entities) {
        this.entities = entities;
        this.entities.sort(Comparator.comparing(E::getName));
    }

    public void initialise(Long channelId, Long messageId, Long requesterId) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.requesterId = requesterId;
        this.future = executorService.schedule(this::delete, 10, TimeUnit.SECONDS);
        addInstance(this);
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    private Integer getPageCount() {
        return (int)(Math.ceil(entities.size() / entitiesPerPage));
    }

    private List<E> getCurrentEntities() {

        ArrayList<E> currentEntities = new ArrayList<>();

        int startingEntity = pageIndex * entitiesPerPage;
        int currentEntityCount = entities.size() - startingEntity;

        if (currentEntityCount > entitiesPerPage) {
            currentEntityCount = entitiesPerPage;
        }

        for (int index = startingEntity; index < startingEntity + currentEntityCount; index++) {
            currentEntities.add(entities.get(index));
        }

        return currentEntities;

    }

    public Message getMessage() {

        List<E> currentEntities = getCurrentEntities();

        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Multiple Matches Found");


        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Which one were you looking for? (Type the number or 'c' to cancel)\n");

        if (this.getPageCount() > 1) {
            stringBuilder.append("'n' to go to the next page, or 'p' for previous\n");
            embedBuilder.setFooter("Page " + (pageIndex+1) + " of " + (getPageCount()+1), null);
        }

        for (int index = 0; index < currentEntities.size(); index++) {

            stringBuilder
                    .append("**[")
                    .append(index + 1)
                    .append("]** - ")
                    .append(currentEntities.get(index).getName())
                    .append("\n");

        }

        embedBuilder.setDescription(stringBuilder.toString());

        messageBuilder.setEmbed(embedBuilder.build());

        return messageBuilder.build();
    }

    public Message pageUp() {
        if (pageIndex < Math.ceil(
                Double.parseDouble(String.valueOf(this.entities.size())) /
                Double.parseDouble(String.valueOf(this.entitiesPerPage)))
        ) pageIndex++;

        return this.getMessage();

    }

    public Message pageDown() {
        if (pageIndex > 0) pageIndex--;

        return this.getMessage();

    }

    public Message choose(int itemIndex) {
        future.cancel(true);
        this.delete();
        return this.getCurrentEntities().get(itemIndex - 1).getMessage();
    }

    public void cancel() {
        future.cancel(true);
        this.delete();
    }

    private void delete() {
        Loggers.APPLICATION_LOG.info("Removing <{}> from channel <{}>.", messageId, channelId);
        HawthorneBot
                .instance()
                .getClient()
                .getTextChannelById(channelId)
                .getMessageById(messageId)
                .complete()
                .delete()
                .queue();
    }

    public void command(MessageReceivedEvent event, String command) {
        if ("c".equals(command)) {
            this.cancel();
            event.getMessage().delete().queue();
        } else if ("n".equals(command)) {
            event.getChannel().getMessageById(this.messageId).complete().editMessage(this.pageUp()).queue();
            event.getMessage().delete().queue();
        } else if ("p".equals(command)) {
            event.getChannel().getMessageById(this.messageId).complete().editMessage(this.pageDown()).queue();
            event.getMessage().delete().queue();
        }
    }
}
