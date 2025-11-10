package com.example.planforplant.ui;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ChatApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.noties.markwon.Markwon;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends NavigationBarActivity{
    private static final int PICK_IMAGE_REQUEST = 100;
    private ImageButton btnImage;
    private Uri selectedImageUri;
    private EditText etMessage;
    private boolean isSending = false;

    private ImageButton btnSend;
    private ScrollView chatScroll;
    private LinearLayout chatContainer;

    private ChatApi chatApi;
    private Markwon markwon;
    private LinearLayout previewLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etMessage = findViewById(R.id.etMessage);
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendEnabled();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        chatScroll = findViewById(R.id.chatScroll);
        chatContainer = findViewById(R.id.chatContainer);

        markwon = Markwon.create(this);
        chatApi = ApiClient.getLocalClient(this).create(ChatApi.class);

        // 🟢 Chọn ảnh từ thư viện
        btnImage.setOnClickListener(v -> openGallery());

        // 🟢 Gửi tin nhắn text như cũ
        btnSend.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                sendImageMessage(selectedImageUri);
            } else {
                sendTextMessage();
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void updateSendEnabled() {
        boolean hasText = etMessage.getText().toString().trim().length() > 0;
        boolean enabled = hasText && !isSending;
        btnSend.setEnabled(enabled);

//        btnSend.setImageAlpha(enabled ? 255 : 120);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                addImagePreview(selectedImageUri);
            }
        }
    }
    private ImageView previewImageView; // thêm ở đầu class

    private void addImagePreview(Uri uri) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        previewLayout = layout;

        previewImageView = new ImageView(this);
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewImageView.setImageURI(uri);
        previewImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(220)
        ));
        previewImageView.setBackground(getDrawable(R.drawable.bg_chat_user));

        Button cancelBtn = new Button(this);
        cancelBtn.setText("❌ Hủy ảnh này");
        cancelBtn.setOnClickListener(v -> {
            chatContainer.removeView(layout);
            selectedImageUri = null;
            previewImageView = null;
        });

        layout.addView(previewImageView);
        layout.addView(cancelBtn);
        chatContainer.addView(layout);
        scrollToBottom();
    }


    private void sendTextMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() || isSending) return;

        isSending = true;
        updateSendEnabled();

        addUserBubble(text);
        etMessage.setText("");
        View typingView = addBotTyping();

        RequestBody messagePart = RequestBody.create(MediaType.parse("text/plain"), text);

        chatApi.sendMessage(messagePart).enqueue(new Callback<String>() {
            @Override public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                removeView(typingView);
                addBotBubbleMarkdown(response.isSuccessful() && response.body()!=null
                        ? response.body()
                        : "**⚠️ Lỗi:** " + response.code() + " - " + response.message());
                isSending = false;
                updateSendEnabled();
                scrollToBottom();
            }

            @Override public void onFailure(Call<String> call, Throwable t) {
                removeView(typingView);
                addBotBubbleMarkdown("**⚠️ Mạng lỗi:** " + t.getMessage());
                isSending = false;
                updateSendEnabled();
                scrollToBottom();
            }
        });
    }

    private void sendImageMessage(Uri imageUri) {
        try {
            File file = new File(getCacheDir(), "upload.jpg");
            try (InputStream in = getContentResolver().openInputStream(imageUri);
                 OutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            if (!etMessage.getText().toString().trim().isEmpty()) {
                addUserBubble(etMessage.getText().toString().trim());
            }
            String userText = etMessage.getText().toString().trim();
            if (userText.isEmpty()) userText = "Phân tích hình ảnh này";

            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody reqFile = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
            RequestBody message = RequestBody.create(MediaType.parse("text/plain"), userText);

            View typingView = addBotTyping();

            chatApi.sendImageMessage(message, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    removeView(typingView);
                    addBotBubbleMarkdown(response.isSuccessful() && response.body()!=null
                            ? response.body()
                            : "**⚠️ Lỗi:** " + response.code() + " - " + response.message());
                    scrollToBottom();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    removeView(typingView);
                    addBotBubbleMarkdown("**⚠️ Lỗi gửi ảnh:** " + t.getMessage());
                    scrollToBottom();
                }
            });

            etMessage.setText("");
            selectedImageUri = null;
            if (previewLayout != null) {
                chatContainer.removeView(previewLayout);
                previewLayout = null;
            }
            previewImageView = null;

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserBubble(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(6), dp(6), dp(6)); // chừa mép màn

        TextView tv = new TextView(this);
        tv.setBackground(getDrawable(R.drawable.bg_chat_user));
        tv.setTextColor(getColor(R.color.white));
        tv.setTextSize(16);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        tv.setIncludeFontPadding(true); // tránh bị cắt mép trên/dưới
        tv.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
        tv.setHyphenationFrequency(android.text.Layout.HYPHENATION_FREQUENCY_NORMAL);
        tv.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.72f)); // ~72% màn
        tv.setText(text);

        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvLp.setMargins(0, 0, dp(8), 0); // cách avatar
        tv.setLayoutParams(tvLp);

        ImageView avatar = new ImageView(this);
        LinearLayout.LayoutParams avaLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        avatar.setLayoutParams(avaLp);
        avatar.setImageResource(R.drawable.ic_user);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);

        row.addView(tv);
        row.addView(avatar);
        chatContainer.addView(row);
        scrollToBottom();
    }
    private void addUserImageBubble(Uri uri) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.END);
        row.setPadding(dp(6), dp(6), dp(6), dp(6));

        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(180), dp(180));
        lp.setMargins(0, 0, dp(8), 0);
        image.setLayoutParams(lp);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageURI(uri);
        image.setBackground(getDrawable(R.drawable.bg_chat_user));

        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        avatar.setImageResource(R.drawable.ic_user);

        row.addView(image);
        row.addView(avatar);
        chatContainer.addView(row);
        scrollToBottom();
    }

    private void addBotBubbleMarkdown(String markdown) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_bot);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        avatarParams.setMargins(0, 0, dp(8), 0);
        avatar.setLayoutParams(avatarParams);

        TextView tv = new TextView(this);
        tv.setBackground(getDrawable(R.drawable.bg_chat_bot));
        tv.setTextColor(getColor(R.color.text_primary));
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        tv.setTextIsSelectable(true);

        // Render Markdown -> TextView
        markwon.setMarkdown(tv, markdown);

        row.addView(avatar);
        row.addView(tv);
        chatContainer.addView(row);
    }

    private View addBotTyping() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_bot);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        avatarParams.setMargins(0, 0, dp(8), 0);
        avatar.setLayoutParams(avatarParams);

        TextView tv = new TextView(this);
        tv.setBackground(getDrawable(R.drawable.bg_chat_bot));
        tv.setTextColor(getColor(R.color.text_secondary));
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        tv.setText("Đang soạn câu trả lời...");

        row.addView(avatar);
        row.addView(tv);
        chatContainer.addView(row);
        scrollToBottom();
        return row;
    }

    private void removeView(View v) {
        if (v != null && v.getParent() == chatContainer) chatContainer.removeView(v);
    }

    private void scrollToBottom() {
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale);
    }
}
