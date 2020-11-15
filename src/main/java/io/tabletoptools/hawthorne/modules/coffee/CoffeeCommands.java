/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 13.04.18 15:17
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
package io.tabletoptools.hawthorne.modules.coffee;

import io.tabletoptools.hawthorne.modulizer.CommandClass;
import io.tabletoptools.hawthorne.modulizer.annotation.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CoffeeCommands extends CommandClass {

    @Command("brew")
    public void brew() {
        MessageReceivedEvent event = this.getEvent();
        if(event.getAuthor().getIdLong() != 135468266695950336L) return;
        event.getChannel().sendMessage("This function is in a permanent state of WIP due to the fact that I do not have an open source coffee machine available at this time.").queue();
    }

}
