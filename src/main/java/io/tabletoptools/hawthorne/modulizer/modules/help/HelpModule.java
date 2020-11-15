package io.tabletoptools.hawthorne.modulizer.modules.help;

import io.tabletoptools.hawthorne.modulizer.Module;
import io.tabletoptools.hawthorne.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.modulizer.annotation.Locked;

@Locked()
@Command("help")
public class HelpModule extends Module {

    @Override
    public void onLoad() {
        this.addCommandClass(HelpCommands.class);
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
