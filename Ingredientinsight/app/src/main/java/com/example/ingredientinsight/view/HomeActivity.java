package com.example.ingredientinsight.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.ingredientinsight.R;
import com.example.ingredientinsight.data.AppDatabase;
import com.example.ingredientinsight.data.ProductDao;
import com.example.ingredientinsight.model.Product;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private TextView textEmpty;
    private ProductAdapter adapter;
    private ProductDao productDao;
    private TextView tabAll, tabBetter, tabModeration, tabLessHealthy;
    private String currentFilter = "All";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        textEmpty = findViewById(R.id.textEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddProduct);

        tabAll = findViewById(R.id.tabAll);
        tabBetter = findViewById(R.id.tabBetter);
        tabModeration = findViewById(R.id.tabModeration);
        tabLessHealthy = findViewById(R.id.tabLessHealthy);

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id);
            startActivity(intent);
        });

        recyclerProducts.setAdapter(adapter);

        productDao = AppDatabase.getInstance(getApplicationContext()).productDao();

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        setupFilterListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void setupFilterListeners() {
        tabAll.setOnClickListener(v -> applyFilter("All"));
        tabBetter.setOnClickListener(v -> applyFilter("Better choice"));
        tabModeration.setOnClickListener(v -> applyFilter("Moderation"));
        tabLessHealthy.setOnClickListener(v -> applyFilter("Less healthy"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateTabStyles();
        loadProducts();
    }

    private void updateTabStyles() {
        resetTabStyles();
        TextView selectedTab;
        switch (currentFilter) {
            case "Better choice":
                selectedTab = tabBetter;
                break;
            case "Moderation":
                selectedTab = tabModeration;
                break;
            case "Less healthy":
                selectedTab = tabLessHealthy;
                break;
            default:
                selectedTab = tabAll;
                break;
        }
        selectedTab.setBackgroundResource(R.drawable.filter_tab_selected);
        selectedTab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void resetTabStyles() {
        TextView[] tabs = {tabAll, tabBetter, tabModeration, tabLessHealthy};
        for (TextView tab : tabs) {
            tab.setBackgroundResource(R.drawable.filter_tab_unselected);
            tab.setTextColor(ContextCompat.getColor(this, R.color.ebony));
        }
    }

    //Load products from database and update ui
    private void loadProducts() {
        new Thread(() -> {
            final List<Product> products;
            if ("All".equals(currentFilter)) {
                products = productDao.getAllProducts();
            } else {
                products = productDao.getProductsByHealthLabel(currentFilter);
            }
            runOnUiThread(() -> {
                adapter.setProducts(products);
                if (products.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                    recyclerProducts.setVisibility(View.GONE);
                } else {
                    textEmpty.setVisibility(View.GONE);
                    recyclerProducts.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
