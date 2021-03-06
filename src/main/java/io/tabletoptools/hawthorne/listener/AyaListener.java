package io.tabletoptools.hawthorne.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AyaListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getMessage().getContentRaw().toLowerCase().contains("aya")){
            User aya = event.getJDA().getUserById(203901518846492672L);
            aya.openPrivateChannel()
                    .complete()
                    .sendMessage("You have been summoned in "+event.getTextChannel().getName())
                    .queue();
        }
    }
}


