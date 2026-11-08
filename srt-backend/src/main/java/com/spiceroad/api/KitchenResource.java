package com.spiceroad.api;

import com.spiceroad.service.KitchenService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static com.spiceroad.config.RoleConstants.*;

@Path("/srt/v1/kitchen/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KitchenResource {

    @Inject
    KitchenService kitchenService;

    /** Open tickets for the kitchen screen; {@code completed=true} lists finished ones instead. */
    @GET
    @RolesAllowed({ADMIN, MANAGER, STAFF})
    public Response getOrders(@QueryParam("completed") boolean completed,
                              @QueryParam("date") String date,
                              @QueryParam("limit") Integer limit,
                              @QueryParam("after") String after) {
        return Response.ok(kitchenService.getKitchenOrders(completed, date, limit, after)).build();
    }

    /** Tick, clear or recall one item. Body: {"from":"PENDING","to":"TICKED"}. */
    @PUT
    @Path("/{sk}/items/{lineId}/status")
    @RolesAllowed({ADMIN, MANAGER, STAFF})
    public Response setItemStatus(@PathParam("sk") String sk,
                                  @PathParam("lineId") String lineId,
                                  Map<String, String> body) {
        if (body == null) {
            throw new BadRequestException("from and to are required");
        }
        return Response.ok(kitchenService.setItemStatus(sk, lineId, body.get("from"), body.get("to"))).build();
    }
}
