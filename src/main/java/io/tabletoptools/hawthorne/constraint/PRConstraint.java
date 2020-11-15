package io.tabletoptools.hawthorne.constraint;

import io.tabletoptools.hawthorne.commands.ConstraintCheck;
import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PRConstraint extends ConstraintCheck {

    @Override
    public boolean check(MessageReceivedEvent event) {
        return event.getMember().getRoles().contains(HawthorneBot.instance().getClient().getRoleById(308325007421865984L));
    }

    @Override
    public String errorMessage() {
        return "Not a PR Member.";
    }
}
