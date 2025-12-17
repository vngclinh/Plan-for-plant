package com.example.planforplant.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.DTO.NotificationResponse;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClicked(NotificationResponse n);
        void onNotificationLongClicked(NotificationResponse n); // optional: xoá, unread...
    }

    private final List<NotificationListItem> items = new ArrayList<>();
    private OnNotificationClickListener listener;

    public void setOnNotificationClickListener(OnNotificationClickListener l) {
        this.listener = l;
    }

    public void setItems(List<NotificationListItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == NotificationListItem.TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_notification_header, parent, false);
            return new HeaderVH(v);
        }

        View v = inflater.inflate(R.layout.item_notification, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        NotificationListItem li = items.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText(li.headerText);
            return;
        }

        ItemVH vh = (ItemVH) holder;
        NotificationResponse n = li.noti;

        vh.tvTitle.setText(n.title);
        vh.tvBody.setText(n.content);
        vh.tvTime.setText(formatTime(n.createdAt));

        // UI: chưa đọc -> đậm hơn, đọc rồi -> mờ
        vh.tvTitle.setAlpha(n.read ? 0.6f : 1f);
        vh.tvBody.setAlpha(n.read ? 0.6f : 1f);

        vh.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClicked(n);
        });

        vh.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNotificationLongClicked(n);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // format ISO -> "HH:mm" (đơn giản)
    private String formatTime(String iso) {
        try {
            // 2025-12-17T03:49:02.967 -> lấy HH:mm
            int t = iso.indexOf('T');
            if (t >= 0 && iso.length() >= t + 6) {
                return iso.substring(t + 1, t + 6);
            }
        } catch (Exception ignored) {}
        return iso == null ? "" : iso;
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvTime;
        ItemVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
