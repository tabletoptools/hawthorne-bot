package io.tabletoptools.hawthorne.model;

import io.tabletoptools.hawthorne.modules.logging.Loggers;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicAmount implements Amount {

    private static final Pattern PATTERN = Pattern.compile("([0-9]+)d([0-9]+)");
    private static final Pattern QUERY_VALIDATION_PATTERN = Pattern.compile("[d0-9-/*+()]+");
    private static final JexlEngine JEXL_ENGINE = new JexlBuilder().create();
    private String query;

    private DynamicAmount(String query) {
        this.query = query;
    }

    public static DynamicAmount withQuery(String query) throws Exception {
        if(!validateExpression(query)) {
            throw new Exception("Error, entered query does not match validation pattern.");
        }
        return new DynamicAmount(query);
    }

    @Override
    public Long getAmount() {

        Matcher matcher = PATTERN.matcher(query);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            String amount = matcher.group(1);
            String dice = matcher.group(2);
            Integer result = rollDice(Integer.parseInt(amount), Integer.parseInt(dice));
            matcher.appendReplacement(buffer, result.toString());
        }
        matcher.appendTail(buffer);

        String result = String.valueOf(evaluateExpression(buffer.toString()));
        try {
            return Long.parseLong(result);
        }
        catch (NumberFormatException ex) {
            try {
                return Math.round(Double.parseDouble(result));
            }
            catch(NumberFormatException ex2) {
                Loggers.APPLICATION_LOG.warn("Caught an exception calculating a dynamic amount: <{}>", ex2);

            }
        }

        return null;
    }

    private Integer rollDice(Integer amount, Integer die) {

        Integer total = 0;

        for (Integer i = 0; i < amount; i++) {
            total+=rollDie(die);
        }

        return total;

    }

    private Integer rollDie(Integer die) {
        return ThreadLocalRandom.current().nextInt(1, die + 1);
    }

    private Object evaluateExpression(String expression) {
        JexlExpression expr = JEXL_ENGINE.createExpression(expression);
        return expr.evaluate(new MapContext());
    }

    private static boolean validateExpression(String expression) {
        return QUERY_VALIDATION_PATTERN.matcher(expression).matches();
    }
}
