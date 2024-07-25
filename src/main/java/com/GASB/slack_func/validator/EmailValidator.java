package com.GASB.slack_func.validator;

import com.GASB.slack_func.annotation.ValidEmail;
import com.GASB.slack_func.repository.org.AdminRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private final AdminRepo adminRepo;

    @Autowired
    public EmailValidator(AdminRepo adminRepo) {
        this.adminRepo = adminRepo;
    }

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