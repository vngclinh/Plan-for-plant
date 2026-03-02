package com.example.planforplant.utils;

import java.util.Locale;

public class ScheduleTypeConverter {

    public static String toVietnamese(String type) {
        if (type == null) return "Khác";

        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING":
                return "Tưới nước";
            case "FERTILIZING":
                return "Bón phân";
            case "PRUNING":
                return "Tỉa cành";
            case "MIST":
                return "Phun sương";
            case "OTHER":
            default:
                return "Khác";
        }
    }

    public static String getEmoji(String type) {
        if (type == null) return "🧺";

        switch (type.toUpperCase(Locale.ROOT)) {
            case "WATERING":
                return "💧";
            case "FERTILIZING":
                return "🌱";
            case "PRUNING":
                return "✂️";
            case "MIST":
                return "💦";
            default:
                return "🧺";
        }
    }
}
