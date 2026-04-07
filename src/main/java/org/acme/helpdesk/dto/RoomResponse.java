package org.acme.helpdesk.dto;

import org.acme.helpdesk.entity.Rooms;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Room details")
public class RoomResponse {
    
    @Schema(description = "Unique room ID", example = "1")
    public Long id;

    @Schema(description = "Unique room name", example = "TEHNIKA")
    public String name;

    @Schema(description = "Room description", example = "Tehnična pomoč uporabnikom")
    public String description;

    public RoomResponse(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static RoomResponse from(Rooms room) {
        return new RoomResponse(room.id, room.name, room.description);
    }
}
