/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 06.02.18 16:19
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

import io.tabletoptools.hawthorne.model.WeightedObject;

import javax.swing.text.html.parser.Entity;
import java.math.BigDecimal;
import java.util.List;

public class RandomWeightedListService {

    private static RandomWeightedListService instance;

    public static RandomWeightedListService instance() {
        if(instance == null) {
            instance = new RandomWeightedListService();
        }
        return instance;
    }

    public <T extends WeightedObject> T getRandomEntity(List<T> entities) {

        BigDecimal totalWeight = BigDecimal.valueOf(0);
        for (T entity : entities)
        {
            totalWeight = totalWeight.add(entity.getWeight());
        }

        BigDecimal random = BigDecimal.valueOf(Math.random()).multiply(totalWeight);
        for(T entity : entities) {
            random = random.subtract(entity.getWeight());
            if(random.compareTo(BigDecimal.valueOf(0)) <= 0) {
                return entity;
            }
        }
        return null;
    }

}
