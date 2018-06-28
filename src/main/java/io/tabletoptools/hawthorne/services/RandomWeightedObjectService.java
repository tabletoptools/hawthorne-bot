package io.tabletoptools.hawthorne.services;

import io.tabletoptools.hawthorne.model.WeightedObject;
import io.tabletoptools.hawthorne.modules.logging.Loggers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class RandomWeightedObjectService {

    public static <T extends WeightedObject> T getObject(Collection<T> objects) {

        BigDecimal totalWeight = getTotalWeight(objects);

        BigDecimal random = BigDecimal.valueOf(Math.random()).multiply(totalWeight);

        Loggers.APPLICATION_LOG.debug("Rolled <{}>", random.setScale(2, RoundingMode.HALF_UP).toString());

        return getObject(objects, random);

    }

    private static <T extends WeightedObject> BigDecimal getTotalWeight(Collection<T> objects) {
        BigDecimal totalWeight = BigDecimal.valueOf(0);
        for (T object : objects) {
            totalWeight = totalWeight.add(object.getWeight());
        }
        return totalWeight;
    }

    private static <T extends WeightedObject> T getObject(Collection<T> objects, BigDecimal random) {

        BigDecimal zero = BigDecimal.valueOf(0);

        for (T object : objects) {
            random = random.subtract(object.getWeight());
            Loggers.APPLICATION_LOG.trace("Subtracted weight <{}> from random resulting in <{}>", object.getWeight().toString(), random.toString());
            if (random.compareTo(zero) <= 0) {
                return object;
            }
        }
        return null;
    }

}
