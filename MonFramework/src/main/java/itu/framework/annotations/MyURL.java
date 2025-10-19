package itu.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface MyURL {
    String value() default "";
}
