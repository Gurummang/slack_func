package com.GASB.slack_func.validator;

import com.GASB.slack_func.annotation.ValidWorkdSpace;
import com.GASB.slack_func.repository.org.WorkspaceConfigRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkSpaceIdValidator implements ConstraintValidator<ValidWorkdSpace, Integer> {

    private final WorkspaceConfigRepo workspaceRepo; // 변수명 수정
    @Override
    public boolean isValid(Integer workspaceId, ConstraintValidatorContext context) { // 변수명 수정
        boolean isValid = workspaceRepo.existsById(workspaceId); // 변수명 수정
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Space ID does not exist.")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
