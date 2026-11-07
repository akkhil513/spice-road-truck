package com.spiceroad.api;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/srt/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    public Response placeOrder(SRTOrder order) {
        return Response.ok(orderService.placeOrder(order)).build();
    }

    /** One page of a day's orders, newest first. Defaults: today, 100 per page. */
    @GET
    public Response getOrders(@QueryParam("date") String date,
                              @QueryParam("limit") Integer limit,
                              @QueryParam("after") String after) {
        return Response.ok(orderService.getOrders(date, limit, after)).build();
    }

    @PUT
    @Path("/{sk}/status")
    public Response updateStatus(@PathParam("sk") String sk, Map<String, String> body) {
        if (body == null) {
            throw new BadRequestException("Status is required");
        }
        return Response.ok(orderService.updateStatus(sk, body.get("status"))).build();
    }
}
