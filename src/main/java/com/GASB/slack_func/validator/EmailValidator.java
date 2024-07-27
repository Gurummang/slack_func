package com.GASB.slack_func.validator;

import com.GASB.slack_func.annotation.ValidEmail;
import com.GASB.slack_func.repository.org.AdminRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private final AdminRepo adminRepo;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        boolean isValid = adminRepo.existsByEmail(email);
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email does not exist.")
                    .addConstraintViolation();
        }
        return isValid;
    }
}