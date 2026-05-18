package com.digitalwallet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates that a phone number loosely follows E.164 format.
 * We allow an optional '+' prefix and 7–15 total digits.
 *
 * Not using a library like libphonenumber here to keep dependencies light;
 * can swap this out later if stricter validation is needed.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // +[country code][number], total 8–16 chars
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    @Override
    public void initialize(ValidPhoneNumber annotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;  // @NotBlank handles the empty case, but let's be safe
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}
