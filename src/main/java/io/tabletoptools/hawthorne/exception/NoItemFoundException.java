/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 04.02.18 19:03
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
package io.tabletoptools.hawthorne.exception;

import io.tabletoptools.hawthorne.model.WeightedTierCategoryPair;

public class NoItemFoundException extends Exception {
    private WeightedTierCategoryPair tierCategoryPair;

    public NoItemFoundException(WeightedTierCategoryPair tierCategoryPair) {
        this.tierCategoryPair = tierCategoryPair;
    }

    public WeightedTierCategoryPair getTierCategoryPair() {
        return tierCategoryPair;
    }

    public void setTierCategoryPair(WeightedTierCategoryPair tierCategoryPair) {
        this.tierCategoryPair = tierCategoryPair;
    }
}
