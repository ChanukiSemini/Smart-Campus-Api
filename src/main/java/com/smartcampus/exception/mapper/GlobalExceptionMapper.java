package com.smartcampus.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// catches anything that slips past the specific mappers above
// logs the real error privately, returns a safe generic message to the client
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        LOG.log(Level.SEVERE, "Unhandled exception", e);
        Map<String, String> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", "An unexpected error occurred. Please contact support.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
