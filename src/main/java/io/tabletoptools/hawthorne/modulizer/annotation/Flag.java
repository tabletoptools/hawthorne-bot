package io.tabletoptools.hawthorne.modulizer.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {
    String value();
    boolean defaultValue();
}
