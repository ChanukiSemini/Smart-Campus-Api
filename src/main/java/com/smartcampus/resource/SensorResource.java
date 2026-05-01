package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // optional ?type= filter e.g. GET /sensors?type=CO2
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.trim().isEmpty()) {
            list = list.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return error(Response.Status.NOT_FOUND, "Sensor not found: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    // verifies roomId exists before creating the sensor - throws 422 if not
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "Sensor 'id' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "Sensor 'roomId' is required.");
        }
        if (store.getSensor(sensor.getId()) != null) {
            return error(Response.Status.CONFLICT, "Sensor '" + sensor.getId() + "' already exists.");
        }

        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Room '" + sensor.getRoomId() + "' does not exist. Cannot register sensor."
            );
        }

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.addSensor(sensor);
        room.getSensorIds().add(sensor.getId());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    // sub-resource locator - hands off to SensorReadingResource for /readings paths
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Response error(Response.Status status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
