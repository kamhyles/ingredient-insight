package com.example.ingredientinsight.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ingredientinsight.R;
import com.example.ingredientinsight.data.AppDatabase;
import com.example.ingredientinsight.data.ProductDao;
import com.example.ingredientinsight.model.Product;
import com.example.ingredientinsight.model.IngredientAnalyzer;


public class EditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private EditText editProductName;
    private EditText editIngredients;
    private Button buttonUpdate;

    private ProductDao productDao;
    private Product product;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        editProductName = findViewById(R.id.editProductName);
        editIngredients = findViewById(R.id.editIngredients);
        buttonUpdate = findViewById(R.id.buttonUpdateProduct);

        productDao = AppDatabase.getInstance(getApplicationContext()).productDao();

        long productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(productId);

        buttonUpdate.setOnClickListener(v -> updateProduct());
    }

    private void loadProduct(long productId) {
        new Thread(() -> {
            Product loaded = productDao.getProductById(productId);
            runOnUiThread(() -> {
                if (loaded == null) {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    product = loaded;
                    editProductName.setText(product.name);
                    editIngredients.setText(product.ingredientsText);
                }
            });
        }).start();
    }

    private void updateProduct() {
        if (product == null) return;

        String newName = editProductName.getText().toString().trim();
        String newIngredients = editIngredients.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Please enter a product name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newIngredients)) {
            Toast.makeText(this, "Please enter ingredients", Toast.LENGTH_SHORT).show();
            return;
        }

        //update fields
        product.name = newName;
        product.ingredientsText = newIngredients;

        //Re run the analyzer with the new ingredients
        IngredientAnalyzer.AnalysisResult analysisResult =
                IngredientAnalyzer.analyze(newIngredients);

        product.healthScore = analysisResult.healthScore;
        product.healthLabel = analysisResult.healthLabel;
        product.analysisExplanation = analysisResult.explanation;

        new Thread(() -> {
            productDao.update(product);
            runOnUiThread(() -> {
                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                finish(); //return
            });
        }).start();
    }

}
