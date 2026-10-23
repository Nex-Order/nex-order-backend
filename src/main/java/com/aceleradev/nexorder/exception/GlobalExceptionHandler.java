package com.aceleradev.nexorder.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ApiResponse(
            responseCode = "400",
            description = "Request validation error",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> methodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest httpServletRequest
    ) {

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid fields"
        );

        error.setTitle("Bad Request");
        error.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> invalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest httpServletRequest
    ) {

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );

        error.setTitle("Unauthorized");
        error.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFoundException(
            NotFoundException ex,
            HttpServletRequest httpServletRequest
    ) {

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        error.setTitle("Not Found");
        error.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ApiResponse(
            responseCode = "409",
            description = "Conflict while executing the operation",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> conflictException(
            ConflictException ex,
            HttpServletRequest httpServletRequest
    ) {

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        error.setTitle("Conflict");
        error.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ApiResponse(
            responseCode = "422",
            description = "Business rule not called",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> businessRuleException(
            BusinessRuleException ex,
            HttpServletRequest httpServletRequest
    ) {

        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );

        error.setTitle("Unprocessable Content");
        error.setInstance(URI.create(httpServletRequest.getRequestURI()));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid fields"
        );

        problem.setTitle("Bad Request");
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.badRequest().body(problem);
    }

}
