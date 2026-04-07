package org.acme.helpdesk.api;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.helpdesk.dto.ConversationResponse;
import org.acme.helpdesk.dto.MessageResponse;
import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.entity.Message;
import org.acme.helpdesk.service.ConversationService;
import org.acme.helpdesk.service.MessageService;
import org.acme.helpdesk.dto.SendMessageRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.acme.helpdesk.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

@Path("/v1/operator/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("OPERATOR")
@Tag(name = "Operator Conversations", description = "Endpoints for helpdesk operators (web browser)")
@SecurityRequirement(name = "BearerAuth")
public class OperatorApi {

    @Inject
    ConversationService conversationService;

    @Inject
    MessageService messageService;

    @Inject
    SecurityIdentity identity;
    
    @GET
    @Path("/waiting")
    @Operation(summary = "Open/waiting conversations for operator",
            description = "List of all conversations waiting for an operator")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of waiting conversations returned"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires OPERATOR)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ConversationResponse> waitingConversations() {
        return conversationService.getWaitingConversations().stream()
                .map(ConversationResponse::from)
                .toList();

    }

    @GET
    @Path("/active")
    @Operation(summary = "Active operator conversations",
            description = "List all active conversations assigned to the currently authenticated operator")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of active conversations returned"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires OPERATOR)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ConversationResponse> activeConversations() {
        return conversationService.getOperatorActiveConversations(identity.getPrincipal().getName()).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @POST
    @Path("/{id}/claim")
    @Operation(summary = "Claim user conversation",
        description = "Claim waiting conversation to start messaging with the user")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Conversation claimed successfully",
            content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @APIResponse(responseCode = "400", description = "Conversation is not in WAITING status",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ConversationResponse claimConversation(
            @PathParam("id") @Parameter(description = "Conversation identifier for claiming", example = "1") Long id) {
        Conversation c = conversationService.claimConversation(id, identity.getPrincipal().getName());
        return ConversationResponse.from(c);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get operator conversation", description = "Get claimed conversation details (only assigned to operator)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Conversation details returned",
            content = @Content(schema = @Schema(implementation = ConversationResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Conversation is not assigned to the current operator",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ConversationResponse getConversation(
            @PathParam("id") @Parameter(description = "Conversation identifier", example = "1") Long id) {
        Conversation c = conversationService.getConversation(id);
        User currentOperator = User.findByUsername(identity.getPrincipal().getName());
        if (c.operator == null || !c.operator.id.equals(currentOperator.id)) {
            throw new ForbiddenException("Not your conversation");
        }
        return ConversationResponse.from(c);
    }

    @GET
    @Path("/{id}/messages")
    @Operation(summary = "Get user/operator messages",
        description = "List messages in an operator's conversation. Use 'since' for polling.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Messages returned successfully"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Conversation is not assigned to the current operator",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Conversation not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<MessageResponse> getMessages(
            @PathParam("id") @Parameter(description = "Conversation identifier", example = "1") Long id,
            @QueryParam("since") @Parameter(description = "ISO 8601 timestamp — returns only messages newer than this value (for polling)", example = "2025-01-01T10:00:00")
            LocalDateTime since) {
        Conversation c = conversationService.getConversation(id);
        User currentOperator = User.findByUsername(identity.getPrincipal().getName());
        if (c.operator == null || !c.operator.id.equals(currentOperator.id)) {
            throw new ForbiddenException("Not your conversation");
        }
        return messageService.getMessages(id, since).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @POST
    @Path("/{id}/messages")
    @Operation(summary = "Send operator message", description = "Reply to the user in an active conversation")
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
