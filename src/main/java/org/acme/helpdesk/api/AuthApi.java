package org.acme.helpdesk.api;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.helpdesk.dto.LoginRequest;
import org.acme.helpdesk.dto.LoginResponse;
import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.jwt.JwtService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Login endpoint for users and operators")
public class AuthApi {
    
    @Inject
    JwtService jwtService;

    private static final Logger LOG = Logger.getLogger(AuthApi.class);

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login", description = "Authenticate with username/password and receive a JWT token")
    public LoginResponse login(@Valid LoginRequest request) {
        User user = User.findByUsername(request.username);
        LOG.info("Attempting login for user: " + request.username);
        if (user == null || !BcryptUtil.matches(request.password, user.password)) {
            throw new NotAuthorizedException("Invalid credentials", Response.status(401).build());
        }
        LOG.info("Login successful for user: " + user.username);
        //Improvement if Login has a flag keep me logged in for 30 days generate token could be extended to 30 days
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.username, user.role.name()/* , jwtService.getTokenValiditySeconds()*/);
    }
}
