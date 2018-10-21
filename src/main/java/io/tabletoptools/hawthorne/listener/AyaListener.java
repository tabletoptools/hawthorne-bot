package io.tabletoptools.hawthorne.listener;

import io.tabletoptools.discord.modulizer.Modulizer;
import io.tabletoptools.hawthorne.model.DynamicAmount;
import io.tabletoptools.hawthorne.model.ListMessageInstance;
import io.tabletoptools.hawthorne.model.LookupItem;
import io.tabletoptools.hawthorne.services.HomebrewItemService;
import io.tabletoptools.hawthorne.util.SearchUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

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


