package com.sebastiancaron.owl.Objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class Mp3file implements Serializable {

    private String fileName;
    private String filePath;
    private String artist;
    private String album;

    private String title;
    private int duration;
    private boolean isSelected;

    private String thumbnailPath;
    private String thumbnailPathHQ;
    private int track;

    public Mp3file() {

    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private Bitmap coverImageBitmap;

    public Bitmap getCoverImageBitmap(Context context) {
        if(thumbnailPath != null){
            String fileName = getThumbnailPath();

            File file = new File(context.getFilesDir(), fileName);

            if (file.exists()) {
                Bitmap albumCoverBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                return albumCoverBitmap;
            } else {
                return null;
            }
        }
        return null;
    }

    public void setCoverImageBitmap(Bitmap coverImageBitmap) {
        this.coverImageBitmap = coverImageBitmap;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setThumbnailPathHQ(String thumbnailPathHQ) {
        this.thumbnailPathHQ = thumbnailPathHQ;
    }

    public String getThumbnailPathHQ() {
        return thumbnailPathHQ;
    }

    public Bitmap getCoverImageBitmapHQ(Context context) {
        if(thumbnailPath != null){
            String fileName = getThumbnailPathHQ();

            File file = new File(context.getFilesDir(), fileName);

            if (file.exists()) {
                Bitmap albumCoverBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                return albumCoverBitmap;
            } else {
                return null;
            }
        }
        return null;
    }
}
