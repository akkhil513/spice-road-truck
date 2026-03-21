package com.spiceroad.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "dish")
public class Dish extends PanacheEntity {

    public String name;

    @Column(length = 1000)
    public String description;

    public double price;

    public String category;

    public String imageUrl;

}