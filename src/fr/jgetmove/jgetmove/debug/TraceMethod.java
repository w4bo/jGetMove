package fr.jgetmove.jgetmove.debug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TraceMethod {
    char symbol() default 0;

    String title() default "";

    boolean displayTitleIfLast() default false;

    boolean displayTitle() default false;
}
