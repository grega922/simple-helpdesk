package org.acme.helpdesk.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.helpdesk.dto.ConversationResponse;
import org.acme.helpdesk.dto.CreateConversationRequest;
import org.acme.helpdesk.dto.MessageResponse;
import org.acme.helpdesk.dto.SendMessageRequest;
import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.entity.Message;
import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.service.ConversationService;
import org.acme.helpdesk.service.MessageService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.acme.helpdesk.dto.ErrorResponse;
import io.quarkus.security.identity.SecurityIdentity;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;


@Path("/v1/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
@Tag(name = "User Conversations", description = "Endpoints for mobile app users")
@SecurityRequirement(name = "BearerAuth")
public class ConversationApi {

    @Inject
    ConversationService conversationService;

    @Inject
    MessageService messageService;

    @Inject
    SecurityIdentity identity;

    private static final Logger LOG = Logger.getLogger(ConversationApi.class);

    @GET
    @Operation(summary = "List of user conversations", description = "List all conversations created by the current user")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of conversations returned successfully"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires USER)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ConversationResponse> myConversations() {
        return conversationService.getUserConversations(identity.getPrincipal().getName()).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @POST
    @Path("/new")
    @Operation(summary = "Create new conversation",
            description = "Create a new helpdesk conversation in a selected room with an initial message")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Conversation created successfully",
            content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @APIResponse(responseCode = "400", description = "Validation error (missing room/title/message)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Room not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createConversation(@Valid CreateConversationRequest request) {
        
        Conversation c = conversationService.createConversation(identity.getPrincipal().getName(), request);
        return Response.status(Response.Status.CREATED)
                .entity(ConversationResponse.from(c))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get specific conversation", description = "Get conversation details by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Conversation details returned",
            content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Conversation does not belong to the current user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ConversationResponse getConversation(
            @PathParam("id") @Parameter(description = "Conversation identifier", example = "1") Long id) {
        Conversation c = conversationService.getConversation(id);
        User currentUser = User.findByUsername(identity.getPrincipal().getName());
        if (c == null || !c.user.id.equals(currentUser.id)) {
            throw new ForbiddenException("Not your conversation");
        }
        return ConversationResponse.from(c);
    }

    @GET
    @Path("/{id}/messages")
    @Operation(summary = "Get messages in conversation", description = "List all messages in a conversation. Supports long-polling via 'since' parameter.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Messages returned successfully"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Conversation does not belong to the current user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<MessageResponse> getMessages(
            @PathParam("id")  @Parameter(description = "Conversation identifier", example = "1") Long id,
            @QueryParam("since") @Parameter(description = "ISO 8601 timestamp — returns only messages newer than this value (for polling)", example = "2023-01-01T10:00:00")
            LocalDateTime since) {
        Conversation c = conversationService.getConversation(id);
        User currentUser = User.findByUsername(identity.getPrincipal().getName());
        if (c == null || !c.user.id.equals(currentUser.id)) {
            throw new ForbiddenException("Not your conversation");
        }
        return messageService.getMessages(id, since).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @POST
    @Path("/{id}/messages")
    @Operation(summary = "Send message to conversation", description = "Send a message in an active conversation")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Message sent successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @APIResponse(responseCode = "400", description = "Conversation is not active or validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Not a participant in this conversation",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response sendMessage(
            @PathParam("id") @Parameter(description = "Conversation identifier", example = "1") Long id,
            @Valid SendMessageRequest request) {
        Message m = messageService.sendMessage(id, identity.getPrincipal().getName(), request);
        return Response.status(Response.Status.CREATED)
                .entity(MessageResponse.from(m))
                .build();
    }

}
