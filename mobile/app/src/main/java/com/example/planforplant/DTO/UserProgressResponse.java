package com.example.planforplant.DTO;

public class UserProgressResponse {
    private String level;          // "MAM", "TRUONG_THANH", "CO_THU"
    private int streak;
    private long completedSchedules;
    private long treeCount;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public long getCompletedSchedules() { return completedSchedules; }
    public void setCompletedSchedules(long completedSchedules) { this.completedSchedules = completedSchedules; }

    public long getTreeCount() { return treeCount; }
    public void setTreeCount(long treeCount) { this.treeCount = treeCount; }
}
