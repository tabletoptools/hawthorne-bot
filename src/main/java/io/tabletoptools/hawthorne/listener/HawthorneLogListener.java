package io.tabletoptools.hawthorne.listener;

import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.model.Statistics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HawthorneLogListener extends ListenerAdapter {

    private final TextChannel jesseLogChannel = HawthorneBot.instance().getClient().getTextChannelById(419296624808951809L);

    private static Statistics statistics = new Statistics();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        statistics.bumpMessageCount();

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

    @Override
    public void onGenericEvent(GenericEvent event) {
        statistics.bumpEventCount();
    }

    @Override
    public void onUserTyping(UserTypingEvent event) {
        statistics.bumpTypingStartCount();
    }

    @Override
    public void onGenericUserPresence(GenericUserPresenceEvent event) {
        statistics.bumpPresenceUpdateCount();
    }

    public static Statistics getStatistics() {
        return statistics;
    }

    public static void resetStatistics() {
        statistics = new Statistics();
    }
}
