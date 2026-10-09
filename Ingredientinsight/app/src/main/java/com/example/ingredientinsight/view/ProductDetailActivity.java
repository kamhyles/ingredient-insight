package com.example.ingredientinsight.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ingredientinsight.R;
import com.example.ingredientinsight.data.AppDatabase;
import com.example.ingredientinsight.data.ProductDao;
import com.example.ingredientinsight.model.Product;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private TextView textName;
    private TextView textHealthLabel;
    private TextView textScore;
    private TextView textIngredients;
    private TextView textExplanation;
    private Button buttonEdit;
    private Button buttonDelete;

    private ProductDao productDao;
    private Product product;

    private long productId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ingredient Insight");



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textName        = findViewById(R.id.textDetailName);
        textHealthLabel = findViewById(R.id.textDetailHealthLabel);
        textScore       = findViewById(R.id.textDetailScore);
        textIngredients = findViewById(R.id.textDetailIngredients);
        textExplanation = findViewById(R.id.textDetailExplanation);
        buttonEdit      = findViewById(R.id.buttonEditProduct);
        buttonDelete    = findViewById(R.id.buttonDeleteProduct);

        productDao = AppDatabase.getInstance(getApplicationContext()).productDao();

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(productId);

        buttonDelete.setOnClickListener(v -> {
            if (product != null) {
                deleteProduct();
            }
        });

        buttonEdit.setOnClickListener(v -> {
            if (product != null) {
                Intent intent = new Intent(ProductDetailActivity.this, EditProductActivity.class);
                intent.putExtra(EditProductActivity.EXTRA_PRODUCT_ID, product.id);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productId != -1) {
            loadProduct(productId);
        }
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
                    bindProductToViews();
                }
            });
        }).start();
    }

    //connect the data to the views
    private void bindProductToViews() {
        textName.setText(product.name);
        textHealthLabel.setText(product.healthLabel);


        textScore.setText("Score: " + product.healthScore);

        //pill tint color logic
        String label = product.healthLabel != null ? product.healthLabel : "";
        int badgeColor;

        if ("Better choice".equalsIgnoreCase(label)) {
            badgeColor = 0xFF4CAF50;
        } else if ("Moderation".equalsIgnoreCase(label)) {
            badgeColor = 0xFFFFC107;
        } else if ("Less healthy".equalsIgnoreCase(label)) {
            badgeColor = 0xFFF44336;
        } else {
            badgeColor = 0xFF9E9E9E;
        }

        Drawable bg = textHealthLabel.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg).setColor(badgeColor);
        } else {

            textHealthLabel.setBackgroundTintList(ColorStateList.valueOf(badgeColor));
        }

        textIngredients.setText(formatIngredients(product.ingredientsText));
        textExplanation.setText(product.analysisExplanation);
    }

    //format ingredients for display
    private String formatIngredients(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String[] ingredients = raw.replace("\n", " ").split("[,;]");

        return Arrays.stream(ingredients)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "• " + s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining("\n"));
    }

    private void deleteProduct() {
        new Thread(() -> {
            productDao.delete(product);
            runOnUiThread(() -> {
                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                finish(); //back to home
            });
        }).start();
    }
}
