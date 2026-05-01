package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "FORBIDDEN");
        body.put("message", e.getMessage());
        body.put("sensorId", e.getSensorId());
        body.put("status", e.getCurrentStatus());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
