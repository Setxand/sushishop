package com.sushishop.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MyConstraintValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUser {
	String message() default "Invalid User";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
