package com.spiceroad.service;

import com.spiceroad.model.SRTDish;
import com.spiceroad.repository.SRTDishRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class DishService {

    @Inject
    SRTDishRepository dishRepository;

    public List<SRTDish> getAllDishes() {
        List<SRTDish> dishes = dishRepository.getAllDishes();
        if (dishes == null || dishes.isEmpty()) {
            throw new NotFoundException("No dishes found");
        }
        return dishes;
    }

    public List<SRTDish> getDishesByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new BadRequestException("Category cannot be empty");
        }
        List<SRTDish> dishes = dishRepository.getDishesByCategory(category);
        if (dishes == null || dishes.isEmpty()) {
            throw new NotFoundException("No dishes found for category: " + category);
        }
        return dishes;
    }

    public SRTDish getDishById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Dish ID cannot be empty");
        }
        SRTDish dish = dishRepository.getDishById(id);
        if (dish == null) {
            throw new NotFoundException("Dish not found with id: " + id);
        }
        return dish;
    }

    public SRTDish createDish(SRTDish dish) {
        validateDish(dish);
        return dishRepository.createDish(dish);
    }

    public SRTDish updateDish(String id, SRTDish dish) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Dish ID cannot be empty");
        }
        SRTDish existing = dishRepository.getDishById(id);
        if (existing == null) {
            throw new NotFoundException("Dish not found with id: " + id);
        }
        validateDish(dish);
        return dishRepository.updateDish(id, dish);
    }

    public void deleteDish(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Dish ID cannot be empty");
        }
        SRTDish existing = dishRepository.getDishById(id);
        if (existing == null) {
            throw new NotFoundException("Dish not found with id: " + id);
        }
        dishRepository.deleteDish(id);
    }

    private void validateDish(SRTDish dish) {
        if (dish == null) {
            throw new BadRequestException("Dish cannot be null");
        }
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            throw new BadRequestException("Dish name is required");
        }
        if (dish.getCategory() == null || dish.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Dish category is required");
        }
        if (dish.getPrice() <= 0) {
            throw new BadRequestException("Dish price must be greater than 0");
        }
        if (dish.getDescription() == null || dish.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Dish description is required");
        }
    }
}