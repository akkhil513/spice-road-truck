package com.spiceroad.api;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/srt/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderService orderService;

    @POST
    public Response placeOrder(SRTOrder order) {
        try {
            SRTOrder saved = orderService.placeOrder(order);
            return Response.ok(saved).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    public Response getAllOrders() {
        try {
            List<SRTOrder> orders = orderService.getAllOrders();
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{sk}/status")
    public Response updateStatus(@PathParam("sk") String sk, Map<String, String> body) {
        try {
            SRTOrder updated = orderService.updateStatus(sk, body.get("status"));
            return Response.ok(updated).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }
}