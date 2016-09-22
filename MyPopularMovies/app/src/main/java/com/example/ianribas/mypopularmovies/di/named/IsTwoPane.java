package com.example.ianribas.mypopularmovies.di.named;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Documented
@Retention(RUNTIME)
public @interface IsTwoPane {
    /**
     * Dependency to indicate the app is in tablet "two pane" mode.
     */
    String value() default "istwopane";
}
