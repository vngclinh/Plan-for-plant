package com.example.planforplant.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.planforplant.R;

/**
 * Màn hình Nhật ký.
 * Kế thừa từ NavigationBarActivity để tự động có thanh điều hướng.
 */
public class DiaryActivity extends NavigationBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập layout cho màn hình này (care_calendar.xml)
        setContentView(R.layout.care_calendar);

        // Code xử lý cho màn hình lịch của bạn sẽ nằm ở đây
    }
}
