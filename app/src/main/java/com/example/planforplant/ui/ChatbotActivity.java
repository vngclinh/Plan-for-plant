package com.example.planforplant.ui;

import android.os.Bundle;

import com.example.planforplant.R;

/**
 * Màn hình Chatbot.
 * Kế thừa từ NavigationBarActivity để tự động có thanh điều hướng.
 */
public class ChatbotActivity extends NavigationBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập layout cho màn hình này
        setContentView(R.layout.chatbot);

        // Code xử lý cho chatbot của bạn sẽ nằm ở đây
    }
}
