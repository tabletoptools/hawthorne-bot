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

public class ItemService {

    private static final String APPLICATION_NAME = "Hawthorne Loot Generator";
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);


    private List<Item> items = new ArrayList<>();
    private Map<String, List<Item>> tierSortedItemLists = new HashMap<>();
    private Map<Integer, List<WeightedTierCategoryPair>> levelBrackets = new HashMap<>();
    private Date lastUpdated;
    private Boolean isAuthenticated = false;
    private GoogleAuthorizationCodeFlow flow;

    private GoogleAuthorizationCodeFlow getFlow() {
        if(this.flow == null) {
            try {
                this.flow = getGoogleAuthorizationCodeFlow();
            }
            catch(IOException ex) {
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

    private final String itemSheetId = "1vj5ASpndXA4RFNV0L-ib7uvltLjH-k4FrqQWJG2uABM";

    public void load() throws NotAuthenticatedException {
        Loggers.APPLICATION_LOG.debug("Loading Item List");
        loadItemList();
        loadTierSortedItemLists();
        Loggers.APPLICATION_LOG.debug("Loading Level Brackets");
        loadLevelBrackets();
        this.lastUpdated = new Date();
    }

    public void update() throws NotAuthenticatedException {
        this.load();
    }

    private void loadTierSortedItemLists() {

        Map<String, List<Item>> tierSortedItemList = new HashMap<>();

        for (Item item : items) {
            String tierKey = item.getTier() + "-" + item.getCategory();
            if (tierSortedItemList.containsKey(tierKey)) {
                tierSortedItemList.get(tierKey).add(item);
            } else {
                List<Item> itemList = new ArrayList<>();
                itemList.add(item);
                tierSortedItemList.put(tierKey, itemList);
            }
        }
        this.tierSortedItemLists = tierSortedItemList;
    }

    private void loadItemList() throws NotAuthenticatedException {
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
                        String name = row.get(0).toString();


                        Integer numericTier = Integer.parseInt(row.get(1).toString());
                        Tier tier = Tier.valueOf("T" + numericTier);

                        BigDecimal weight = new BigDecimal(row.get(2) == null ? "0" : row.get(2).toString());

                        String categoryAsString = row.get(3).toString();
                        Category category = Category.fromString(categoryAsString);

                        Loggers.APPLICATION_LOG.debug("Processing Item <{}>: <{}>", tier.getTier(), name);

                        Map<Integer, Amount> amountPerLevel = new HashMap<>();
                        for (int i = 4; i < 22; i++) {
                            if(row.get(i) == null) {
                                amountPerLevel.put(i-1, new StaticAmount(1L));
                            }
                            else {
                                try {
                                    amountPerLevel.put(i-1, new StaticAmount(Long.parseLong(String.valueOf(row.get(i)))));
                                }
                                catch(NumberFormatException ex) {
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

    public void loadLevelBrackets() throws NotAuthenticatedException {
        Map<Integer, List<WeightedTierCategoryPair>> levelBrackets = new HashMap<>();
        try {
            Sheets service = getSheetsService();
            ValueRange response = service.spreadsheets().values().get(itemSheetId, "LevelBrackets!A2:T").execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) {
                Loggers.APPLICATION_LOG.error("No data found for supplied table.");
            } else {
                values.forEach(row -> {
                    try {
                        for (int x = 3; x <= 20; x++) {
                            try {
                                Tier tier = Tier.valueOf(row.get(0).toString());
                                Category category = Category.fromString(row.get(1).toString());
                                BigDecimal weight = BigDecimal.valueOf(0);
                                if (row.size() > x - 1) weight = new BigDecimal(row.get(x - 1).toString());
                                WeightedTierCategoryPair pair = new WeightedTierCategoryPair(tier, category, weight);
                                if (!levelBrackets.containsKey(x)) {
                                    levelBrackets.put(x, new ArrayList<>());
                                }
                                levelBrackets.get(x).add(pair);
                            } catch (IllegalArgumentException ex) {
                                Loggers.APPLICATION_LOG.debug("Skipping a row due to an Illegal Argument Exception.");

                            }
                        }
                    } catch (Exception ex) {
                        Loggers.APPLICATION_LOG.warn("Exception: ", ex);
                    }
                });

            }
        } catch (IOException ex) {
            Loggers.APPLICATION_LOG.warn("IOException: ", ex);
        }
        this.levelBrackets = levelBrackets;
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
        if(credential == null) {
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

    public List<Item> getItemList(Tier tier, Category category) {
        String tierName = tier + "-" + category;
        if (tierSortedItemLists.containsKey(tierName)) {
            return tierSortedItemLists.get(tierName);
        }
        return new ArrayList<Item>();
    }

    public List<Item> getItemList() {
        return this.items;
    }

    public Map<Integer, List<WeightedTierCategoryPair>> getLevelBrackets() {
        return this.levelBrackets;
    }

    public List<WeightedTierCategoryPair> getLevelBracket(Integer level) {
        return this.levelBrackets.get(level);
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        isAuthenticated = authenticated;
    }
}
