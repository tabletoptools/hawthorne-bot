/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 02.02.18 17:19
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
package io.tabletoptools.hawthorne.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Item implements WeightedObject {
    private String name;
    private Tier tier;
    private BigDecimal weight;
    private Category category;
    private BigDecimal chance;
    private Map<Integer, Amount> amountPerLevel = new HashMap<>();

    public Item(String name, Tier tier, BigDecimal weight, Category category, Map<Integer, Amount> amountPerLevel) {
        this.name = name;
        this.tier = tier;
        this.weight = weight;
        this.category = category;
        this.amountPerLevel = amountPerLevel;
    }

    public Item(String name, Tier tier, BigDecimal weight, Category category, Map<Integer, Amount> amountPerLevel, BigDecimal chance) {
        this.name = name;
        this.tier = tier;
        this.weight = weight;
        this.category = category;
        this.chance = chance;
        this.amountPerLevel = amountPerLevel;
    }

    public String getName() {
        return name;
    }

    public Tier getTier() {
        return tier;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getChance() {
        return chance;
    }

    public Map<Integer, Amount> getAmountPerLevel() {
        return amountPerLevel;
    }

    public void setAmountPerLevel(Map<Integer, Amount> amountPerLevel) {
        this.amountPerLevel = amountPerLevel;
    }

    public Item withChance(BigDecimal chance) {
        return new Item(this.name, this.tier, this.weight, this.category, this.amountPerLevel, chance);
    }
}
