package com.spiceroad.repository;

import com.spiceroad.entity.Dish;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DishRepository implements PanacheRepository<Dish> {
    
}