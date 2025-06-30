package org.gegolabs.mcp.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for documenting Capability classes, parameters, and methods.
 * This annotation allows documenting the description of the Capability,
 * the description of the execute method parameter, and the description of the result.
 * It can be used at the class level, parameter level, and method level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Name {
    /**
     * Description of the Capability. This is the default parameter, so it can be used without explicitly naming it.
     * @return the description of what this Capability does
     */
    String value() default "";
}
