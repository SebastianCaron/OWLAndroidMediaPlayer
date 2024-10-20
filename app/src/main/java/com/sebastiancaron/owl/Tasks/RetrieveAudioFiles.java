package com.sebastiancaron.owl.Tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import java.util.ArrayList;

public class RetrieveAudioFiles extends AsyncTask<Void, Void, List<Mp3file>> {

        private final Context context;
        private final OnAudioFilesRetrievedListener listener;
        public float progress = 0;

        public RetrieveAudioFiles(Context context, OnAudioFilesRetrievedListener listener) {
                this.context = context;
                this.listener = listener;
        }

    @Override
    protected List<Mp3file> doInBackground(Void... voids) {
        List<Mp3file> mp3FilesList = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TRACK
        };

        String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int totalFiles = cursor.getCount();
                int processedFiles = 0;

                while (cursor.moveToNext()) {
                    processedFiles++;
                    progress = ((int) ((processedFiles / (float) totalFiles) * 100));

                    Mp3file mp3FileInfo = new Mp3file();
                    mp3FileInfo.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
                    mp3FileInfo.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
                    if (mp3FileInfo.getArtist().equals("<unknown>")) {
                        mp3FileInfo.setArtist("");
                    }

                    mp3FileInfo.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
                    mp3FileInfo.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)) / 1000);
                    mp3FileInfo.setFilePath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                    mp3FileInfo.setTrack(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TRACK)));

                    String fileName = "album_cover_" + mp3FileInfo.getTitle().replaceAll("/", "_").replaceAll(" ", "") + mp3FileInfo.getAlbum().replaceAll("/", "_").replaceAll(" ", ""); // Nom de fichier unique basé sur l'identifiant de l'album
                    String fileNameHQ = fileName + "HQ.jpg";
                    fileName = fileName + ".jpg";
                    File file = new File(context.getFilesDir(), fileName);
                    File fileHQ = new File(context.getFilesDir(), fileNameHQ);

                    if(file.exists()){
                        mp3FileInfo.setThumbnailPath(fileName);
                    }

                    if(fileHQ.exists()){
                        mp3FileInfo.setThumbnailPathHQ(fileNameHQ);
                    }

                    mp3FilesList.add(mp3FileInfo);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mp3FilesList;
    }


        @Override
        protected void onPostExecute(List<Mp3file> mp3FilesList) {
            if (listener != null) {
                listener.onAudioFilesRetrieved(mp3FilesList);
            }
        }

    private void processImageInBackground(final Mp3file mp3FileInfo, int n_file, int total_files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Votre logique de traitement d'image ici
                Bitmap mp3Thumbnail = getMp3Thumbnail(mp3FileInfo.getFilePath());
                if (mp3Thumbnail != null) {
                    String fileName = "album_cover_" + mp3FileInfo.getTitle().replaceAll("/", "_").replaceAll(" ", "") + mp3FileInfo.getAlbum().replaceAll("/", "_").replaceAll(" ", ""); // Nom de fichier unique basé sur l'identifiant de l'album
                    String fileNameHQ = fileName + "HQ.jpg";
                    fileName = fileName + ".jpg";
                    File file = new File(context.getFilesDir(), fileName);
                    File fileHQ = new File(context.getFilesDir(), fileNameHQ);

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        mp3Thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try (FileOutputStream fos = new FileOutputStream(fileHQ)) {
                        mp3Thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mp3FileInfo.setThumbnailPath(fileName);
                    mp3FileInfo.setThumbnailPathHQ(fileNameHQ);
                    System.out.println(mp3FileInfo.getTitle() + ": OK ! " + n_file + " / " + total_files);
                } else {
                    mp3FileInfo.setThumbnailPath(null);
                }



                // Notify UI thread if needed
                //publishProgress(100);
            }
        }).start();
    }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        public interface OnAudioFilesRetrievedListener {
            void onAudioFilesRetrieved(List<Mp3file> mp3FilesList);
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
