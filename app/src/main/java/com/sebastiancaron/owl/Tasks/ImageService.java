package com.sebastiancaron.owl.Tasks;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageService extends Service {

    private boolean isProcessingComplete = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Owl.getInstance().setServiceRunning(true);
        if (intent != null) {
            ArrayList<Mp3file> objectList = (ArrayList<Mp3file>) intent.getExtras().get("objectList");

            if (objectList != null) {
                processObjectsInBackground(objectList);
            }
        }

        return START_NOT_STICKY;
    }

    private void processObjectsInBackground(final ArrayList<Mp3file> objectList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int processedObjects = 0;
                while (processedObjects < objectList.size()) {
                    Mp3file currentObject = objectList.get(processedObjects);

                    Bitmap mp3Thumbnail = getMp3Thumbnail(currentObject.getFilePath());
                    if (mp3Thumbnail != null) {
                        String fileName = "album_cover_" + currentObject.getTitle().replaceAll("/", "_").replaceAll(" ", "").replaceAll("#", "hash") + currentObject.getAlbum().replaceAll("/", "_").replaceAll(" ", "").replaceAll("#", "hash"); // Nom de fichier unique basé sur l'identifiant de l'album
                        String fileNameHQ = fileName + "HQ.jpg";
                        fileName = fileName + ".jpg";
                        File file = new File(getFilesDir(), fileName);
                        File fileHQ = new File(getFilesDir(), fileNameHQ);
                        if(!file.exists()){
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                mp3Thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if(!fileHQ.exists()){
                            try (FileOutputStream fos = new FileOutputStream(fileHQ)) {
                                mp3Thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        currentObject.setThumbnailPath(fileName);
                        currentObject.setThumbnailPathHQ(fileNameHQ);
                    } else {
                        currentObject.setThumbnailPath(null);
                    }
                    Owl.getInstance().getAllFiles().set(processedObjects, currentObject);
                    processedObjects++;

                    showNotification(processedObjects, objectList.size());
                }
                isProcessingComplete = true; // Mark processing as complete

                // If processing is complete, cancel the ongoing notification
                if (isProcessingComplete) {
                    Owl.getInstance().setServiceRunning(false);
                    Owl.getInstance().saveFiles(Owl.getInstance().getAllFiles());
                    playlistProcess();
                    Owl.getInstance().refreshUI();
                    cancelNotification();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImageService.this, ImageService.this.getString(R.string.audio_fini), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                stopSelf();
            }
        }).start();
    }

    private void playlistProcess(){
        Owl owl = Owl.getInstance();
        owl.setProcessPlaylistRunning(true);
        List<Mp3file> allFiles = owl.getAllFiles();
        List<Playlist> playlists = owl.getPlaylists();
        if(playlists.size() > 0){
            for(int i = 0; i < playlists.size(); i++){
                Playlist p = playlists.get(i);
                owl.setCoverPlaylist(p);
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
                if(artistPlaylist.getPathToImage() == null){
                    artistPlaylist.setPathToImage(mp3File.getThumbnailPath());
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
            if(albumPlaylist.getPathToImage() == null){
                albumPlaylist.setPathToImage(mp3File.getThumbnailPath());
            }
            albumPlaylist.getSongs().add(mp3File);
        }


        List<Playlist> artistPlaylistsList = new ArrayList<>(artistPlaylists.values());
        List<Playlist> albumPlaylistsList = new ArrayList<>(albumPlaylists.values());

        artistPlaylistsList.sort((a, b) -> Integer.compare(b.getSongs().size(), a.getSongs().size()));
        albumPlaylistsList.sort((a, b) -> Integer.compare(b.getSongs().size(), a.getSongs().size()));

        for(Playlist playlist : albumPlaylistsList){
            List<Mp3file> songs = playlist.getSongs();
            Collections.sort(songs, (a, b) -> Integer.compare(a.getTrack(), b.getTrack()));
        }
        owl.setProcessPlaylistRunning(false);
        owl.saveAlbumPlaylists(albumPlaylistsList);
        owl.saveArtistPlaylists(artistPlaylistsList);
    }

    private void showNotification(int processedObjects, int totalFiles) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Create the notification channel if running on Android Oreo (API 26) or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = "hibou";
                CharSequence channelName = "OWL App";
                String channelDescription = "Owl notification channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);

                notificationManager.createNotificationChannel(channel);
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
            remoteViews.setTextViewText(R.id.notificationTitle, "Traitement des Vignettes Audio");
            remoteViews.setTextViewText(R.id.notificationText, processedObjects + " / " + totalFiles);

            //int progress = (int) (((float) processedObjects / totalFiles) * 100);

            remoteViews.setProgressBar(R.id.progressBar, totalFiles, processedObjects, false);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "hibou")
                    .setSmallIcon(R.drawable.test)
                    .setOngoing(true)
                    .setSilent(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContent(remoteViews);


            if (isProcessingComplete) {
                cancelNotification();
            } else {
                // Display the notification
                notificationManager.notify(69, builder.build());
                startForeground(69, builder.build());
            }
        }
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(69);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Bitmap getMp3Thumbnail(String mp3FilePath) {
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mp3FilePath);

            byte[] artworkBytes = retriever.getEmbeddedPicture();
            if (artworkBytes != null) {
                return BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.length);
            }
            return null;
        }catch (RuntimeException e){
            return null;
        }

    }
}
