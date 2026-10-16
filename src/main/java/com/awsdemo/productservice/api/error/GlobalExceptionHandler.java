package com.awsdemo.productservice.api.error;

import com.awsdemo.productservice.config.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        log.info("Product lookup miss productId={}", ex.getProductId());
        return build(HttpStatus.NOT_FOUND, "Product Not Found", ex.getMessage(), "product-not-found");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request body has invalid fields", "validation-failed");
        pd.setProperty("errors", fieldErrors);
        log.info("Validation failed errors={}", fieldErrors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed Request",
                "Request body is missing or not valid JSON", "malformed-request");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found",
                "No endpoint for " + ex.getHttpMethod() + " /" + ex.getResourcePath(), "not-found");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", "internal-error");
    }

    private ProblemDetail build(HttpStatus status, String title, String detail, String typeSlug) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://api.dev.nexacommerce.in/errors/" + typeSlug));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("traceId", MDC.get(TraceIdFilter.MDC_KEY));
        return pd;
    }
}
