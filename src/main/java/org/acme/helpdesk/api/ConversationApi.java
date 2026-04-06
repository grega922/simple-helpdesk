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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.security.identity.SecurityIdentity;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;


@Path("/v1/conversations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
@Tag(name = "User Conversations", description = "Endpoints for mobile app users")
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
    public List<ConversationResponse> myConversations() {
        return conversationService.getUserConversations(identity.getPrincipal().getName()).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @POST
    @Path("/new")
    @Operation(summary = "Create new conversation",
            description = "Create a new helpdesk conversation in a selected room with an initial message")
    public Response createConversation(@Valid CreateConversationRequest request) {
        
        Conversation c = conversationService.createConversation(identity.getPrincipal().getName(), request);
        return Response.status(Response.Status.CREATED)
                .entity(ConversationResponse.from(c))
                .build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get specific conversation", description = "Get conversation details by ID")
    public ConversationResponse getConversation(@PathParam("id") Long id) {
        Conversation c = conversationService.getConversation(id);
        User currentUser = User.findByUsername(identity.getPrincipal().getName());
        if (c == null || !c.user.id.equals(currentUser.id)) {
            throw new ForbiddenException("Not"+ currentUser.username + "conversation");
        }
        return ConversationResponse.from(c);
    }

    @GET
    @Path("/{id}/messages")
    @Operation(summary = "Get messages in conversation", description = "List all messages in a conversation")
    public List<MessageResponse> getMessages(
            @PathParam("id") Long id,
            @QueryParam("since") @Parameter(description = "ISO 8601 timestamp — returns only messages newer than this value (for polling)")
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
    public Response sendMessage(@PathParam("id") Long id, @Valid SendMessageRequest request) {
        Message m = messageService.sendMessage(id, identity.getPrincipal().getName(), request);
        return Response.status(Response.Status.CREATED)
                .entity(MessageResponse.from(m))
                .build();
    }

}
