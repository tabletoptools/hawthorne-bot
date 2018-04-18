/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 14.04.18 16:19
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
package io.tabletoptools.hawthorne.modules.formhooks;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.tabletoptools.discord.modulizer.Module;
import io.tabletoptools.discord.modulizer.annotation.Command;
import io.tabletoptools.hawthorne.HawthorneBot;
import io.tabletoptools.hawthorne.modules.logging.Loggers;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command("forms")
public class FormModule extends Module {

    private static final String ADVENTURER_REGISTRATION_FORM_ID = "DpCAsU";
    private static final String DM_APPLICATION_FORM_ID = "tKWAOD";
    private static final String INCIDENT_REPORT_FORM_ID = "fjNhAc";
    private static final String COUNCIL_APPLICATION_FORM_ID = "PayNbA";

    private static final String REGISTRATION_FIELD_ID_NAME = "P8UTXkCRdrir";
    private static final String REGISTRATION_FIELD_ID_USERNAME = "EXTgVo9eRp5R";
    private static final String REGISTRATION_FIELD_ID_EMAIL = "uCI3TJGsy9iC";
    private static final String REGISTRATION_FIELD_ID_TOWN = "A7496ehMTpBg";
    private static final String REGISTRATION_FIELD_ID_RULE = "Nz5DIzUsV9GQ";

    private static final String INCIDENT_REPORT_FIELD_ID_USERNAME = "mdrfCyhTj3If";
    private static final String INCIDENT_REPORT_FIELD_ID_SEVERITY = "oP0SZroKDUqA";

    private static final String APPLICATION_FIELD_ID_USERNAME = "uLIADukkwQGo";

    private static final String COUNCIL_APPLICATION_FIELD_ID_USERNAME = "tB3D404EdqMo";
    private static final String COUNCIL_APPLICATION_FIELD_ID_AVAILABILITY = "h1AX9hs0rYP3";
    private static final String COUNCIL_APPLICATION_FIELD_ID_ONLY_COUNCIL_CHARACTER = "nWCDj7yf7SeQ";
    private static final String COUNCIL_APPLICATION_FIELD_ID_CHARACTER_NAME = "ioKXeYK7scaH";
    private static final String COUNCIL_APPLICATION_FIELD_ID_WHY_JOIN = "hoGbCbL48WcQ";
    private static final String COUNCIL_APPLICATION_FIELD_ID_WORK_SO_FAR = "rdTki46BTebg";
    private static final String COUNCIL_APPLICATION_FIELD_ID_FUTURE_VISION = "Zh0y2AIaaEjh";

