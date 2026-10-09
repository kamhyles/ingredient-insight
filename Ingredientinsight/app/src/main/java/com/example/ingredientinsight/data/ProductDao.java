package com.example.ingredientinsight.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ingredientinsight.model.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE healthLabel = :healthLabel ORDER BY timestamp DESC")
    List<Product> getProductsByHealthLabel(String healthLabel);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(long id);
}
