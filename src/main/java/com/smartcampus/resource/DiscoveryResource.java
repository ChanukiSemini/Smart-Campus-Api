package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        Map<String, Object> info = new HashMap<>();
        info.put("api", "Smart Campus API");
        info.put("version", "1.0");
        info.put("contact", "admin@smartcampus.ac.uk");
        info.put("status", "operational");

        // HATEOAS links so clients know where the resources are
        Map<String, String> links = new HashMap<>();
        links.put("self",    base + "/api/v1");
        links.put("rooms",   base + "/api/v1/rooms");
        links.put("sensors", base + "/api/v1/sensors");
        info.put("_links", links);

        return Response.ok(info).build();
    }

    // hit this to test the global 500 exception mapper
    @GET
    @Path("/test-error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError() {
        throw new RuntimeException("Deliberate test error for the global safety net");
    }
}
