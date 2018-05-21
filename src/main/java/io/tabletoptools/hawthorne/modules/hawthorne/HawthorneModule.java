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
package io.tabletoptools.hawthorne.modules.hawthorne;

import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.discord.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.modules.api.APIModule;
import io.tabletoptools.hawthorne.modules.formhooks.FormModule;
import io.tabletoptools.hawthorne.modules.logging.LoggingModule;

@Command("hawthorne")
public class HawthorneModule extends Module {

    @Override
    public void onLoad() {
        this.addCommandClass(HawthorneCommands.class);
        this.loadSubmodule(new FormModule());
        this.loadSubmodule(new LoggingModule(), false);
        this.loadSubmodule(new APIModule());

    }

    @Override
    public void onUnload() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

}
