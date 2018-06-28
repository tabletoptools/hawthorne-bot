/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 03.02.18 18:36
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

import io.tabletoptools.hawthorne.exception.NoItemFoundException;
import io.tabletoptools.hawthorne.exception.NotAuthenticatedException;
import io.tabletoptools.hawthorne.model.Category;
import io.tabletoptools.hawthorne.model.Item;
import io.tabletoptools.hawthorne.model.Tier;
import io.tabletoptools.hawthorne.model.WeightedTierCategoryPair;
import io.tabletoptools.hawthorne.modules.logging.Loggers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RandomItemService {

    private static RandomItemService instance;
    private boolean respectItemCount = true;

    public static RandomItemService instance() {
        if (instance == null) {
            instance = new RandomItemService();
        }
        return instance;
    }

    public void setRespectItemCount(boolean respectItemCount) {
        this.respectItemCount = respectItemCount;
    }

    public Item getRandomItem(Integer APL) throws NoItemFoundException, NotAuthenticatedException {

        List<WeightedTierCategoryPair> bracket = ItemService.instance().getLevelBracket(APL);
        WeightedTierCategoryPair tierCategoryPair = getRandomTierCategoryPair(bracket);

        assert tierCategoryPair != null;
        return getRandomItemFromList(tierCategoryPair, ItemService.instance().getItemList(tierCategoryPair.getTier(), tierCategoryPair.getCategory()));

    }

    private WeightedTierCategoryPair getRandomTierCategoryPair(List<WeightedTierCategoryPair> pairs) throws NotAuthenticatedException {

        BigDecimal totalWeight = BigDecimal.valueOf(0);
        for (WeightedTierCategoryPair pair : pairs) {
            totalWeight = totalWeight.add(this.respectItemCount
                    ? pair.getWeight().multiply(BigDecimal.valueOf(ItemService.instance().getItemList(pair.getTier(), pair.getCategory()).size()))
                    : pair.getWeight());
        }

        BigDecimal random = BigDecimal.valueOf(Math.random()).multiply(totalWeight);
        for (WeightedTierCategoryPair pair : pairs) {
            random = random.subtract(this.respectItemCount
                    ? pair.getWeight().multiply(BigDecimal.valueOf(ItemService.instance().getItemList(pair.getTier(), pair.getCategory()).size()))
                    : pair.getWeight());
            if (random.compareTo(BigDecimal.valueOf(0)) <= 0) {
                if (totalWeight.compareTo(BigDecimal.valueOf(0)) == 0) {
                    return pair.withChance(BigDecimal.valueOf(100));
                }
                return pair.withChance(this.respectItemCount ? pair.getWeight().multiply(BigDecimal.valueOf(ItemService.instance().getItemList(pair.getTier(), pair.getCategory()).size())).divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        : pair.getWeight().divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
            }
        }
        return null;
    }

    private Item getRandomItemFromList(WeightedTierCategoryPair tierCategoryPair, List<Item> items) {

        BigDecimal totalWeight = BigDecimal.valueOf(0);
        for (Item i : items) {
            totalWeight = totalWeight.add(i.getWeight());
        }

        BigDecimal random = BigDecimal.valueOf(Math.random()).multiply(totalWeight);
        for (Item i : items) {
            random = random.subtract(i.getWeight());
            if (random.compareTo(BigDecimal.valueOf(0)) <= 0) {
                return i.withChance(calculateChance(tierCategoryPair.getChance(), i.getWeight().divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))));

            }
        }
        return null;
    }

    public BigDecimal getChanceForItem(Integer level, Item item) throws NoItemFoundException, NotAuthenticatedException {
        List<WeightedTierCategoryPair> pairs = ItemService.instance().getLevelBracket(level);
        BigDecimal totalTierWeight = BigDecimal.valueOf(0);
        WeightedTierCategoryPair containingPair = null;
        for (WeightedTierCategoryPair pair : pairs) {
            totalTierWeight = totalTierWeight.add(this.respectItemCount
                    ? pair.getWeight().multiply(BigDecimal.valueOf(ItemService.instance().getItemList(pair.getTier(), pair.getCategory()).size()))
                    : pair.getWeight());
            Loggers.APPLICATION_LOG.debug("Skipping Tier <{}.{}>, no items in tier category pair.", pair.getTier(), pair.getCategory());
            if (item.getTier() == pair.getTier() && item.getCategory() == pair.getCategory()) {
                containingPair = pair;
            }
        }

        assert containingPair != null;
        List<Item> items = ItemService.instance().getItemList(containingPair.getTier(), containingPair.getCategory());

        if (containingPair.getWeight().compareTo(BigDecimal.valueOf(0)) == 0) return BigDecimal.valueOf(0);

        BigDecimal tierChance = this.respectItemCount ? containingPair.getWeight().multiply(BigDecimal.valueOf(items.size())).divide(totalTierWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : containingPair.getWeight().divide(totalTierWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        BigDecimal totalItemWeight = BigDecimal.valueOf(0);
        for (Item i : items) {
            if (i.getTier() == containingPair.getTier() && i.getCategory() == containingPair.getCategory()) {

                totalItemWeight = totalItemWeight.add(i.getWeight());
            }
        }

        if (totalItemWeight.compareTo(BigDecimal.valueOf(0)) == 0) throw new NoItemFoundException(containingPair);

        BigDecimal itemChance = item.getWeight().divide(totalItemWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return calculateChance(tierChance, itemChance);
    }

    public BigDecimal getChanceForTier(Integer level, Tier tier, Category category) throws NotAuthenticatedException {
        List<WeightedTierCategoryPair> pairs = ItemService.instance().getLevelBracket(level);
        BigDecimal totalTierWeight = BigDecimal.valueOf(0);
        WeightedTierCategoryPair containingPair = null;
        for (WeightedTierCategoryPair pair : pairs) {
            totalTierWeight = totalTierWeight.add(this.respectItemCount
                    ? pair.getWeight().multiply(BigDecimal.valueOf(ItemService.instance().getItemList(pair.getTier(), pair.getCategory()).size()))
                    : pair.getWeight());

            if (tier == pair.getTier() && category == pair.getCategory()) {
                containingPair = pair;
            }
        }
        assert containingPair != null;
        List<Item> items = ItemService.instance().getItemList(containingPair.getTier(), containingPair.getCategory());

        if (containingPair.getWeight().compareTo(BigDecimal.valueOf(0)) == 0) return BigDecimal.valueOf(0);

        return containingPair.getWeight().multiply(this.respectItemCount ? BigDecimal.valueOf(items.size()) : BigDecimal.valueOf(1)).divide(totalTierWeight, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));


    }

    private BigDecimal calculateChance(BigDecimal tierChance, BigDecimal itemChance) {
        return tierChance.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).multiply(itemChance.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)).multiply(BigDecimal.valueOf(100));
    }

    private Integer getAmountOfItemsForTierInList(WeightedTierCategoryPair tierCategoryPair, List<Item> items) {
        int x = 0;
        for (Item item : items) {
            if (item.getTier() == tierCategoryPair.getTier() && item.getCategory() == tierCategoryPair.getCategory()) {
                x++;
            }
        }
        return x;
    }

}
