package com.deep3.medusLabs.aws.exceptions;

import com.amazonaws.SdkClientException;
import com.deep3.medusLabs.model.APIError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatus status, WebRequest request) {
        APIError apiError = new APIError(status, ex, request);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(InvalidPasswordComplexity.class)
    protected ResponseEntity<Object> handleNotAcceptable(InvalidPasswordComplexity ex, WebRequest request) {
        APIError apiError = new APIError(HttpStatus.NOT_ACCEPTABLE, ex,request);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(ObjectNotFoundException ex, WebRequest request) {
        APIError apiError = new APIError(HttpStatus.NOT_FOUND, ex, request);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, ex, request);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({
            SdkClientException.class,
            Exception.class
    })
    protected ResponseEntity<Object> handleInternalServerError(Exception ex, WebRequest request) {
        APIError apiError = new APIError(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(APIError apiError) {
        LOG.error(apiError.getMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
