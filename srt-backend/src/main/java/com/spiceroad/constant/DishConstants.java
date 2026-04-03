package com.spiceroad.constant;

public class DishConstants {

    public static final String TABLE_NAME = System.getenv("TABLE_NAME") != null ?
            System.getenv("TABLE_NAME") :
            "srt-dishes-dev";
}
