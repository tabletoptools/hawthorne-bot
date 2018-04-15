/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 14.04.18 00:44
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
package io.tabletoptools.hawthorne.modules.logging;

import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.hawthorne.HawthorneBot;

public class LoggingModule extends Module {

    private LogListener logListener = new LogListener();

    @Override
    public void onLoad() {
    }

    @Override
    public void onUnload() {
        if(HawthorneBot.instance().getClient().getRegisteredListeners().contains(logListener)) {
            HawthorneBot.instance().getClient().removeEventListener(logListener);
        }
    }

    @Override
    public void onEnable() {
        if(!HawthorneBot.instance().getClient().getRegisteredListeners().contains(logListener)) {
            HawthorneBot.instance().getClient().addEventListener(logListener);
        }
    }

    @Override
    public void onDisable() {
        if(HawthorneBot.instance().getClient().getRegisteredListeners().contains(logListener)) {
            HawthorneBot.instance().getClient().removeEventListener(logListener);
        }
    }
}
