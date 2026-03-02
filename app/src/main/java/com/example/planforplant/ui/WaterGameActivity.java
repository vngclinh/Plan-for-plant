package com.example.planforplant.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planforplant.DTO.UserProgressResponse;
import com.example.planforplant.R;
import com.example.planforplant.api.ApiClient;
import com.example.planforplant.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaterGameActivity extends NavigationBarActivity {

    private ImageView ivPlant, ivWateringCan, ivDrop1, ivDrop2, ivDrop3;
    private TextView tvLevel, tvStreak, tvLevelHint, tvWaterSubtitle;
    private ProgressBar progressLevel;

    private ApiService api;

    // quản lý “đã tưới hôm nay chưa”
    private SharedPreferences waterPrefs;
    private static final String PREF_NAME = "water_game_prefs";
    private static final String KEY_LAST_WATER_DATE = "last_water_date";
    private String todayStr;
    private boolean hasWateredToday = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_game);

        api = ApiClient.getLocalClient(this).create(ApiService.class);

        ivPlant = findViewById(R.id.ivPlant);
        ivWateringCan = findViewById(R.id.ivWateringCan);
        ivDrop1 = findViewById(R.id.ivWaterDrop1);
        ivDrop2 = findViewById(R.id.ivWaterDrop2);
        ivDrop3 = findViewById(R.id.ivWaterDrop3);

        tvLevel = findViewById(R.id.tvLevel);
        tvStreak = findViewById(R.id.tvStreak);
        tvLevelHint = findViewById(R.id.tvLevelHint);
        progressLevel = findViewById(R.id.progressLevel);

        // setup ngày hôm nay + prefs
        todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        waterPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String lastWaterDate = waterPrefs.getString(KEY_LAST_WATER_DATE, null);
        hasWateredToday = todayStr.equals(lastWaterDate);

        updateTopBarMessage();
        // xoá streak để test
        waterPrefs.edit().remove(KEY_LAST_WATER_DATE).apply();
        // load progress ban đầu
        loadProgress();

        ivPlant.setOnClickListener(v -> {
            if (hasWateredToday) {
                // Đã tưới rồi: không gọi API nữa
                Toast.makeText(this,
                        "Hôm nay bạn tưới rồi đó, quay lại vào ngày mai nhé 🌿",
                        Toast.LENGTH_SHORT).show();
                ivPlant.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.plant_bounce)
                );
                return;
            }

            // lần đầu trong ngày: animate + call API
            playWaterAnimation();
            callWaterApi();  // POST /api/user/water-tree
        });
    }

    /* ----------------- ANIMATION ----------------- */

    private void playWaterAnimation() {
        // Bình xuất hiện + nghiêng
        ivWateringCan.setAlpha(1f);
        Animation canAnim = AnimationUtils.loadAnimation(this, R.anim.watering_can_in);
        canAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // ẩn bình sau khi tưới xong cho mềm
                ivWateringCan.setAlpha(0f);
            }
        });
        ivWateringCan.startAnimation(canAnim);

        // Nhiều giọt rơi lần lượt
        animateDrop(ivDrop1, 0);
        animateDrop(ivDrop2, 150);
        animateDrop(ivDrop3, 300);

        // Cây nhún nhẹ
        ivPlant.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.plant_bounce)
        );
    }

    private void animateDrop(ImageView drop, long delayMs) {
        drop.clearAnimation();
        drop.setAlpha(1f);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.water_drop_fall);
        anim.setStartOffset(delayMs);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                drop.setAlpha(0f);
            }
        });

        drop.startAnimation(anim);
    }

    /* ----------------- API CALLS ----------------- */

    private void callWaterApi() {
        api.waterTreeStreak().enqueue(new Callback<UserProgressResponse>() {
            @Override
            public void onResponse(Call<UserProgressResponse> call,
                                   Response<UserProgressResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                UserProgressResponse p = response.body();

                // đánh dấu đã tưới hôm nay
                hasWateredToday = true;
                waterPrefs.edit().putString(KEY_LAST_WATER_DATE, todayStr).apply();
                updateTopBarMessage();

                updateProgressUI(p);
            }

            @Override
            public void onFailure(Call<UserProgressResponse> call, Throwable t) {
                Toast.makeText(WaterGameActivity.this,
                        "Tưới cây thất bại, thử lại sau nhé 🌧",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProgress() {
        api.getProgress().enqueue(new Callback<UserProgressResponse>() {
            @Override
            public void onResponse(Call<UserProgressResponse> call,
                                   Response<UserProgressResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                UserProgressResponse p = response.body();
                updateProgressUI(p);
            }

            @Override
            public void onFailure(Call<UserProgressResponse> call, Throwable t) { }
        });
    }

    /* ----------------- UI HELPER ----------------- */

    private void updateTopBarMessage() {
        if (hasWateredToday) {
            tvStreak.setText("Bạn đã tưới cây hôm nay, quay lại vào ngày mai nhé 🌼");
        } else {
            tvStreak.setText("Bạn chưa tưới cây hôm nay");
        }
    }

    private void updateProgressUI(UserProgressResponse p) {
        String level = p.getLevel();
        int streak = p.getStreak();
        long done = p.getCompletedSchedules();
        long treeCount = p.getTreeCount();

        // Cấp độ + icon
        tvLevel.setText(mapLevelLabel(level));

        // Hint chi tiết
        tvLevelHint.setText(buildHintDetail(level, done, treeCount, streak));



        // Thanh tiến độ %
        int percent = calculateProgressPercent(level, done, treeCount, streak);
        progressLevel.setProgress(percent);

        switch (level) {
            case "MAM":
                ivPlant.setImageResource(R.drawable.ic_plant_mam);
                break;
            case "TRUONG_THANH":
                ivPlant.setImageResource(R.drawable.ic_plant_truong_thanh);
                break;
            case "CO_THU":
                ivPlant.setImageResource(R.drawable.ic_plant_co_thu);
                break;
        }
    }

    private String mapLevelLabel(String level) {
        switch (level) {
            case "TRUONG_THANH":
                return "Cấp độ: Cây trưởng thành \uD83E\uDEB4";
            case "CO_THU":
                return "Cấp độ: Cây cổ thụ 🌳";
            case "MAM":
            default:
                return "Cấp độ: Mầm non 🌱";
        }
    }

    // Text nhỏ bên dưới progress – hiển thị tiến độ hiện tại
    private String buildHintDetail(String level, long done, long treeCount, int streak) {
        if ("MAM".equals(level)) {
            return "Đã hoàn thành " + done + "/10 lịch chăm, "
                    + treeCount + "/3 cây, streak " + streak + "/50 ngày để lên cấp cây trưởng thành \uD83E\uDEB4";
        } else if ("TRUONG_THANH".equals(level)) {
            return "Đã hoàn thành " + done + "/50 lịch chăm, "
                    + treeCount + "/10 cây, streak " + streak + "/200 ngày để lên cấp cây cổ thụ 🌳";
        } else { // CO_THU
            return "Bạn có " + treeCount + " cây và đã hoàn thành "
                    + done + " lịch chăm, streak " + streak + " ngày rồi ✨";
        }
    }

    // Tính % thanh tiến độ theo từng cấp
// Tính % thanh tiến độ theo từng cấp = (done + tree + streak) / (tổng cần)
    private int calculateProgressPercent(String level, long done, long treeCount, int streak) {
        int currentSum;
        int requiredSum;

        if ("MAM".equals(level)) {
            // Mầm: 10 plan + 3 cây + 50 streak
            int doneClamped   = (int) Math.min(done, 10);
            int treeClamped   = (int) Math.min(treeCount, 3);
            int streakClamped = Math.min(streak, 50);

            currentSum = doneClamped + treeClamped + streakClamped;
            requiredSum = 10 + 3 + 50; // 63
        } else if ("TRUONG_THANH".equals(level)) {
            // Trưởng thành: 50 plan + 10 cây + 200 streak
            int doneClamped   = (int) Math.min(done, 50);
            int treeClamped   = (int) Math.min(treeCount, 10);
            int streakClamped = Math.min(streak, 200);

            currentSum = doneClamped + treeClamped + streakClamped;
            requiredSum = 50 + 10 + 200; // 260
        } else {
            // Cây cổ thụ: luôn full
            return 100;
        }

        float ratio = (requiredSum == 0) ? 0f : (currentSum * 1f / requiredSum);
        return Math.round(ratio * 100);
    }

}
