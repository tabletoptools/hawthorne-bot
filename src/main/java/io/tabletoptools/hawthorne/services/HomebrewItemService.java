package io.tabletoptools.hawthorne.services;

import io.tabletoptools.hawthorne.model.LookupItem;
import io.tabletoptools.hawthorne.modules.logging.Loggers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HomebrewItemService {

    private static HomebrewItemService instance;

    private List<LookupItem> items = new ArrayList<>();

    public static HomebrewItemService instance() {
        if (instance == null) {
            instance = new HomebrewItemService();
            instance.load();
        }
        return instance;
    }

    public void update() {
        this.load();
    }

    public void load() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/tabletoptools/Hawthorne-Docs/develop/hawthorne/homebrew.md");

            URLConnection urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
            InputStream inputStream = urlConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);

            ArrayList<LookupItem> items = new ArrayList<>();
            boolean passedIntroduction = false;
            LookupItem currentItem = new LookupItem();
            while (scanner.hasNextLine()) {
                String next = scanner.nextLine();
                if (!passedIntroduction && "***".equals(next)) {
                    passedIntroduction = true;

                } else if (passedIntroduction && "***".equals(next)) {
                    // Save current item
                    items.add(currentItem);
                    currentItem = new LookupItem();
                } else if (passedIntroduction) {
                    if(currentItem.getName() == null) {
                        if(!next.isEmpty() && next.length() > 3) {
                            currentItem.setName(next.substring(3));
                        }
                    } else currentItem.appendDescription(next);
                }

            }
            scanner.close();
            urlConnection.getInputStream().close();
            this.items = items;
        } catch (IOException e) {
            Loggers.APPLICATION_LOG.error("Error reading homebrew docs.");
        }
    }

    public List<LookupItem> getItems() {
        return items;
    }
}
