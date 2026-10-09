package com.example.ingredientinsight.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ingredientinsight.R;
import com.example.ingredientinsight.data.AppDatabase;
import com.example.ingredientinsight.data.ProductDao;
import com.example.ingredientinsight.model.Product;
import com.example.ingredientinsight.model.IngredientAnalyzer;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class AddProductActivity extends AppCompatActivity {

    private EditText editProductName;
    private EditText editIngredients;
    private ProductDao productDao;

    private static final int REQUEST_IMAGE_PICK = 1001;

    private TextRecognizer textRecognizer;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String scannedText = result.getData().getStringExtra(CameraActivity.EXTRA_SCANNED_TEXT);
                    if (scannedText != null && !scannedText.isEmpty()) {
                        editIngredients.setText(scannedText);
                        Toast.makeText(this, "Text scanned from camera", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No text was scanned", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Toolbar toolbar = findViewById(R.id.toolbar_add_product);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        editProductName = findViewById(R.id.editProductName);
        editIngredients = findViewById(R.id.editIngredients);
        Button buttonSave = findViewById(R.id.buttonSaveProduct);
        Button buttonScanFromGallery = findViewById(R.id.buttonScanFromGallery);
        Button buttonScanFromCamera = findViewById(R.id.buttonScanFromCamera);

        //Room database
        productDao = AppDatabase.getInstance(getApplicationContext()).productDao();

        //Google ML Kit text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //save button
        buttonSave.setOnClickListener(v -> saveProduct());

        //gallery scan button
        buttonScanFromGallery.setOnClickListener(v -> openImagePicker());

        //camera scan button
        buttonScanFromCamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            cameraLauncher.launch(intent);
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

    //open gallery
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    //get the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                runTextRecognition(imageUri);
            } else {
                Toast.makeText(this, "Could not get image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //text recognition code (from googles ml kit website documentation)
    private void runTextRecognition(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);

            textRecognizer
                    .process(image)
                    .addOnSuccessListener(visionText -> {
                        String allText = visionText.getText();
                        if (allText == null || allText.trim().isEmpty()) {
                            Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
                        } else {
                            editIngredients.setText(allText);
                            Toast.makeText(this, "Text recognized", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to recognize text: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProduct() {
        String name = editProductName.getText().toString().trim();
        String ingredients = editIngredients.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a product name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ingredients)) {
            Toast.makeText(this, "Please enter ingredients", Toast.LENGTH_SHORT).show();
            return;
        }

        //call the analyzer
        IngredientAnalyzer.AnalysisResult analysisResult =
                IngredientAnalyzer.analyze(ingredients);

        Product product = new Product();
        product.name = name;
        product.ingredientsText = ingredients;


        product.healthScore = analysisResult.healthScore;
        product.healthLabel = analysisResult.healthLabel;
        product.analysisExplanation = analysisResult.explanation;
        product.timestamp = System.currentTimeMillis();
        product.imageUri = null;

        new Thread(() -> {
            productDao.insert(product);
            runOnUiThread(() -> {
                Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show();
                finish(); //back to HomeActivity
            });
        }).start();
    }

}
