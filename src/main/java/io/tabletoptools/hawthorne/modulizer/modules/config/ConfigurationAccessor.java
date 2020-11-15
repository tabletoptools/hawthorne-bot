package io.tabletoptools.hawthorne.modulizer.modules.config;

import java.util.Optional;

public interface ConfigurationAccessor {

    Optional<Object> getEntry(String key);
    Optional<Object> getEntry(Long scopedObjectId, String key);
    Optional<Object> getEntry(String modulePath, String key);

    void setEntry(Object object, String key);
    void setEntry(Object object, String key, Long scopedObjectId);
    void setEntry(Object object, String key, String modulePath);

}
