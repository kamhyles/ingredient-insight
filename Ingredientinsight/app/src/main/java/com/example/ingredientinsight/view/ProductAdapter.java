package com.example.ingredientinsight.view;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientinsight.R;
import com.example.ingredientinsight.model.Product;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> products = new ArrayList<>();
    private final OnProductClickListener clickListener;

    public ProductAdapter(OnProductClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, clickListener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final View viewHealthIndicator;
        private final TextView textProductName;
        private final TextView textHealthLabel;
        private final TextView textScanDate;
        private final TextView textHealthBadge;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            viewHealthIndicator = itemView.findViewById(R.id.viewHealthIndicator);
            textProductName     = itemView.findViewById(R.id.textProductName);
            textHealthLabel     = itemView.findViewById(R.id.textHealthLabel);
            textScanDate        = itemView.findViewById(R.id.textScanDate);
            textHealthBadge     = itemView.findViewById(R.id.textHealthBadge);
        }

        void bind(final Product product, final OnProductClickListener clickListener) {
            textProductName.setText(product.name);
            textHealthLabel.setText(product.healthLabel);
            textHealthBadge.setText(product.healthLabel);

            //display the date
            Date date = new Date(product.timestamp);
            String formattedDate =
                    DateFormat.getDateInstance(DateFormat.SHORT).format(date);
            textScanDate.setText("Scanned on " + formattedDate);


            int indicatorColor;
            int badgeColor;

            String label = product.healthLabel != null
                    ? product.healthLabel
                    : "";

            if ("Better choice".equalsIgnoreCase(label)) {
                indicatorColor = 0xFF4CAF50;
                badgeColor     = 0xFF4CAF50;
            } else if ("Moderation".equalsIgnoreCase(label)) {
                indicatorColor = 0xFFFFC107;
                badgeColor     = 0xFFFFC107;
            } else if ("Less healthy".equalsIgnoreCase(label)) {
                indicatorColor = 0xFFF44336;
                badgeColor     = 0xFFF44336;
            } else {
                indicatorColor = 0xFF9E9E9E;
                badgeColor     = 0xFF9E9E9E;
            }

            //color of dot
            viewHealthIndicator.setBackgroundColor(indicatorColor);

            //tint for badge
            Drawable bg = textHealthBadge.getBackground();
            if (bg instanceof GradientDrawable) {
                ((GradientDrawable) bg).setColor(badgeColor);
            } else {
                textHealthBadge.setBackgroundColor(badgeColor);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });
        }
    }
}
