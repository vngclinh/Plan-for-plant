package com.example.planforplant.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.example.planforplant.R;
import com.example.planforplant.model.PlantResponse;
import com.example.planforplant.model.Result;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

public class DetailActivity extends AppCompatActivity {
    private ImageView plantImage;
    private TextView tvPlantName, tvOverview, tvFamily, tvGenus, tvSpecies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        plantImage = findViewById(R.id.plantImage);
        tvPlantName = findViewById(R.id.tvPlantName);
        tvOverview = findViewById(R.id.tvOverview);
        tvFamily = findViewById(R.id.tvFamily);
        tvGenus = findViewById(R.id.tvGenus);
        tvSpecies = findViewById(R.id.tvSpecies);

        // hiển thị ảnh (giữ code rotate như bạn đã có)
        handleImage();

        // nhận JSON từ intent
        String json = getIntent().getStringExtra("plantResponseJson");
        if (json != null) {
            PlantResponse response = new Gson().fromJson(json, PlantResponse.class);
            bindPlantData(response);
        }
    }

    private void bindPlantData(PlantResponse response) {
        if (response == null) return;

        // tên cây (bestMatch)
        tvPlantName.setText(response.bestMatch != null ? response.bestMatch : "Unknown");

        if (response.results != null && !response.results.isEmpty()) {
            Result first = response.results.get(0);

            if (first.species != null) {
                tvSpecies.setText(first.species.scientificName);
                if (first.species.family != null)
                    tvFamily.setText(first.species.family.scientificName);
                if (first.species.genus != null)
                    tvGenus.setText(first.species.genus.scientificName);

                if (first.species.commonNames != null && !first.species.commonNames.isEmpty()) {
                    tvOverview.setText("Common names: hiển thị tạm common name" + String.join(", ", first.species.commonNames));
                } else {
                    tvOverview.setText("No common names available");
                }
            }
        }
    }

    private void handleImage() {
        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("imageUri");
        if (imageUriString == null) return;

        Uri imageUri = Uri.parse(imageUriString);
        try {
            Bitmap bitmap;
            if ("file".equals(imageUri.getScheme())) {
                String path = imageUri.getPath();
                bitmap = BitmapFactory.decodeFile(path);
                ExifInterface ei = new ExifInterface(path);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotateBitmap(bitmap, orientation);
            } else {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                ExifInterface ei = new ExifInterface(getContentResolver().openInputStream(imageUri));
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotateBitmap(bitmap, orientation);
            }
            plantImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270); break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
