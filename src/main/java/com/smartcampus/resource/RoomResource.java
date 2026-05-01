package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "Room 'id' is required.");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "Room 'name' is required.");
        }
        if (room.getCapacity() <= 0) {
            return error(Response.Status.BAD_REQUEST, "Room 'capacity' must be greater than 0.");
        }
        if (store.getRoom(room.getId()) != null) {
            return error(Response.Status.CONFLICT, "Room with ID '" + room.getId() + "' already exists.");
        }

        store.addRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return error(Response.Status.NOT_FOUND, "Room not found: " + roomId);
        }
        return Response.ok(room).build();
    }

    // blocked if the room still has sensors - throws RoomNotEmptyException -> 409
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return error(Response.Status.NOT_FOUND, "Room not found: " + roomId);
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        store.deleteRoom(roomId);
        Map<String, String> msg = new HashMap<>();
        msg.put("message", "Room '" + roomId + "' deleted successfully.");
        return Response.ok(msg).build();
    }

    private Response error(Response.Status status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
