package com.example.planforplant.ui;

import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ChatApi;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends NavigationBarActivity{

    private EditText etMessage;
    private boolean isSending = false;

    private ImageButton btnSend;
    private ScrollView chatScroll;
    private LinearLayout chatContainer;

    private ChatApi chatApi;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatScroll = findViewById(R.id.chatScroll);
        chatContainer = findViewById(R.id.chatContainer);

        // Markwon render Markdown
        markwon = Markwon.create(this);
        chatApi = ApiClient.getLocalClient(this).create(ChatApi.class);

        btnSend.setEnabled(false);
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                updateSendEnabled();
            }
        });
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty() || isSending) return;

            isSending = true;
            updateSendEnabled();

            addUserBubble(text);
            etMessage.setText("");

            View typingView = addBotTyping();

            chatApi.sendMessage(new ChatApi.MessageRequest(text)).enqueue(new Callback<String>() {
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
        });
    }

    private void updateSendEnabled() {
        boolean hasText = etMessage.getText().toString().trim().length() > 0;
        boolean enabled = hasText && !isSending;
        btnSend.setEnabled(enabled);

//        btnSend.setImageAlpha(enabled ? 255 : 120);
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
