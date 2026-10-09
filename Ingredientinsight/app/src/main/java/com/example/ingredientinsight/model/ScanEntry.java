package com.example.ingredientinsight.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "scan_entries",
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = CASCADE
        ),
        indices = {
                @Index("productId")
        }
)
public class ScanEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long productId;

    public long timestamp;

    public String source;
}
