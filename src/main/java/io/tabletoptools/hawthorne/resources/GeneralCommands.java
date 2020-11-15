package io.tabletoptools.hawthorne.resources;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Arrays;
import io.tabletoptools.hawthorne.commands.Command;
import io.tabletoptools.hawthorne.commands.Constraint;
import io.tabletoptools.hawthorne.commands.Description;
import io.tabletoptools.hawthorne.commands.Parameter;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.constraint.*;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.jexl3.*;

import java.util.ArrayList;
import java.util.List;

public class GeneralCommands {

    @Command("echo")
    @Description("Echo something")
    @Constraint(BotOwnerConstraint.class)
    public static void echo(MessageReceivedEvent event, @Parameter("Arguments") String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg);
            builder.append(" ");
        }
        event.getChannel().sendMessage(builder).queue();
    }

    @Command("shutdown")
    @Description("Shutdown the bot.")
    @Constraint(BotOwnerConstraint.class)
    public static void shutdown(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Removing all pending loot rolls and shutting down...").queue();
        HawthorneBot.instance().shutdown();
    }

    @Command("restart")
    @Description("Shutdown the bot.")
    @Constraint(BotOwnerConstraint.class)
    public static void restart(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Removing all pending loot rolls and restarting...").queue();
        HawthorneBot.instance().restart();
    }

    @Command("eval")
    @Description("Evaluate an expression")
    @Constraint(BotOwnerConstraint.class)
    public static void eval(MessageReceivedEvent event, @Parameter("Expression") String... args) {

        event.getMessage().delete().queue();
        JexlEngine jexl = new JexlBuilder().create();

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg)
                    .append(" ");
        }
        String expr = builder.toString().trim();
        JexlExpression expression = jexl.createExpression(expr);

        JexlContext jexlContext = new MapContext();
        jexlContext.set("event", event);

        String result = String.valueOf(expression.evaluate(jexlContext));

        if(result != null) event.getChannel().sendMessage(result).queue();
    }

    @Command("game")
    @Description("Game Ad Commands")
    @Constraint(BotOwnerConstraint.class)
    public static void game(MessageReceivedEvent event, @Parameter("args") String... args) {
        event.getTextChannel().sendMessage(
                "Subscribed you to level "
                + args[1]
                + " sessions. I'll notify you if anything interesting pops up. Enable auto-session-signup with `dev!autosignup true`")
                .queue();
    }

    @Command("saveStats")
    @Description("Save Bot Statistics")
    @Constraint(BotOwnerConstraint.class)
    public static void saveStats(MessageReceivedEvent event) {
        try {
            HawthorneBot.saveStatistics();
        }
        catch(Exception ex) {
            Loggers.APPLICATION_LOG.error("Error: ", ex);
            throw ex;
        }
    }

    @Command("adventurer")
    @Constraint(value = {
            HawthorneHeadOfStaffConstraint.class,
            HawthorneAdminConstraint.class,
            BotOwnerConstraint.class,
            TesterConstraint.class,
            PRConstraint.class
    }, enforceAll = false)
    public static void adventurer(MessageReceivedEvent event, @Parameter("args") String... args) {

        Member member = event.getMessage().getMentionedMembers().get(0);

        event.getMessage().delete().queue();
        event.getGuild().removeRoleFromMember(member, event.getGuild().getRoleById(445939304695595028L)).queue();
        event.getGuild().addRoleToMember(member, event.getGuild().getRoleById(343393950079385610L)).queue();
        event.getChannel().sendMessage("Made " + member.getEffectiveName() + " an adventurer.").queue();

    }

    @Command("dm")
    @Constraint(value = {
            HawthorneHeadOfStaffConstraint.class,
            HawthorneAdminConstraint.class,
            BotOwnerConstraint.class,
            TesterConstraint.class,
            PRConstraint.class
    }, enforceAll = false)
    public static void dm(MessageReceivedEvent event, @Parameter("args") String... args) {

        Member member = event.getMessage().getMentionedMembers().get(0);
        event.getMessage().delete().queue();

        List<Role> roles = new ArrayList<>();

        roles.add(event.getGuild().getRoleById(343394051938320388L));
        roles.add(event.getGuild().getRoleById(378954937297666052L));
        roles.add(event.getGuild().getRoleById(418756942941650955L));
        roles.forEach(role -> event.getGuild().addRoleToMember(member, role).queue());
        event.getChannel().sendMessage("Made " + member.getEffectiveName() + " a trial DM.").queue();
    }
    @Command("ctd")
    public static void commonToDraconic(MessageReceivedEvent event, @Parameter("text") String... text) {

        event.getMessage().delete().queue();

        String textString = Arrays.stream(text).reduce("", ((s, s2) -> s + " " + s2));
        String mode = "ctd";

        event.getTextChannel().sendMessage(translate(textString, mode)).queue();

    }

    @Command("dtc")
    public static void draconicToCommon(MessageReceivedEvent event, @Parameter("text") String... text) {

        event.getMessage().delete().queue();

        String textString = Arrays.stream(text).reduce("", ((s, s2) -> s + " " + s2));
        String mode = "dtc";

        event.getTextChannel().sendMessage(translate(textString, mode)).queue();

    }

    private static String translate(String textString, String mode) {

        try {
            String html = Unirest.post("http://draconic.twilightrealm.com/")
                    .field("action", mode)
                    .field("text", textString)
                    .field("translate", "Translate")
                    .asString().getBody();

            Document htmlDoc = Jsoup.parse(html);
            String result = htmlDoc.getElementsByTag("textarea")
                    .get("ctd".equals(mode) ? 1 : 0).ownText();
            return result;
        } catch (UnirestException e) {
            e.printStackTrace();
            return "";
        }
    }
}
