package io.tabletoptools.hawthorne.model;

import io.tabletoptools.hawthorne.Loggers;

public enum Category {
    Consumable,
    Combat,
    NonCombat,
    Summoning,
    GameChanging,
    NYI;

    public static Category fromString(String value) {
        try {
            return Category.valueOf(value);
        }
        catch(IllegalArgumentException ex) {
            switch (value) {
                case "Non-Combat":
                    return Category.NonCombat;
                case "Game-Changing":
                    return Category.GameChanging;
                default:
                    Loggers.APPLICATION_LOG.warn("Caught unknown Category: <{}>", value);
                    return Category.NYI;
            }
        }
    }
}
