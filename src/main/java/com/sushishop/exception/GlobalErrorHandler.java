package com.sushishop.exception;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

	private static final Logger logger = Logger.getLogger(GlobalErrorHandler.class);


	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException ex) {
		return new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalStateException(final IllegalStateException ex) {
		return new ErrorResponse("INVALID_STATE", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler({AccessDeniedException.class, BadCredentialsException.class})
	public ErrorResponse handleAccessDenied(RuntimeException ex) {
		return new ErrorResponse("ACCESS_DENIED", ex.getMessage());
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAccessDeniedException(final AuthenticationException ex) {
		return new ErrorResponse("AUTH_FAILED", ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleRuntimeException(final RuntimeException ex) {
		logger.warn("Runtime exception: ", ex);
		return new ErrorResponse("INTERNAL_ERROR", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleInternalError(final Exception ex) {
		return new ErrorResponse("INTERNAL_ERROR", ex.getMessage());
	}
}

