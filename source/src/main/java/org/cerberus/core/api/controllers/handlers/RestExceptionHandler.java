/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.api.controllers.handlers;

import org.cerberus.core.api.controllers.wrappers.ResponseWrapper;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedInsertOperationException;
import org.cerberus.core.api.exceptions.FailedReadOperationException;
import org.cerberus.core.api.exceptions.InvalidRequestException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author mlombard
 */
@RestControllerAdvice(basePackages = "org.cerberus.core.api")
public class RestExceptionHandler {

    public RestExceptionHandler() {
        super();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String error = ex.getParameterName() + " parameter is missing";
        return this.buildResponseEntity(new ResponseWrapper<>(HttpStatus.BAD_REQUEST, error, ex));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {

        StringBuilder builder = new StringBuilder()
                .append(ex.getContentType())
                .append(" media type is not supported. Supported media types are ")
                .append(
                        ex.getSupportedMediaTypes()
                                .stream()
                                .map(MediaType::toString)
                                .collect(Collectors.joining(", "))
                );

        return this.buildResponseEntity(new ResponseWrapper<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.toString(), ex));
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.BAD_REQUEST);
        responseWrapper.setMessage("Validation error");
        responseWrapper.addValidationErrors(ex.getBindingResult().getFieldErrors());
        responseWrapper.addValidationError(ex.getBindingResult().getGlobalErrors());
        return this.buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.BAD_REQUEST, "Illegal argument", ex);
        return this.buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String error = "Malformed JSON request";
        return this.buildResponseEntity(new ResponseWrapper<>(HttpStatus.BAD_REQUEST, error, ex));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex) {
        String error = "Error writing JSON output";
        return this.buildResponseEntity(new ResponseWrapper<>(HttpStatus.INTERNAL_SERVER_ERROR, error, ex));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex) {

        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.BAD_REQUEST);
        responseWrapper.setMessage(String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()));
        responseWrapper.setDebugMessage(ex.getMessage());
        return this.buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.NOT_FOUND);
        responseWrapper.setMessage(ex.getMessage());
        return this.buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleInvalidRequestException(
            InvalidRequestException ex, WebRequest request) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.UNPROCESSABLE_ENTITY);
        responseWrapper.setMessage(ex.getMessage());
        return buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.BAD_REQUEST);
        responseWrapper.setMessage(
                String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                        ex.getName(), ex.getValue(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName())
        );
        responseWrapper.setDebugMessage(ex.getMessage());
        return this.buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler({BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<Object> handleAccessDeniedException(final BadCredentialsException ex, final WebRequest request) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.UNAUTHORIZED);
        responseWrapper.setMessage(ex.getMessage());
        return buildResponseEntity(responseWrapper);
    }

    @ExceptionHandler({DataAccessException.class, FailedInsertOperationException.class, FailedReadOperationException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleDatabaseException(final RuntimeException ex, final WebRequest request) {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>(HttpStatus.INTERNAL_SERVER_ERROR);
        responseWrapper.setMessage(ex.getMessage());
        return buildResponseEntity(responseWrapper);
    }

    private ResponseEntity<Object> buildResponseEntity(ResponseWrapper<Object> responseWrapper) {
        return new ResponseEntity<>(responseWrapper, responseWrapper.getStatus());
    }

}
