package com.spiceroad.service;

import java.util.List;

import com.spiceroad.entity.Dish;
import com.spiceroad.repository.DishRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DishService {

    @Inject
    DishRepository dishRepository;

    public List<Dish> getAllDishes() {
        return dishRepository.listAll();
    }

    public List<Dish> getDishesByCategory(String category) {
        return dishRepository.list("category", category);
    }

    public Dish getDish(Long id) {
        return dishRepository.findById(id);
    }

    @Transactional
    public Dish createDish(Dish dish) {
        dish.id = null;
        dishRepository.persist(dish);
        return dish;
    }

    @Transactional
    public Dish updateDish(Long id, Dish updated) {
        Dish dish = dishRepository.findById(id);
        if (dish == null) return null;

        dish.name = updated.name;
        dish.description = updated.description;
        dish.price = updated.price;
        dish.category = updated.category;
        dish.imageUrl = updated.imageUrl;

        return dish;
    }

    @Transactional
    public boolean deleteDish(Long id) {
        return dishRepository.deleteById(id);
    }

}