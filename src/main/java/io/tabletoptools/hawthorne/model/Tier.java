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

public enum Tier {
    T1("T1"),
    T2("T2"),
    T3("T3"),
    T4("T4"),
    T5("T5"),
    T6("T6");

    private final String tier;

    Tier(String tier) {
        this.tier = tier;
    }

    public String getTier() {
        return this.tier;
    }
}
