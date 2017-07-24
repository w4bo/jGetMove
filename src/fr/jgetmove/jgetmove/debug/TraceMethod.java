package fr.jgetmove.jgetmove.debug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify which method need to be traced by debug.
 * <p>
 * If the annotation is present, the first character of the method will be used as the symbol of the method.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TraceMethod {
    /**
     * Overwrites the default symbol
     *
     * @return the symbol to use for the method
     */
    char symbol() default 0;

    /**
     * Overwrites the method name as the title
     *
     * @return a custom title
     */
    String title() default "";

    /**
     * if set to true, it will display the title of the method if the logs are currently displayed from this method
     *
     * @return true if it needs to be display
     */
    boolean displayTitleIfLast() default false;

    /**
     * Allows to display the title of the method if the precedent log isn't from this method.
     *
     * @return true if the title can be displayed
     */
    boolean displayTitle() default false;
}
