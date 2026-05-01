package com.smartcampus.exception.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "UNPROCESSABLE_ENTITY");
        body.put("message", e.getMessage());
        body.put("hint", "Check that the referenced roomId exists before creating the sensor.");
        return Response.status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
