package io.tabletoptools.hawthorne.model;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class LookupItem implements ChoosableEntity {

    private String name;
    private StringBuilder description = new StringBuilder();

    public void appendDescription(String content) {
        this.description
                .append(content)
                .append("\n");
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Message getMessage() {

        return new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle(this.getName())
                        .setDescription(this.description.toString())
                        .setFooter("Homebrew.", null)
                        .build()
                ).build();
    }
}
