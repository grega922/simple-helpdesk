package org.acme.helpdesk.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.helpdesk.entity.Rooms;
import org.acme.helpdesk.dto.CreateRoomRequest;
import org.acme.helpdesk.dto.ErrorResponse;
import org.acme.helpdesk.dto.RoomResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("OPERATOR")
@Tag(name = "Room Management", description = "Endpoints for managing helpdesk rooms")
@SecurityRequirement(name = "BearerAuth")
public class RoomApi {

    @GET
    @Operation(summary = "List all rooms", description = "Returns all available helpdesk rooms")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of rooms returned successfully"),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires OPERATOR)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<RoomResponse> getAllRooms() {
        return Rooms.<Rooms>listAll().stream()
                .map(RoomResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get room by room ID", description = "Returns a single room by its unique identifier")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Room details returned",
            content = @Content(schema = @Schema(implementation = RoomResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires OPERATOR)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "404", description = "Room not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    public RoomResponse getRoom(
        @PathParam("id") @Parameter(description = "Room identifier", example = "1") Long id) {
        Rooms room = Rooms.findById(id);
        if (room == null) {
            throw new NotFoundException("Room with id " + id + " not found");
        }
        return RoomResponse.from(room);
    }

    @POST
    @Transactional
    @Path("/new")
    @Operation(summary = "Create a new room", description = "Create a new helpdesk room that users can start conversations in")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Room created successfully",
            content = @Content(schema = @Schema(implementation = RoomResponse.class))),
        @APIResponse(responseCode = "400", description = "Validation error or room name already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "401", description = "Missing or invalid JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @APIResponse(responseCode = "403", description = "Insufficient role (requires OPERATOR)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createRoom(@Valid CreateRoomRequest request) {
        if (Rooms.findByName(request.name) != null) {
            throw new BadRequestException("Room '" + request.name + "' already exists");
        }

        Rooms room = new Rooms();
        room.name = request.name;
        room.description = request.description;
        room.persist();

        return Response.status(Response.Status.CREATED)
                .entity(RoomResponse.from(room))
                .build();
    }
}
