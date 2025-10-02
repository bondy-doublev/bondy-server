package org.example.interactionservice.validator.MaxSizeFromConfig;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.example.interactionservice.property.PropsConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MaxSizeFromConfigValidator implements ConstraintValidator<MaxSizeFromConfig, List<?>> {
    private final PropsConfig props;
    private String configKey;
    private String messageTemplate;

    @Override
    public void initialize(MaxSizeFromConfig constraintAnnotation) {
        this.configKey = constraintAnnotation.configKey();
        this.messageTemplate = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        int limit = switch (configKey) {
            case "media" -> props.getPost().getMediaLimit();
            case "tags"  -> props.getPost().getTagLimit();
            case "content" -> props.getPost().getContentLimit();
            default -> Integer.MAX_VALUE;
        };

        if (value.size() > limit) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    messageTemplate.replace("{limit}", String.valueOf(limit))
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
