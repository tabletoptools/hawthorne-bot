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
import java.util.Map;

public class Item implements WeightedObject {
    private String name;
    private String tierName;
    private BigDecimal weight;
    private String categoryName;
    private Map<Integer, Amount> amountPerLevel;

    public Item(String name, String tierName, BigDecimal weight, String categoryName, Map<Integer, Amount> amountPerLevel) {
        this.name = name;
        this.tierName = tierName;
        this.weight = weight;
        this.categoryName = categoryName;
        this.amountPerLevel = amountPerLevel;
    }

    public Item(String name, BigDecimal weight, Map<Integer, Amount> amountPerLevel) {
        this.name = name;
        this.weight = weight;
        this.amountPerLevel = amountPerLevel;
    }

    public String getName() {
        return name;
    }

    public String getTierName() {
        return tierName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Map<Integer, Amount> getAmountPerLevel() {
        return amountPerLevel;
    }

    public void setAmountPerLevel(Map<Integer, Amount> amountPerLevel) {
        this.amountPerLevel = amountPerLevel;
    }

    @Override
    public String toString() {
        return name;
    }
}
