package com.spiceroad.api;

import java.util.List;

import com.spiceroad.entity.Dish;
import com.spiceroad.service.DishService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/srt/v1/dishes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishService dishService;

    @GET
    public List<Dish> getDishes(@QueryParam("category") String category) {
        if (category != null && !category.isBlank()) {
            return dishService.getDishesByCategory(category);
        }
        return dishService.getAllDishes();
    }

    @GET
    @Path("/{id}")
    public Response getDishById(@PathParam("id") Long id) {
        Dish dish = dishService.getDish(id);
        if (dish == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Dish not found with id: " + id + "\"}")
                    .build();
        }
        return Response.ok(dish).build();
    }

    @POST
    public Response addDish(Dish dish) {
        Dish saved = dishService.createDish(dish);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateDish(@PathParam("id") Long id, Dish dish) {
        Dish updatedDish = dishService.updateDish(id, dish);
        if (updatedDish != null) {
            return Response.ok(updatedDish).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteDish(@PathParam("id") Long id) {
        boolean deleted = dishService.deleteDish(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}