package com.example.planforplant.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.planforplant.R;
import com.example.planforplant.ui.MainActivity;
import com.example.planforplant.ui.ScheduleListActivity;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String gardenName = getInputData().getString("gardenName");
        String type = getInputData().getString("type");
        Log.d("NotificationWorker", "Worker executed for " + gardenName + " - " + type);
        showNotification(gardenName, type);
        return Result.success();
    }

    private void showNotification(String gardenName, String type) {
        Context context = getApplicationContext();
        String channelId = "planforplant_channel";
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Plan For Plant Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhắc nhở chăm sóc cây 🌿");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, ScheduleListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_plant)
                .setContentTitle("🌿 Nhắc nhở chăm sóc cây")
                .setContentText("Đến giờ " + type.toLowerCase() + " cho " + gardenName + " rồi!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
