package com.spiceroad.api;

import com.spiceroad.model.SRTOrder;
import com.spiceroad.service.CounterOrderRequest;
import com.spiceroad.service.CounterService;
import jakarta.annotation.security.RolesAllowed;
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

import java.util.List;

import static com.spiceroad.config.RoleConstants.*;

/** Counter and kiosk order routes. All of them need a signed-in admin, manager or staff user. */
@Path("/srt/v1/counter/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ADMIN, MANAGER, STAFF})
public class CounterResource {

    /** Body of the items update: the version the screen saw plus the new lines (ids and quantities). */
    public record ItemsUpdate(Integer orderVersion, List<SRTOrder.OrderItem> items) {
    }

    /** Body of the send request: the version the screen saw. */
    public record VersionRequest(Integer orderVersion) {
    }

    @Inject
    CounterService counterService;

    @POST
    public Response create(CounterOrderRequest request) {
        return Response.status(Response.Status.CREATED).entity(counterService.create(request)).build();
    }

    /** {@code state=open} (default) or {@code state=past[&date=yyyy-MM-dd]}. */
    @GET
    public Response list(@QueryParam("state") String state,
                         @QueryParam("date") String date,
                         @QueryParam("limit") Integer limit,
                         @QueryParam("after") String after) {
        return Response.ok(counterService.list(state, date, limit, after)).build();
    }

    @GET
    @Path("/{sk}")
    public Response get(@PathParam("sk") String sk) {
        return Response.ok(counterService.get(sk)).build();
    }

    @PUT
    @Path("/{sk}/items")
    public Response replaceItems(@PathParam("sk") String sk, ItemsUpdate body) {
        if (body == null) {
            throw new BadRequestException("orderVersion and items are required");
        }
        return Response.ok(counterService.replaceItems(sk, body.orderVersion(), body.items())).build();
    }

    @POST
    @Path("/{sk}/hold")
    public Response hold(@PathParam("sk") String sk) {
        return Response.ok(counterService.hold(sk)).build();
    }

    @POST
    @Path("/{sk}/resume")
    public Response resume(@PathParam("sk") String sk) {
        return Response.ok(counterService.resume(sk)).build();
    }

    @POST
    @Path("/{sk}/send")
    public Response send(@PathParam("sk") String sk, VersionRequest body) {
        if (body == null) {
            throw new BadRequestException("orderVersion is required");
        }
        return Response.ok(counterService.send(sk, body.orderVersion())).build();
    }

    @POST
    @Path("/{sk}/discard")
    public Response discard(@PathParam("sk") String sk) {
        return Response.ok(counterService.discard(sk)).build();
    }

    @POST
    @Path("/{sk}/pickup")
    public Response pickUp(@PathParam("sk") String sk) {
        return Response.ok(counterService.pickUp(sk)).build();
    }
}
