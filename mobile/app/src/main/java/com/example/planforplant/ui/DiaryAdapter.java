package com.example.planforplant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planforplant.R;
import com.example.planforplant.DTO.DiaryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private final Context context;
    private final List<DiaryResponse> diaries;
    private final List<Long> selectedIds = new ArrayList<>();
    private boolean selectionMode = false;

    private final OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public DiaryAdapter(Context context, List<DiaryResponse> diaries,
                        OnSelectionChangedListener listener) {
        this.context = context;
        this.diaries = diaries;
        this.listener = listener;
    }

    public void setData(List<DiaryResponse> list) {
        diaries.clear();
        diaries.addAll(list);
        // Sắp xếp theo entryTime mới nhất → cũ nhất (nếu entryTime là chuỗi yyyy-MM-dd ...)
        Collections.sort(diaries, (a, b) -> b.getEntryTime().compareTo(a.getEntryTime()));
        selectedIds.clear();
        selectionMode = false;
        notifyDataSetChanged();
    }

    public List<Long> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_diary_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryResponse diary = diaries.get(position);
        holder.tvDate.setText(formatDate(diary.getEntryTime()));
        holder.tvText.setText(diary.getContent());

        boolean isSelected = selectedIds.contains(diary.getId());
        holder.itemView.setBackgroundColor(isSelected ? 0x2234A853 : 0x00000000);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(diary, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(diary, position);
            }
            return true;
        });
    }

    private void toggleSelection(DiaryResponse diary, int position) {
        if (selectedIds.contains(diary.getId())) {
            selectedIds.remove(diary.getId());
        } else {
            selectedIds.add(diary.getId());
        }

        notifyItemChanged(position);

        if (selectedIds.isEmpty()) selectionMode = false;
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    private String formatDate(String rawDate) {
        // Tuỳ định dạng entryTime backend trả về (ví dụ "2025-11-05T12:00:00")
        if (rawDate == null) return "";
        if (rawDate.contains("T")) {
            return rawDate.split("T")[0]; // Lấy phần ngày "2025-11-05"
        }
        return rawDate;
    }

    @Override
    public int getItemCount() {
        return diaries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDiaryDate);
            tvText = itemView.findViewById(R.id.tvDiaryText);
        }
    }
}
