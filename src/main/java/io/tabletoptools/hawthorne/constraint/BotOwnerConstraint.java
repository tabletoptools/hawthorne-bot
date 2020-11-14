/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 03.02.18 18:26
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
package io.tabletoptools.hawthorne.constraint;

import ch.hive.discord.bots.commands.ConstraintCheck;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BotOwnerConstraint extends ConstraintCheck {
    @Override
    public boolean check(MessageReceivedEvent event) {

        return "384579076075290634".equals(event.getAuthor().getId());
    }

    @Override
    public String errorMessage() {
        return "Not a bot owner.";
    }
}
