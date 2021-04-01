package ua.j2ee.nefodov.rateaggregator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.ExecutionException;

@ControllerAdvice
public class AggregatorExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> handleConflict(NullPointerException e, WebRequest request) {
        logger.warn(e.getClass().getName() + ". Message: " + e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse("Connection time-out"),
                headers, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleConflict(IllegalArgumentException e, WebRequest request) {
        logger.warn(e.getClass().getName() + ". Message: " + e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse(e.getMessage()),
                headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExecutionException.class)
    protected ResponseEntity<ErrorResponse> handleConflict(ExecutionException e, WebRequest request) {
        logger.warn(e.getClass().getName() + ". Message: " + e.getCause().getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);
        e.getCause().printStackTrace();
        if (e.getCause().getMessage().contains("Invalid")) {
            return new ResponseEntity<>(new ErrorResponse(e.getCause().getMessage()),
                    headers, HttpStatus.BAD_REQUEST);
        } else if (e.getCause().getMessage().contains("Unexpected")) {
            return new ResponseEntity<>(new ErrorResponse("Connection time-out"),
                    headers, HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>(new ErrorResponse(e.getCause().getMessage()),
                    headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(value = {InterruptedException.class})
    protected ResponseEntity<ErrorResponse> handleConflict(Exception e, WebRequest request) {
        logger.warn(e.getClass().getName() + ". Message: " + e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse("Internal server error"),
                headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
                                                                        HttpHeaders headers, HttpStatus status,
                                                                        WebRequest webRequest) {
        logger.warn("Connection timeout");
        if (webRequest.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse("Connection timeout"),
                headers, HttpStatus.REQUEST_TIMEOUT);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status,
                                                                          WebRequest request) {
        logger.warn("Missing request parameter");
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse("Missing request parameter: " + ex.getParameterName()),
                headers, HttpStatus.REQUEST_TIMEOUT);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatus status, WebRequest request) {
        logger.warn("Incorrect type of parameters");
        if (request.getParameter("xml") != null) headers.setContentType(MediaType.APPLICATION_XML);
        else headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(new ErrorResponse("Incorrect type of parameters. Date format - YYYY-MM-DD, " +
                "currency - iso 4217 code"),
                headers, HttpStatus.REQUEST_TIMEOUT);
    }

    private static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
