package io.tabletoptools.hawthorne.constraint;

import ch.hive.discord.bots.commands.ConstraintCheck;
import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PRConstraint extends ConstraintCheck {

    @Override
    public boolean check(MessageReceivedEvent event) {
        if(event.getMember().getRoles().contains(HawthorneBot.instance().getClient().getRoleById(308325007421865984L))) {
            return true;
        }
        return false;
    }

    @Override
    public String errorMessage() {
        return "Not a PR Member.";
    }
}
