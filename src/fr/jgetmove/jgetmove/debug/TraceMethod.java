/*
 * Copyright 2017 jGetMove
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