    private Date lastCheck = new Date();
    private ScheduledExecutorService scheduledExecutorService;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            performCheck();
        }
    };

    void performCheck() {
        try {
            Loggers.APPLICATION_LOG.info("Running check for new form submissions.");
            Date checkDate = lastCheck;
            lastCheck = new Date();

            JSONObject responses = getFormResponsesSinceDate(ADVENTURER_REGISTRATION_FORM_ID, checkDate).getBody().getObject();
            if (responses.getInt("total_items") > 0) {
                Loggers.APPLICATION_LOG.info("Got new Registrations: <{}>", responses.getInt("total_items"));
                responses.getJSONArray("items").forEach(item -> {
                    JSONObject response = ((JSONObject) item);
                    JSONArray answers = response.getJSONArray("answers");
                    List<Object> answerList = answers.toList();

                    String name = getAnswerString(answerList, REGISTRATION_FIELD_ID_NAME, "text");
                    String username = getAnswerString(answerList, REGISTRATION_FIELD_ID_USERNAME, "text");
                    String email = getAnswerString(answerList, REGISTRATION_FIELD_ID_EMAIL, "email");
                    String townName = getAnswerString(answerList, REGISTRATION_FIELD_ID_TOWN, "text");
                    String ruleQuestion = getAnswerString(answerList, REGISTRATION_FIELD_ID_RULE, "text");
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New Adventurer Registration from " + name)
                            .setAuthor(username)
                            .addField("Email", email, false)
                            .addField("Town Name", townName, true)
                            .addField("Rule Question", ruleQuestion, true)
                            .setDescription("Check the Sheet for more information.")
                            .setColor(new Color(73, 98, 62))
                            .setThumbnail("https://cdn1.iconfinder.com/data/icons/ordinary-people/512/adventurer-512.png")
                            .build();
                    HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage(embed).queue();

                });
            }

            responses = getFormResponsesSinceDate(DM_APPLICATION_FORM_ID, checkDate).getBody().getObject();
            if (responses.getInt("total_items") > 0) {
                Loggers.APPLICATION_LOG.info("Got new Applications: <{}>", responses.getInt("total_items"));
                responses.getJSONArray("items").forEach(item -> {
                    JSONObject response = ((JSONObject) item);
                    JSONArray answers = response.getJSONArray("answers");
                    List<Object> answerList = answers.toList();

                    String username = getAnswerString(answerList, APPLICATION_FIELD_ID_USERNAME, "text");
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New DM Application.")
                            .setAuthor(username)
                            .setDescription("Check the Sheet for more information.")
                            .setColor(new Color(254, 122, 65))
                            .setThumbnail("https://res.cloudinary.com/teepublic/image/private/s--nXMgcagO--/t_Preview/b_rgb%3A191919%2Cc_limit%2Cf_auto%2Ch_313%2Cq_90%2Cw_313/v1515831469/production/designs/2276218_0")
                            .build();
                    HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage(embed).queue();

                });
            }

            responses = getFormResponsesSinceDate(INCIDENT_REPORT_FORM_ID, checkDate).getBody().getObject();
            if (responses.getInt("total_items") > 0) {
                Loggers.APPLICATION_LOG.info("Got new Incident Reports: <{}>", responses.getInt("total_items"));
                responses.getJSONArray("items").forEach(item -> {
                    JSONObject response = ((JSONObject) item);
                    JSONArray answers = response.getJSONArray("answers");
                    List<Object> answerList = answers.toList();

                    String username = getAnswerString(answerList, INCIDENT_REPORT_FIELD_ID_USERNAME, "text");
                    Integer severity = getAnswerInteger(answerList, INCIDENT_REPORT_FIELD_ID_SEVERITY, "number");
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New severity " + severity + " incident report.")
                            .setAuthor(username)
                            .setDescription("Check the Sheet for more information.")
                            .setColor(new Color(239, 93, 67))
                            .setThumbnail("https://cdn4.iconfinder.com/data/icons/alphabet-3/500/Alert_exclamation_exclamation_mark_mark-512.png")
                            .build();
                    HawthorneBot.instance().getClient().getTextChannelById(417398439526137856L).sendMessage(embed).queue();

                });
            }

            responses = getFormResponsesSinceDate(COUNCIL_APPLICATION_FORM_ID, checkDate).getBody().getObject();
            if (responses.getInt("total_items") > 0) {
                Loggers.APPLICATION_LOG.info("Got new Council Applications: <{}>", responses.getInt("total_items"));
                responses.getJSONArray("items").forEach(item -> {
                    JSONObject response = ((JSONObject) item);
                    JSONArray answers = response.getJSONArray("answers");
                    List<Object> answerList = answers.toList();

                    String username = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_USERNAME, "text");
                    String charactername = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_CHARACTER_NAME, "text");
                    Boolean isOnlyCharacter = getAnswerBoolean(answerList, COUNCIL_APPLICATION_FIELD_ID_ONLY_COUNCIL_CHARACTER, "boolean");
                    String availability = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_AVAILABILITY, "text");
                    String whyJoin = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_WHY_JOIN, "text");
                    String workSoFar = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_WORK_SO_FAR, "text");
                    String futureVision = getAnswerString(answerList, COUNCIL_APPLICATION_FIELD_ID_FUTURE_VISION, "text");


                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("New council application.")
                            .setAuthor(charactername)
                            .setFooter("Application from " + username + ".", null)
                            .addField("Is this your only character on the council?", isOnlyCharacter ? "Yes" : "No", true)
                            .addField("Availability", availability, false)
                            .addField("Why do you want to be a council member?", whyJoin, false)
                            .addField("What have you done for Lerwick and the Guild so far?", workSoFar, false)
                            .addField("What is your vision for Lerwick and the Hawthorne Guild’s future?", futureVision, false)
                            .build();
                    TextChannel channel = HawthorneBot.instance().getClient().getTextChannelById(386486269053763584L);
                    Message message = new MessageBuilder()
                            .append(HawthorneBot.instance().getClient().getGuildById(308324031478890497L).getRoleById(308325470930206720L).getAsMention())
                            .build();
                    channel.sendMessage(message).queue();
                    channel.sendMessage(embed).queue();

                });
            }

        } catch (UnirestException ex) {
            Loggers.APPLICATION_LOG.warn("Unirest Exception: ", ex);
        }
    }

    private String getAnswerString(List<Object> answerList, String fieldId, String type) {
        Optional<Object> objectOptional = answerList
                .stream()
                .filter(a -> (fieldId.equals(((HashMap) ((HashMap) a).get("field")).get("id"))))
                .findFirst();
        return objectOptional.isPresent()
                ? ((HashMap) objectOptional.get()).get(type) != null
                ? ((String) ((HashMap) objectOptional.get()).get(type))
                : ""
                : "";
    }

    private Integer getAnswerInteger(List<Object> answerList, String fieldId, String type) {
        Optional<Object> objectOptional = answerList
                .stream()
                .filter(a -> (fieldId.equals(((HashMap) ((HashMap) a).get("field")).get("id"))))
                .findFirst();
        return objectOptional.filter(o -> ((HashMap) o).get(type) != null).map(o -> ((Integer) ((HashMap) o).get(type))).orElse(-1);
    }

    private Boolean getAnswerBoolean(List<Object> answerList, String fieldId, String type) {
        Optional<Object> objectOptional = answerList
                .stream()
                .filter(a -> (fieldId.equals(((HashMap) ((HashMap) a).get("field")).get("id"))))
                .findFirst();
        return objectOptional.filter(o -> ((HashMap) o).get(type) != null).map(o -> ((Boolean) ((HashMap) o).get(type))).orElse(false);
    }

    private HttpResponse<JsonNode> getFormResponsesSinceDate(String formId, Date fromDate) throws UnirestException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return Unirest.get("https://api.typeform.com/forms/" + formId + "/responses")
                .header("Authorization", "Bearer " + HawthorneBot.instance().getTypeformToken())
                .queryString("since", dateFormat.format(fromDate))
                .asJson();
    }


    @Override
    public void onLoad() {
        this.addCommandClass(FormCommands.class);
    }

    @Override
    public void onUnload() {
        if (!scheduledExecutorService.isShutdown() && !scheduledExecutorService.isTerminated()) scheduledExecutorService.shutdown();
    }

    @Override
    public void onEnable() {
        if (!isEnabled()) {
            //Restart Scheduler
            if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(runnable, 5, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public void onDisable() {
        if (isEnabled()) {
            if (!scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdown();
        }
    }
}
