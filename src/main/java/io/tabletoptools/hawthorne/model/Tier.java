/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 02.02.18 17:20
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tier implements WeightedObject, Comparable {

    private String name;
    private BigDecimal weight;
    private List<Category> categories = new ArrayList<>();

    public Tier(String name) {
        this.name = name;
    }

    public Tier(String name, BigDecimal weight) {
        this.name = name;
        this.weight = weight;
    }

    @Override
    public BigDecimal getWeight() {
        return this.weight;
    }

    public Tier withWeight(BigDecimal weight) {
        this.weight = weight;
        return this;
    }

    public Tier withCategory(Category category) {
        this.categories.add(category);
        return this;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(Object o) {
        Tier ot = (Tier)o;
        if(this.equals(o)) return 0;
        if(this.getName().equals(ot.getName())) return 0;
        else return Integer.parseInt(this.getName().substring(1)) - Integer.parseInt(ot.getName().substring(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tier)) return false;
        Tier tier = (Tier) o;
        return Objects.equals(getName(), tier.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
