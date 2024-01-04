package com.example.cameraxvideo.pojo;

import java.io.File;

public class ImageFile {
    private File file;
    private boolean isLiked=false;

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
}
