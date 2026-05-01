package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICT");
        body.put("message", e.getMessage());
        body.put("roomId", e.getRoomId());
        body.put("sensorCount", e.getSensorCount());
        body.put("hint", "Remove or reassign all sensors before deleting the room.");
        return Response.status(Response.Status.CONFLICT)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
