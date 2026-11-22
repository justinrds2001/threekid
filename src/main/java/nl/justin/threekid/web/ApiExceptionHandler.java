package nl.justin.threekid.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import nl.justin.threekid.domain.SelfReferenceException;
import nl.justin.threekid.domain.TooManyParentsException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(SelfReferenceException.class)
	ProblemDetail handleSelfReference(SelfReferenceException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(TooManyParentsException.class)
	ProblemDetail handleTooManyParents(TooManyParentsException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
