package io.tabletoptools.hawthorne.listener;

import io.tabletoptools.hawthorne.model.DynamicAmount;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class WeatherListener extends ListenerAdapter {
    public WeatherListener() {
        super();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals("/weather")){
            String[] weatherTypes={"Sunny", "Rainy", "Cloudy", "Snowy"};
            try {
                DynamicAmount amount=DynamicAmount.withQuery("1d4");
                event.getTextChannel().sendMessage(weatherTypes[Math.toIntExact(amount.getAmount())-1]).queue();

            } catch (Exception e) {
                e.printStackTrace();


            }


        }


    }
}
