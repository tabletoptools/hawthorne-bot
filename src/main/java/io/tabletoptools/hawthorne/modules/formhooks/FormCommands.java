/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 15.04.18 00:16
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
package io.tabletoptools.hawthorne.modules.formhooks;

import io.tabletoptools.hawthorne.modulizer.CommandClass;
import io.tabletoptools.hawthorne.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.api.entities.Message;

public class FormCommands extends CommandClass {

    @Command("check")
    public void check() {
        Loggers.APPLICATION_LOG.info("Triggering Form Check Manually...");
        getEvent().getMessage().delete().queue();
        Message message = getEvent().getChannel().sendMessage("Checking for new Submissions...").complete();
        ((FormModule)this.getModule()).performCheck();
        message.editMessage("Checked for new Submissions.").queue();
    }

}
