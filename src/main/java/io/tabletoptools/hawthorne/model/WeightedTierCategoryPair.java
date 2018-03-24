/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 04.02.18 18:32
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

public class WeightedTierCategoryPair implements WeightedObject {

    private Tier tier;
    private Category category;
    private BigDecimal weight;
    private BigDecimal chance;

    public WeightedTierCategoryPair(Tier tier, Category category, BigDecimal weight) {
        this.tier = tier;
        this.category = category;
        this.weight = weight;
    }

    public WeightedTierCategoryPair(Tier tier, Category category, BigDecimal weight, BigDecimal chance) {
        this.tier = tier;
        this.category = category;
        this.weight = weight;
        this.chance = chance;
    }

    public Tier getTier() {
        return tier;
    }


    public Category getCategory() {
        return category;
    }

    public BigDecimal getWeight() {
        return weight;
    }


    public BigDecimal getChance() {
        return chance;
    }

    public WeightedTierCategoryPair withChance(BigDecimal chance) {
        return new WeightedTierCategoryPair(this.tier, this.category, this.weight, chance);
    }
}
