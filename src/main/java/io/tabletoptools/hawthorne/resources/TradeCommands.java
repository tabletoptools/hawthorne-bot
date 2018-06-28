package io.tabletoptools.hawthorne.resources;

import ch.hive.discord.bots.commands.Command;
import ch.hive.discord.bots.commands.Parameter;
import io.tabletoptools.hawthorne.HawthorneBot;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TradeCommands {

    Channel DOWNTIME_LOG = HawthorneBot.instance().getClient().getTextChannelById(343396649797812224L);

    @Command("quantity")
    public static void getQuantity(MessageReceivedEvent event) {

    }

    @Command("buy")
    public static void buy(MessageReceivedEvent event, @Parameter("character") String character, @Parameter("item") String item, @Parameter("amount") Integer amount) {



    }

}
