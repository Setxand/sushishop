package com.sushishop.validation;

import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MyConstraintValidator implements ConstraintValidator<ValidUser, UserDTO> {

	@Override
	public void initialize(ValidUser constraintAnnotation) {

	}

	@Override
	public boolean isValid(UserDTO dto, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;

		if (User.UserRole.ROLE_ADMIN.name().equals(dto.role)) {
			isValid = SecurityContextHolder.getContext()
					.getAuthentication()
					.getAuthorities()
					.stream()
					.anyMatch(a -> a.getAuthority().equals(User.UserRole.ROLE_ADMIN.name()));
		}

		return isValid;
	}
}
