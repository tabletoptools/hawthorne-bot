/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 05.02.18 11:36
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
import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class HawthorneRulesConstraint extends ConstraintCheck {

    @Override
    public boolean check(MessageReceivedEvent event) {
        return event.getMember().getRoles().contains(HawthorneBot.instance().getClient().getRoleById(308325009506697227L));
    }

    @Override
    public String errorMessage() {
        return "Not in the rules team.";
    }
}
