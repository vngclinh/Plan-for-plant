package com.example.planforplant.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.planforplant.R;

public class ImagePreviewDialog {

    /**
     * Hiển thị ảnh toàn màn hình với hiệu ứng zoom nhẹ.
     */
    public static void show(Context context, String imageUrl) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
        );

        ImageView fullImage = dialog.findViewById(R.id.fullImage);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_plant)
                .into(fullImage);

        // click ra ngoài để đóng
        fullImage.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
