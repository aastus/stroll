package com.example.stroll.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "achievements")
public class Achievement {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;

    private String description;

    // drawable resource id
    private int imageResId;

    private boolean isUnlocked = false;

    private int currentProgress = 0;

    private int maxProgress = 1;

    // timestamp часу розблокування
    private long unlockedTime = 0L;

    public Achievement(
            @NonNull String id,
            String title,
            String description,
            int imageResId,
            boolean isUnlocked,
            int currentProgress,
            int maxProgress,
            long unlockedTime
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.isUnlocked = isUnlocked;
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
        this.unlockedTime = unlockedTime;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public long getUnlockedTime() {
        return unlockedTime;
    }

    public void setUnlockedTime(long unlockedTime) {
        this.unlockedTime = unlockedTime;
    }
}