/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 03.02.18 00:37
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
package io.tabletoptools.hawthorne.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.*;
import io.tabletoptools.hawthorne.modules.logging.Loggers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ItemService {

    private static final String APPLICATION_NAME = "Hawthorne Loot Generator";
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

    private List<Item> items = new ArrayList<>();
    private Map<Integer, Set<Tier>> tiersPerLevel = new HashMap<>();
    private Boolean isAuthenticated = false;
    private GoogleAuthorizationCodeFlow flow;

    private GoogleAuthorizationCodeFlow getFlow() {
        if (this.flow == null) {
            try {
                this.flow = getGoogleAuthorizationCodeFlow();
            } catch (IOException ex) {
                Loggers.APPLICATION_LOG.error("ERROR getting Google Authorization Code Flow!");
                return null;
            }
        }
        return this.flow;
    }

    private static ItemService instance;

    public static ItemService instance() throws NotAuthenticatedException {
        if (instance == null) {
            instance = new ItemService();
            instance.load();
        }
        return instance;
    }

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private final String itemSheetId = "1E-8xhTVVwii4NR7hfYMAuF6PCzGrbbOfJ0VzeF4UGrE";

    public void load() throws NotAuthenticatedException {
        Loggers.APPLICATION_LOG.info("Loading Tiers...");
        loadTiers();
        Loggers.APPLICATION_LOG.info("Loading Categories...");
        loadCategories();
        Loggers.APPLICATION_LOG.info("Loading Items...");
        loadItems();
    }

    public void update() throws NotAuthenticatedException {
        this.load();
    }

    private void loadItems() throws NotAuthenticatedException {
        List<Item> items = new ArrayList<>();
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values().get(itemSheetId, "ItemData!A2:V").execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) {
                Loggers.APPLICATION_LOG.error("No data found for supplied table.");
            } else {
                values.forEach(row -> {
                    try {

                        if(row.get(1) != null && !"".equals(row.get(1))) {

                            String name = row.get(0).toString();

                            String tier = "T" + row.get(1).toString();

                            BigDecimal weight = new BigDecimal(row.get(2) == null ? "0" : row.get(2).toString());

                            String category = row.get(3).toString();

                            Loggers.APPLICATION_LOG.debug("Processing Item <{}>: <{}>", tier, name);

                            Map<Integer, Amount> amountPerLevel = new HashMap<>();
                            for (int i = 4; i < 22; i++) {
                                if (row.get(i) == null) {
                                    amountPerLevel.put(i - 1, new StaticAmount(1L));
                                } else {
                                    try {
                                        amountPerLevel.put(i - 1, new StaticAmount(Long.parseLong(String.valueOf(row.get(i)))));
                                    } catch (NumberFormatException ex) {
                                        try {
                                            amountPerLevel.put(i - 1, DynamicAmount.withQuery(String.valueOf(row.get(i))));
                                        } catch (Exception ex2) {
                                            Loggers.APPLICATION_LOG.warn("Error while getting amount for level and item. Setting default (1): ", ex2);
                                            amountPerLevel.put(i - 1, new StaticAmount(1L));
                                        }
                                    }
                                }
                            }

                            Item item = new Item(name, tier, weight, category, amountPerLevel);
                            items.add(item);

                        }
                    } catch (Exception ex) {
                        Loggers.APPLICATION_LOG.warn("Exception: ", ex);
                    }
                });

            }
        } catch (IOException ex) {
            Loggers.APPLICATION_LOG.warn("IOException: ", ex);
        }
        this.items = items;
    }

    private void loadTiers() throws NotAuthenticatedException {
        Map<Integer, Set<Tier>> tiersPerLevel = new HashMap<>();
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values().get(itemSheetId, "Tiers!A2:S").execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) Loggers.APPLICATION_LOG.error("No data found for supplied table.");
            else {
                values.forEach(row -> {
                    if (!isNullOrEmpty(row.get(0))) {
                        try {
                            for (int i = 3; i <= 20; i++) {
                                try {
                                    //Base Tier
                                    Tier tier = new Tier(row.get(0).toString())
                                            .withWeight(new BigDecimal(isNullOrEmpty(row.get(i - 2)) ? "0" : row.get(i - 2).toString()));
                                    if (!tiersPerLevel.containsKey(i)) {
                                        tiersPerLevel.put(i, new TreeSet<>());
                                    }
                                    tiersPerLevel.get(i).add(tier);
                                } catch (IllegalArgumentException ex) {
                                    Loggers.APPLICATION_LOG.debug("Skipping a row due to an Illegal Argument Exception.");
                                }
                            }
                        } catch (Exception ex) {
                            Loggers.APPLICATION_LOG.warn("Exception: ", ex);
                        }
                    } else Loggers.APPLICATION_LOG.info("Skipping a row, not a tier.");
                });
            }
        } catch (IOException ex) {
            Loggers.APPLICATION_LOG.warn("IOException", ex);
        }
        this.tiersPerLevel = tiersPerLevel;
    }

    private void loadCategories() throws NotAuthenticatedException {
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values().get(itemSheetId, "LevelBrackets!A2:T").execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) Loggers.APPLICATION_LOG.error("No data found for supplied table.");
            else {
                values.forEach(row -> {
                    if(!isNullOrEmpty(row.get(0))) {
                        try {
                            for (int i = 3; i <= 20; i++) {
                                try {
                                    Category category = new Category(row.get(1).toString())
                                            .withWeight(new BigDecimal(isNullOrEmpty(row.get(i - 1)) ? "0" : row.get(i - 1).toString()));

                                    Optional<Tier> tier = this.getTiersPerLevel()
                                            .get(i)
                                            .stream()
                                            .filter(t -> t.getName().equals(row.get(0).toString()))
                                            .findFirst();
                                    if (tier.isPresent()) {
                                        tier.get().withCategory(category);
                                    } else {
                                        Loggers.APPLICATION_LOG.error("No tier defined for Category <{}>.", row.get(0).toString());
                                    }

                                } catch (IllegalArgumentException ex) {
                                    Loggers.APPLICATION_LOG.debug("Skipping a row due to an Illegal Argument Exception.");
                                }
                            }
                        } catch (Exception ex) {
                            Loggers.APPLICATION_LOG.warn("Exception: ", ex);
                        }
                    } else Loggers.APPLICATION_LOG.info("Skipping a row, invalid category, missing name.");
                });
            }
        } catch (IOException ex) {
            Loggers.APPLICATION_LOG.warn("IOException", ex);
        }
    }

    private Sheets getSheetsService() throws IOException, NotAuthenticatedException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize() throws IOException, NotAuthenticatedException {
        GoogleAuthorizationCodeFlow flow = this.getFlow();

        Credential credential = flow.loadCredential("hawthorne");
        if (credential == null) {
            throw new NotAuthenticatedException("Error: Not Authorized.");
            //Not authorized
            //Break here and wait for explicit Authorization
        }
        this.isAuthenticated = true;
        return credential;

        //Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        //return credential;
    }

    public String requestAuthenticationURL() throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow();

        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();

        url.setRedirectUri("urn:ietf:wg:oauth:2.0:oob");

        return url.build();
    }

    public void authenticate(String username, String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow();
        GoogleTokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
                .execute();
        flow.createAndStoreCredential(response, "hawthorne");
    }

    protected GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws IOException {
        InputStream in = ItemService.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
    }

    public List<Item> getItems(Tier tier, Category category) {
        return this.items
                .parallelStream()
                .filter(item -> item.getCategoryName().equals(category.toString()) && item.getTierName().equals(tier.toString()))
                .collect(Collectors.toList());
    }

    public List<Item> getItems() {
        return this.items;
    }

    public Map<Integer, Set<Tier>> getTiersPerLevel() {
        return tiersPerLevel;
    }

    public Boolean getAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isNullOrEmpty(Object string) {
        return string == null || string.toString().isEmpty();

    }
}
