package com.example.cameraxvideo.pojo;

import java.io.File;

public class VideoFile {
    private File file;
    private boolean isLiked = false;
    private String time;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
