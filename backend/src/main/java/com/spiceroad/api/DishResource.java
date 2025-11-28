package com.spiceroad.api;

import java.util.List;
import java.util.Optional;

import com.spiceroad.entity.Dish;
import com.spiceroad.service.DishService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/srt/v1/dishes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishService dishService;

    @GET
    public List<Dish> getDishes() {
        return dishService.getAllDishes();
    }

    @GET
    @Path("/{id}")
    public Dish getDishById(@PathParam("id") Long id) {
        Dish dish = dishService.getDish(id);
        if (dish == null) {
            throw new RuntimeException("Dish not found with id: " + id);  
        }
        return dish;
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
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteDish(@PathParam("id") Long id) {
        boolean deleted = dishService.deleteDish(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
