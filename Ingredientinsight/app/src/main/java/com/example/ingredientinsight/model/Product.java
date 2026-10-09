package com.example.ingredientinsight.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String ingredientsText;

    //overall health score
    public int healthScore;

    //label
    @NonNull
    public String healthLabel;

    //explanation
    public String analysisExplanation;

    //time product was saved
    public long timestamp;

    //image uri
    public String imageUri;
}
