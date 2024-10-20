package com.sebastiancaron.owl.Objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String nom;
    private String pathToImage;
    private List<Mp3file> songs;
    private Bitmap image;

    public Playlist(){

    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }

    public void setSongs(List<Mp3file> songs) {
        this.songs = songs;
    }

    public List<Mp3file> getSongs() {
        return songs;
    }

    public String getNom() {
        return nom;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public Bitmap getCoverImageBitmap(Context context) {
        if(pathToImage != null){
            String fileName = getPathToImage();

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

    public Playlist clone(){
        Playlist playlist = new Playlist();
        playlist.setNom(this.nom);
        playlist.setPathToImage(this.pathToImage);

        List<Mp3file> new_songs = new ArrayList<>();
        for(int i =0; i < this.songs.size(); i++){
            new_songs.add(this.songs.get(i));
        }

        playlist.setSongs(new_songs);

        return playlist;
    }
}
