package io.tabletoptools.hawthorne.util;

import io.tabletoptools.hawthorne.model.ChoosableEntity;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SearchUtils {

    public static <T extends ChoosableEntity> List<T> search(String query, Collection<T> entities) {

        List<String> entityNames = entities.stream().map(T::getName).collect(Collectors.toList());

        // Full match query
        List<T> selection = entities.stream().filter(entity -> entity.getName().toLowerCase().equals(query))
                .collect(Collectors.toList());

        // Partial Match
        if (selection.isEmpty()) {
            selection = entities.stream().filter(entity -> entity.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // Fuzzy Match
        if (selection.isEmpty()) {
            List<ExtractedResult> possibleEntities = FuzzySearch.extractTop(query, entityNames, 5, 5);
            selection = entities.stream()
                    .filter(entity ->
                            possibleEntities.stream().anyMatch(extractedResult ->
                                    entity.getName().equals(extractedResult.getString())))
                    .collect(Collectors.toList());
        }

        return selection;

    }

}
