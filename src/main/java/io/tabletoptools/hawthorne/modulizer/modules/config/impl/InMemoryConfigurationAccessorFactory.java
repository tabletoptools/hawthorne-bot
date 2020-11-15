/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 13.04.18 11:08
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

import java.util.HashMap;
import java.util.Map;

public class InMemoryConfigurationAccessorFactory {

    private static final Map<String, Object> globalConfigurations = new HashMap<>();
    private static final Map<String, Map<String, Object>> moduleConfigurations = new HashMap<>();
    private static final Map<Long, Map<String, Object>> scopedConfigurations = new HashMap<>();
    private static final ThreadLocal<InMemoryConfigurationAccessor> localAccessors = new ThreadLocal<>();



    public static Map<String, Object> getGlobalConfigurations() {
        return globalConfigurations;
    }

    public static Map<String, Map<String, Object>> getModuleConfigurations() {
        return moduleConfigurations;
    }

    public static Map<Long, Map<String, Object>> getScopedConfigurations() {
        return scopedConfigurations;
    }

    public static InMemoryConfigurationAccessor getLocalAccessor() {
        if(localAccessors.get() == null) localAccessors.set(new InMemoryConfigurationAccessor());
        return localAccessors.get();
    }

    public static void removeLocalAccessor() {
        localAccessors.remove();
    }
}
