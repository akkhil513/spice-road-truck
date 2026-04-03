package com.spiceroad.api;

import com.spiceroad.model.SRTDish;
import com.spiceroad.service.DishService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/srt/v1/dishes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishService dishService;

    @GET
    public Response getDishes(@QueryParam("category") String category) {
        List<SRTDish> dishes;
        if (category != null && !category.trim().isEmpty()) {
            dishes = dishService.getDishesByCategory(category);
        } else {
            dishes = dishService.getAllDishes();
        }
        return Response.ok(dishes).build();
    }

    @GET
    @Path("/{id}")
    public Response getDishById(@PathParam("id") String id) {
        SRTDish dish = dishService.getDishById(id);
        return Response.ok(dish).build();
    }

    @POST
    public Response createDish(SRTDish dish) {
        SRTDish created = dishService.createDish(dish);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateDish(@PathParam("id") String id, SRTDish dish) {
        SRTDish updated = dishService.updateDish(id, dish);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteDish(@PathParam("id") String id) {
        dishService.deleteDish(id);
        return Response.noContent().build();
    }
}