package com.GASB.slack_func.validator;

import com.GASB.slack_func.annotation.ValidSpaceId;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SpaceIdValidator implements ConstraintValidator<ValidSpaceId, String> {

    private final OrgSaaSRepo orgSaaSRepo;

    @Autowired
    public SpaceIdValidator(OrgSaaSRepo orgSaaSRepo) {
        this.orgSaaSRepo = orgSaaSRepo;
    }

    @Override
    public boolean isValid(String spaceId, ConstraintValidatorContext context) {
        boolean isValid = orgSaaSRepo.existsBySpaceId(spaceId);
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Space ID does not exist.")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
