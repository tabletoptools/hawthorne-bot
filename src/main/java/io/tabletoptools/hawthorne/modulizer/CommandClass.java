/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 11.04.18 20:36
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
package io.tabletoptools.hawthorne.modulizer;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandClass {

    private MessageReceivedEvent event;
    private Module module;

    protected MessageReceivedEvent getEvent() {
        return this.event;
    }

    public void setEvent(MessageReceivedEvent event) {
        this.event = event;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
