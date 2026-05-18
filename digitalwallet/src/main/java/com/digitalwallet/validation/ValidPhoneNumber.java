package com.digitalwallet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom constraint for E.164-ish phone number format.
 * Allows +CountryCode followed by 7–14 digits, e.g. +14155552671
 *
 * Using a custom annotation here instead of @Pattern so the error message
 * is descriptive and we can change the regex in one place.
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Phone number must be in E.164 format (e.g. +14155552671)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
