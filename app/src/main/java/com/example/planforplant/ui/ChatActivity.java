package com.example.planforplant.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
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
    private ImageButton btnSend;
    private ScrollView chatScroll;
    private LinearLayout chatContainer;

    private ChatApi chatApi;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot); // thay bằng layout xml của bạn

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatScroll = findViewById(R.id.chatScroll);
        chatContainer = findViewById(R.id.chatContainer);

        // Markwon render Markdown
        markwon = Markwon.create(this);
        // Retrofit local client (đã có Scalars + Gson + interceptor)
        chatApi = ApiClient.getLocalClient(this).create(ChatApi.class);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            // 1) Bubble user
            addUserBubble(text);
            etMessage.setText("");

            // 2) Hiển thị typing
            View typingView = addBotTyping();
            btnSend.setEnabled(false);

            // 3) Call API
            chatApi.sendMessage(new ChatApi.MessageRequest(text)).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    removeView(typingView);
                    btnSend.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        addBotBubbleMarkdown(response.body());
                    } else {
                        addBotBubbleMarkdown("**⚠️ Lỗi:** " + response.code() + " - " + response.message());
                    }
                    scrollToBottom();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    removeView(typingView);
                    btnSend.setEnabled(true);
                    addBotBubbleMarkdown("**⚠️ Mạng lỗi:** " + t.getMessage());
                    scrollToBottom();
                }
            });

            scrollToBottom();
        });
    }

    private void addUserBubble(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.END);
        row.setPadding(0, dp(6), 0, dp(6));

        TextView tv = new TextView(this);
        tv.setBackground(getDrawable(R.drawable.bg_chat_user));
        tv.setTextColor(getColor(R.color.white));
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        tv.setText(text);

        row.addView(tv);
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
