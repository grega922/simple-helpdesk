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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@Path("/v1/operator/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("OPERATOR")
@Tag(name = "Operator Conversations", description = "Endpoints for helpdesk operators (web browser)")
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
    public List<ConversationResponse> waitingConversations() {
        return conversationService.getWaitingConversations().stream()
                .map(ConversationResponse::from)
                .toList();

    }

    @GET
    @Path("/active")
    @Operation(summary = "Active current operator conversations",
            description = "List all active conversations assigned to the current operator")
    public List<ConversationResponse> activeConversations() {
        return conversationService.getOperatorActiveConversations(identity.getPrincipal().getName()).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @POST
    @Path("/{id}/claim")
    @Operation(summary = "Claim user conversation",
        description = "Claim waiting conversation to start messaging with the user")
    public ConversationResponse claimConversation(@PathParam("id") Long id) {
        Conversation c = conversationService.takeConversation(id, identity.getPrincipal().getName());
        return ConversationResponse.from(c);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get operator conversation", description = "Get claimed conversation details (only assigned to operator)")
    public ConversationResponse getConversation(@PathParam("id") Long id) {
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
    public List<MessageResponse> getMessages(
            @PathParam("id") Long id,
            @QueryParam("since") @Parameter(description = "ISO 8601 timestamp — returns only messages newer than this value (for polling)")
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
    public Response sendMessage(@PathParam("id") Long id, @Valid SendMessageRequest request) {
        Message m = messageService.sendMessage(id, identity.getPrincipal().getName(), request);
        return Response.status(Response.Status.CREATED)
                .entity(MessageResponse.from(m))
                .build();
    }
}
