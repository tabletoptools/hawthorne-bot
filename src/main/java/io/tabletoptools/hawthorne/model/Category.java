package io.tabletoptools.hawthorne.model;

import io.tabletoptools.hawthorne.modules.logging.Loggers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Category implements WeightedObject {

    private BigDecimal weight;
    private String name;
    private List<Item> items = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    @Override
    public BigDecimal getWeight() {
        return this.weight;
    }

    public Category withWeight(BigDecimal weight) {
        this.weight = weight;
        return this;
    }

    public Category withItem(Item item) {
        this.items.add(item);
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
