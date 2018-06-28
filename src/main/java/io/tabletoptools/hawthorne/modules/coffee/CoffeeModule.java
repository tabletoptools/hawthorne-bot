package io.tabletoptools.hawthorne.modules.coffee;

import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.discord.modulizer.annotation.Command;

//Why is this a thing?
//TODO: Buy open source coffee machine
@Command("coffee")
public class CoffeeModule extends Module {

    @Override
    public void onLoad() {
        this.addCommandClass(CoffeeCommands.class);

    }

    @Override
    public void onUnload() {
        //TODO: Coffee module unload? I guess?
    }

    @Override
    public void onEnable() {
        //I should probably just delete this module for good.
    }

    @Override
    public void onDisable() {
        //Random comment.
    }
}
