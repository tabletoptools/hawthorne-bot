/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 12.04.18 12:50
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
package io.tabletoptools.hawthorne.modulizer;

import java.lang.reflect.Method;
import java.util.List;

public class MethodMatch {

    private Method method;
    private Integer depth;
    private Module module;

    public MethodMatch(Module module, Method method, Integer depth) {
        this.module = module;
        this.method = method;
        this.depth = depth;
    }

    public Method getMethod() {
        return method;
    }

    public Module getModule() {
        return module;
    }

    public Integer getDepth() {
        return depth;
    }

}