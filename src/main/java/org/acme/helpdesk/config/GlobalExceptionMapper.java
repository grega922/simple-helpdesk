package org.acme.helpdesk.config;

import jakarta.ws.rs.*;
import jakarta.ws.rs.ext.*;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import org.acme.helpdesk.dto.ErrorResponse;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    //Check Quarkus Security exceptions (Such as wrong user roles on wrong API) and return body
     @ServerExceptionMapper(io.quarkus.security.UnauthorizedException.class)
    public Response handleUnauthorized(io.quarkus.security.UnauthorizedException e) {
        return buildResponse("UNAUTHORIZED", "Login required!", 401);
    }
    @ServerExceptionMapper(io.quarkus.security.ForbiddenException.class)
    public Response handleForbidden(io.quarkus.security.ForbiddenException e) {
        return buildResponse("FORBIDDEN", "Wrong user permissions!", 403);
    }

    // This method handles all exceptions thrown by the application and maps them to appropriate HTTP responses
    @Override
    public Response toResponse(Exception exception) {

        //Check type of exception and return response with error message and status code
        if (exception instanceof NotFoundException e) {
            return buildResponse("NOT_FOUND", e.getMessage(), 404);
        }
        if (exception instanceof BadRequestException e) {
            return buildResponse("BAD_REQUEST", e.getMessage(), 400);
        }
        if (exception instanceof ForbiddenException e) {
            return buildResponse("FORBIDDEN", e.getMessage(), 403);
        }
        if (exception instanceof NotAuthorizedException e) {
            return buildResponse("UNAUTHORIZED", e.getMessage(), 401);
        }
        if (exception instanceof jakarta.validation.ConstraintViolationException e) {
            String details = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
            return buildResponse("VALIDATION_ERROR", details, 400);
        }

        LOG.error("Unhandled exception", exception);
        return buildResponse("INTERNAL_ERROR", "An unexpected error occurred", 500);
    }

    private Response buildResponse(String error, String message, int status) {
        return Response.status(status)
                .entity(new ErrorResponse(error, message))
                .build();
    }
}
