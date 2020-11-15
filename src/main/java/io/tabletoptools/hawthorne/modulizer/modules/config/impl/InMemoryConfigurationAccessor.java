/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 13.04.18 10:39
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
package io.tabletoptools.hawthorne.modulizer.modules.config.impl;

import io.tabletoptools.hawthorne.modulizer.modules.config.ConfigurationAccessor;

import java.util.Map;
import java.util.Optional;

public class InMemoryConfigurationAccessor implements ConfigurationAccessor {

    @Override
    public Optional<Object> getEntry(String key) {
        Map<String, Object> configuration = InMemoryConfigurationAccessorFactory.getGlobalConfigurations();
        return Optional.ofNullable(configuration.get(key));
    }

    @Override
    public Optional<Object> getEntry(Long scopedObjectId, String key) {
        Map<String, Object> configuration = InMemoryConfigurationAccessorFactory.getScopedConfigurations().get(scopedObjectId);
        if(configuration == null) return Optional.empty();
        return Optional.ofNullable(configuration.get(key));
    }

    @Override
    public Optional<Object> getEntry(String modulePath, String key) {
        Map<String, Object> configuration = InMemoryConfigurationAccessorFactory.getModuleConfigurations().get(modulePath);
        if(configuration == null) return Optional.empty();
        return Optional.ofNullable(configuration.get(key));
    }

    @Override
    public void setEntry(Object object, String key) {

    }

    @Override
    public void setEntry(Object object, String key, Long scopedObjectId) {

    }

    @Override
    public void setEntry(Object object, String key, String modulePath) {

    }

}
