package com.sebastiancaron.owl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Tasks.ImageService;
import com.sebastiancaron.owl.Tasks.RetrieveAudioFiles;
import com.sebastiancaron.owl.Tools.DataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DataManager dataManager;
    private List<Mp3file> allFiles;
    private RetrieveAudioFiles task;
    private static final int REQUEST_PERMISSION = 1001;

    private TextView progressText;

    private Runnable runnable;
    private final Handler handler = new Handler();
    private Owl owl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        owl = Owl.getInstance();
        progressText = findViewById(R.id.textViewProgress);

        dataManager = Owl.getInstance().getDataManager();
        allFiles = Owl.getInstance().getFiles();
        if(allFiles == null){
            RetrieveFiles();
            //checkPermissions();
        }else{
            Intent i = new Intent(MainActivity.this, MainListener.class);
            startActivity(i);
            finish();
        }
    }

    public void RetrieveFiles(){
        runnable = new Runnable() {
            @Override
            public void run() {
                progressText.setText(((int) task.progress) + " %");
                System.out.println("Progress : " + task.progress);
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 500);

        task = new RetrieveAudioFiles(getBaseContext(), new RetrieveAudioFiles.OnAudioFilesRetrievedListener() {
            @Override
            public void onAudioFilesRetrieved(List<Mp3file> mp3FilesLists) {
                allFiles = mp3FilesLists;
                Owl.getInstance().saveFiles(allFiles);
                dataManager.setAllFiles(allFiles);

                Intent intent = new Intent(getBaseContext(), ImageService.class);
                intent.putExtra("objectList", new ArrayList<>(allFiles));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }

                List<Playlist> playlists = owl.getPlaylists();
                if(playlists.size() != 0){
                    int n_supprime = 0;
                    for(int i = 0; i < playlists.size(); i++){
                        Playlist playlist = playlists.get(i);
                        List<Mp3file> songs = playlist.getSongs();
                        for(int j = 0; j < songs.size()-n_supprime; j++){
                            int index = -1;
                            Mp3file playlist_audio = songs.get(j-n_supprime);
                            for(int k = 0; k < allFiles.size(); k++){
                                Mp3file file_audio = allFiles.get(k);
                                if(file_audio.getArtist().equals(playlist_audio.getArtist()) && file_audio.getAlbum().equals(playlist_audio.getAlbum()) && file_audio.getTitle().equals(playlist_audio.getTitle()) && file_audio.getDuration() == playlist_audio.getDuration()){

                                    index = k;
                                    break;
                                }
                            }

                            if(index == -1){
                                // ON SUPPRIME
                                songs.remove(j-n_supprime);
                                n_supprime++;
                                j--;

                            }else{
                                songs.set(j-n_supprime, allFiles.get(index));
                            }
                        }
                    }
                    owl.savePlaylists();
                }

                Map<String, Playlist> artistPlaylists = new HashMap<>();
                Map<String, Playlist> albumPlaylists = new HashMap<>();

                for (Mp3file mp3File : allFiles) {
                    // Create or update artist playlist
                    String artistKey = mp3File.getArtist();
                    String oldArtistKey = artistKey;
                    if (artistKey.endsWith(" - Topic")) {
                        artistKey = artistKey.substring(0, artistKey.lastIndexOf(" - Topic"));
                    } else if (artistKey.endsWith("VEVO")) {
                        artistKey = artistKey.substring(0, artistKey.lastIndexOf("VEVO"));
                    }

                    artistKey = artistKey.replace(" ", "");
                    if(!(artistKey.equals("<unknown>")) && !(artistKey.equals(""))){
                        Playlist artistPlaylist = artistPlaylists.get(artistKey);
                        if (artistPlaylist == null) {
                            artistPlaylist = new Playlist();
                            artistPlaylist.setNom(oldArtistKey);
                            artistPlaylist.setSongs(new ArrayList<>());
                            artistPlaylist.setPathToImage(mp3File.getThumbnailPath());
                            artistPlaylists.put(artistKey, artistPlaylist);
                        }
                        artistPlaylist.getSongs().add(mp3File);
                    }
                    // Create or update album playlist
                    String albumKey = mp3File.getAlbum();
                    Playlist albumPlaylist = albumPlaylists.get(albumKey);
                    if (albumPlaylist == null) {
                        albumPlaylist = new Playlist();
                        albumPlaylist.setNom(albumKey);
                        albumPlaylist.setPathToImage(mp3File.getThumbnailPath());
                        albumPlaylist.setSongs(new ArrayList<>());
                        albumPlaylists.put(albumKey, albumPlaylist);
                    }
                    albumPlaylist.getSongs().add(mp3File);
                }


                List<Playlist> artistPlaylistsList = new ArrayList<>(artistPlaylists.values());
                List<Playlist> albumPlaylistsList = new ArrayList<>(albumPlaylists.values());

                Collections.sort(artistPlaylistsList, (a, b) -> a.getSongs().size() < b.getSongs().size() ? 1 : a.getSongs().size() == b.getSongs().size() ? 0 : -1);
                Collections.sort(albumPlaylistsList, (a, b) -> a.getSongs().size() < b.getSongs().size() ? 1 : a.getSongs().size() == b.getSongs().size() ? 0 : -1);

                for(Playlist playlist : albumPlaylistsList){
                    List<Mp3file> songs = playlist.getSongs();
                    Collections.sort(songs, (a, b) -> Integer.compare(a.getTrack(), b.getTrack()));
                }

                owl.saveAlbumPlaylists(albumPlaylistsList);
                owl.saveArtistPlaylists(artistPlaylistsList);


                Intent i = new Intent(MainActivity.this, MainListener.class);
                startActivity(i);
                handler.removeCallbacks(runnable);
                finish();
            }
        });
        task.execute();

    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

        } else {
            RetrieveFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                RetrieveFiles();
            } else {
                RetrieveFiles();
                Toast.makeText(getApplicationContext(), "Permission refusée. Impossible de récupérer les fichiers audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}