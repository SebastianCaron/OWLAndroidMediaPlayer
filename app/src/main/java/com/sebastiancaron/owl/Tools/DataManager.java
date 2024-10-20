package com.sebastiancaron.owl.Tools;

import android.media.MediaPlayer;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;

import java.util.List;
import java.util.Locale;

public class DataManager {

    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private List<Mp3file> allFiles;
    private int currentSong;

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setAllFiles(List<Mp3file> allFiles) {
        this.allFiles = allFiles;
        Owl.getInstance().setAllFiles(allFiles);
    }

    public List<Mp3file> getAllFiles() {
        return allFiles;
    }

    public int getCurrentSongIndex() {
        return currentSong;
    }

    public Mp3file getCurrentSong() {
        return playlist.getSongs().get(currentSong);
    }

    public void setCurrentSongIndex(int currentSong) {
        this.currentSong = currentSong;
    }

    public String getFormattedDuration(int duration) {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }


}

