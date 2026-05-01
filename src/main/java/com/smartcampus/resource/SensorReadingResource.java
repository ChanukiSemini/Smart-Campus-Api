package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// No @Path here - this class is reached via the sub-resource locator in SensorResource
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return error(Response.Status.NOT_FOUND, "Sensor not found: " + sensorId);
        }
        List<SensorReading> history = store.getReadings(sensorId);
        return Response.ok(history).build();
    }

    // blocked if sensor is MAINTENANCE or OFFLINE - throws 403
    // also updates the parent sensor's currentValue as a side effect
    @POST
    public Response addReading(SensorReading incoming) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return error(Response.Status.NOT_FOUND, "Sensor not found: " + sensorId);
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) ||
            "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        SensorReading saved = new SensorReading(incoming.getValue());
        store.addReading(sensorId, saved);
        sensor.setCurrentValue(saved.getValue()); // keep the snapshot up to date
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    private Response error(Response.Status status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
