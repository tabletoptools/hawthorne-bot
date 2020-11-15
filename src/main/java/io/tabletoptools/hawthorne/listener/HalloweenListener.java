package io.tabletoptools.hawthorne.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HalloweenListener extends ListenerAdapter {
    private static String STORIES_URL =
            "https://gist.githubusercontent.com/IthayaL/76321897e64f3dc0092a8a7ffbe752cc/raw/530814404dc094ed7d4f3e63270cd37b3a9c9274/Halloween";
    private static String HALLOWEEN_MSG_TAG = "/trickortreat";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals(HALLOWEEN_MSG_TAG)){
            try {
                // url pointing to the collection of stories where each story must be on a single line
                URL halloweenUrl = new URL(STORIES_URL);
                // open an input stream to read the stories
                BufferedReader in = new BufferedReader(new InputStreamReader(halloweenUrl.openStream()));
                List<String> stories = new ArrayList<>();
                String inputLine;

                // read the stories from the input stream
                while ((inputLine = in.readLine()) != null) {
                    // but skip blank lines
                    if (inputLine.length() != 0) {
                        stories.add(inputLine);
                    }
                }

                in.close(); // close the stream

                if (stories.size() != 0) { // there is at least one story
                    // choose a random element from the stories list
                    int storyIndex = ThreadLocalRandom.current().nextInt(0, stories.size());
                    String story = stories.get(storyIndex);

                    // send back the result
                    event.getTextChannel().sendMessage(story).queue();
                } else {
                    event.getTextChannel().sendMessage("Too bad, no treats for you!");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                event.getTextChannel().sendMessage("No story: " + e.getMessage()).queue();
            } catch (IOException e) {
                e.printStackTrace();
                event.getTextChannel().sendMessage("No story: I/O error").queue();
            }
        }
    }
}
