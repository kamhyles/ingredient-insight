package com.example.ingredientinsight.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ingredientinsight.model.ScanEntry;

import java.util.List;

@Dao
public interface ScanEntryDao {

    @Insert
    long insert(ScanEntry entry);

    @Delete
    void delete(ScanEntry entry);

    @Query("SELECT * FROM scan_entries WHERE productId = :productId ORDER BY timestamp DESC")
    List<ScanEntry> getScansForProduct(long productId);

    @Query("SELECT COUNT(*) FROM scan_entries WHERE productId = :productId")
    int getScanCountForProduct(long productId);
}
