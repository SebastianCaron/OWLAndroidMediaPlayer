package com.sebastiancaron.owl;

import static java.lang.Double.min;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sebastiancaron.owl.Fragments.AccueilFragment;
import com.sebastiancaron.owl.Fragments.BibliothequeFragment;
import com.sebastiancaron.owl.Fragments.ExplorerFragment;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Tools.DataManager;
import com.sebastiancaron.owl.Tools.NotificationService;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Owl extends Application {
    private static Owl instance;
    private DataManager dataManager;

    private List<Mp3file> allFiles;

    private MediaPlayer mediaPlayer;
    private Playlist currentPlaylist;
    private List<Mp3file> audioSuivants;
    private Activity currentActivity;

    private List<Playlist> playlists;

    private AccueilFragment accueilFragment;
    private MainListener mainListener;

    @Override
    public void onCreate() {
        super.onCreate();
        playlists = loadPlaylists();
        instance = this;
        dataManager = new DataManager();
        mediaPlayer = null;

        loadLightMode();

        if(!lightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }



    }

    private boolean serviceRunning = false;

    public void setServiceRunning(boolean serviceRunning) {
        this.serviceRunning = serviceRunning;
    }

    public boolean isServiceRunning() {
        return serviceRunning;
    }

    private boolean processPlaylistRunning = false;

    public void setProcessPlaylistRunning(boolean processPlaylistRunning) {
        this.processPlaylistRunning = processPlaylistRunning;
    }
    public boolean isProcessPlaylistRunning() {
        return processPlaylistRunning;
    }

    public static Owl getInstance() {
        return instance;
    }

    public void setAllFiles(List<Mp3file> allFiles) {
        this.allFiles = allFiles;
    }

    public List<Mp3file> getAllFiles() {
        return allFiles;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Obtient la liste de fichiers MP3 à partir des préférences partagées.
     * Convertit la représentation JSON de la liste de fichiers en utilisant la bibliothèque Gson.
     *
     * @return La liste de fichiers MP3 ou null si la liste n'est pas présente dans les préférences partagées.
     */
    public List<Mp3file> getFiles() {
        // Obtient une référence aux préférences partagées avec le nom "Owl" en mode privé
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);

        // Récupère la représentation JSON de la liste de fichiers à partir des préférences partagées sous la clé "mp3List"
        String jsonMp3List = sharedPreferences.getString("mp3List", null);

        // Vérifie si la représentation JSON de la liste de fichiers est présente
        if (jsonMp3List != null) {
            // Initialise un objet Gson pour la conversion en format Java
            Gson gson = new Gson();

            // Obtient le type de la liste de fichiers à partir du type token
            Type type = new TypeToken<List<Mp3file>>() {}.getType();

            // Convertit la représentation JSON en liste de fichiers MP3
            List<Mp3file> mp3List = gson.fromJson(jsonMp3List, type);

            // Retourne la liste de fichiers MP3
            return mp3List;
        }

        // Retourne null si la liste de fichiers n'est pas présente dans les préférences partagées
        return null;
    }


    /**
     * Enregistre la liste de fichiers MP3 spécifiée dans les préférences partagées.
     * Convertit la liste de fichiers en format JSON à l'aide de la bibliothèque Gson
     * avant de l'enregistrer dans les préférences partagées sous la clé "mp3List".
     *
     * @param files La liste de fichiers MP3 à enregistrer.
     */
    public void saveFiles(List<Mp3file> files) {
        if(serviceRunning){
            return;
        }
        // Initialise un objet Gson pour la conversion en format JSON
        Gson gson = new Gson();

        // Convertit la liste de fichiers en format JSON
        String jsonMp3List = gson.toJson(files);

        // Obtient une référence aux préférences partagées avec le nom "Owl" en mode privé
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);

        // Initialise un éditeur pour les préférences partagées
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Enregistre la liste de fichiers en format JSON sous la clé "mp3List"
        editor.putString("mp3List", jsonMp3List);

        // Applique les modifications à l'éditeur des préférences partagées
        editor.apply();
    }


    public void clear(){
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    /**
     * Crée un nouveau lecteur audio (MediaPlayer) s'il n'existe pas encore.
     * Configure le lecteur audio pour diffuser le flux audio en mode musique.
     * Ajoute un auditeur d'achèvement pour gérer les événements de fin de lecture,
     * tels que le passage à la chanson suivante et la mise à jour de l'interface utilisateur.
     */
    public void createMediaPlayer() {
        // Vérifie si le lecteur audio (MediaPlayer) n'existe pas encore
        if (mediaPlayer == null) {
            // Crée un nouveau lecteur audio
            mediaPlayer = new MediaPlayer();

            // Configure le lecteur audio pour diffuser le flux audio en mode musique
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // Ajoute un auditeur d'achèvement pour gérer les événements de fin de lecture
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(isLooping()){
                        replaySong();
                    }else{
                        // Passe à la chanson suivante
                        nextSong();
                    }
                    // Actualise l'interface utilisateur
                    instance.refreshUI();
                }
            });
        }
    }
    private boolean looping = false;

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isLooping() {
        return looping;
    }

    private boolean shuffle = false;

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Obtient la chanson actuellement en cours de lecture dans la playlist actuelle.
     * Si une playlist est actuellement sélectionnée, retourne la chanson à l'index spécifié
     * par le gestionnaire de données (dataManager).
     *
     * @return La chanson actuellement en cours de lecture ou null si aucune playlist n'est sélectionnée.
     */
    public Mp3file getCurrentSong() {
        // Vérifie si une playlist est actuellement sélectionnée
        if (getCurrentPlaylist() != null) {
            // Retourne la chanson à l'index spécifié par le gestionnaire de données (dataManager)
            return getCurrentPlaylist().getSongs().get(dataManager.getCurrentSongIndex());
        }
        // Retourne null si aucune playlist n'est sélectionnée
        return null;
    }


    /**
     * Commence la lecture audio de la chanson spécifiée dans la playlist actuelle.
     * Crée un nouveau lecteur audio s'il n'existe pas encore. Met à jour l'index de la chanson en cours.
     * Charge le fichier MP3 à partir du chemin de la chanson spécifiée dans la playlist.
     * Actualise l'interface utilisateur (UI) et affiche la notification associée.
     *
     * @param index L'index de la chanson à jouer dans la playlist actuelle.
     */
    public void playAudio(int index) {
        // Crée un nouveau lecteur audio s'il n'existe pas encore
        createMediaPlayer();

        // Met à jour l'index de la chanson en cours
        dataManager.setCurrentSongIndex(index);

        try {
            // Charge le fichier MP3 à partir du chemin de la chanson spécifiée dans la playlist
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentPlaylist.getSongs().get(index).getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Actualise l'interface utilisateur (UI)
        refreshUI();
        Bitmap cover = getCurrentSong().getCoverImageBitmap(this);
        if(cover != null){
            Palette.from(cover).generate(palette -> {
                int vibrantColor = palette.getDominantColor(ContextCompat.getColor(this, android.R.color.transparent));
                setNotification_color(vibrantColor);
                showNotif();
            });
        }else{
            if(isNight(this)){
                setNotification_color(getColor(R.color.background_night));
            }else{
                setNotification_color(getColor(R.color.background));
            }
        }

        // Affiche la notification associée
        showNotif();
    }


    public void pauseAudio(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        refreshMainListener();
        Bitmap cover = getCurrentSong().getCoverImageBitmap(this);
        if(cover != null){
            Palette.from(cover).generate(palette -> {
                int vibrantColor = palette.getDominantColor(ContextCompat.getColor(this, android.R.color.transparent));
                setNotification_color(vibrantColor);
                showNotif();
            });
        }else{
            if(isNight(this)){
                setNotification_color(getColor(R.color.background_night));
            }else{
                setNotification_color(getColor(R.color.background));
            }
        }
        showNotif();
    }

    public void continueAudio(){
        mediaPlayer.start();
        refreshMainListener();
        showNotif();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    /**
     * Joue le prochain audio
     */
    public void nextSong(){
        int currentSongIndex = dataManager.getCurrentSongIndex();
        if(isShuffle()){
            Random random = new Random();
            int valeur = random.nextInt(currentPlaylist.getSongs().size());
            if(currentPlaylist.getSongs().size() > 1){
                while (valeur == currentSongIndex){
                    valeur = random.nextInt(currentPlaylist.getSongs().size());
                }
            }
            playAudio(valeur);
            return;
        }

        currentSongIndex = (currentSongIndex + 1) % currentPlaylist.getSongs().size();
        dataManager.setCurrentSongIndex(currentSongIndex);
        playAudio(currentSongIndex);
    }

    public void replaySong(){
        int currentSongIndex = dataManager.getCurrentSongIndex();
        playAudio(currentSongIndex);
    }

    /**
     * Joue l'audio précédent
     */
    public void previousSong(){
        int currentSongIndex = dataManager.getCurrentSongIndex();
        if(isShuffle()){
            Random random = new Random();
            int valeur = random.nextInt(currentPlaylist.getSongs().size());
            if(currentPlaylist.getSongs().size() > 1){
                while (valeur == currentSongIndex){
                    valeur = random.nextInt(currentPlaylist.getSongs().size());
                }
            }
            playAudio(valeur);
            return;
        }
        currentSongIndex = (currentSongIndex - 1) % currentPlaylist.getSongs().size();
        if(currentSongIndex < 0){
            currentSongIndex += currentPlaylist.getSongs().size();
        }
        dataManager.setCurrentSongIndex(currentSongIndex);
        playAudio(currentSongIndex);
    }

    /**
     * Cette fonction vise à récupérer un sous-ensemble de chansons de la playlist actuelle,
     * en commençant par la chanson suivante après celle qui est actuellement en cours de lecture.
     * Le nombre de chansons à charger est limité à 15 ou au nombre total de chansons dans la playlist,
     * selon la plus petite valeur.
     * La fonction gère le retour au début de la playlist si l'index dépasse la taille de la playlist.
     */
    public List<Mp3file> getSubPlaylist(){
        // Obtenir l'index de la chanson actuelle dans la playlist
        int currentSongIndex = dataManager.getCurrentSongIndex();
        // Limiter le nombre de chansons à charger à un maximum de 15 ou à la taille de la playlist, selon la plus petite valeur
        int limitToLoad = (int) min(15, getCurrentPlaylist().getSongs().size());
        // Créer une nouvelle ArrayList pour stocker le sous-ensemble de chansons
        List<Mp3file> sub = new ArrayList<>();
        // Itérer sur le nombre de chansons à charger et les ajouter à la liste du sous-ensemble
        for (int i = 0; i < limitToLoad; i++) {
            // Incrémenter l'index de la chanson actuelle
            currentSongIndex++;
            // Revenir au début de la playlist si l'index dépasse la taille de la playlist
            if (currentSongIndex >= getCurrentPlaylist().getSongs().size()) {
                currentSongIndex -= getCurrentPlaylist().getSongs().size();
            }
            // Ajouter la chanson à l'index de currentSongIndex à la liste du sous-ensemble
            sub.add(currentPlaylist.getSongs().get(currentSongIndex));
        }
        // Retourner le sous-ensemble de chansons
        return sub;
    }

    /**
     * Retourne l'ensemble des Musiques suivantes.
     */
    public List<Mp3file> getAllNext(){
        int currentSongIndex = dataManager.getCurrentSongIndex();
        return currentPlaylist.getSongs().subList(currentSongIndex+1, currentPlaylist.getSongs().size());
    }


    /**
     * Effectue des opérations de nettoyage lors de la terminaison de l'application.
     * Libère les ressources liées au lecteur audio, arrête le service de notification.
     */
    @Override
    public void onTerminate() {
        // Appel de la méthode onTerminate de la classe parente
        super.onTerminate();

        // Libération des ressources du lecteur audio
        mediaPlayer.release();
        mediaPlayer = null;

        if(mainListener != null){
            mainListener.playBar.setVisibility(View.INVISIBLE);
            mainListener.finish();
        }

        currentPlaylist = null;
        // Création d'une intention pour arrêter le service de notification
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }


    /**
     * Termine l'application en effectuant plusieurs actions de nettoyage.
     * Libère les ressources du lecteur audio, masque la barre de lecture,
     * termine l'activité principale, et arrête le service de notification.
     */
    public void terminate() {
        // Libération des ressources du lecteur audio
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
        mediaPlayer = null;
        currentPlaylist = null;

        // Masquage de la barre de lecture dans l'activité principale
        mainListener.playBar.setVisibility(View.INVISIBLE);

        // Appel de la méthode finish() pour terminer l'activité principale
        mainListener.finish();

        // Création d'une intention pour arrêter le service de notification
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }

    /**
     * Retourne la liste des playlists ou une liste vide si elles n'existent pas.
     *
     */
    public List<Playlist> getPlaylists() {
        if(playlists != null){
            return playlists;
        }
        return new ArrayList<>();

    }






    private static final String ALBUM_PLAYLISTS_FILE_PATH = "album_playlists.json";
    private static final String ARTIST_PLAYLISTS_FILE_PATH = "artist_playlists.json";
    private static final String USER_PLAYLISTS_FILE_PATH = "playlists";

    public void saveAlbumPlaylists(List<Playlist> albumPlaylists) {
        savePlaylists_f(albumPlaylists, ALBUM_PLAYLISTS_FILE_PATH);
    }

    public List<Playlist> loadAlbumPlaylists() {
        return loadPlaylists_f(ALBUM_PLAYLISTS_FILE_PATH);
    }

    public void saveArtistPlaylists(List<Playlist> artistPlaylists) {
        savePlaylists_f(artistPlaylists, ARTIST_PLAYLISTS_FILE_PATH);
    }

    public List<Playlist> loadArtistPlaylists() {
        return loadPlaylists_f(ARTIST_PLAYLISTS_FILE_PATH);
    }

    public void savePlaylists() {
        savePlaylists_f(playlists, USER_PLAYLISTS_FILE_PATH);
    }

    public List<Playlist> loadPlaylists() {
        return loadPlaylists_f(USER_PLAYLISTS_FILE_PATH);
    }

    private void savePlaylists_f(List<Playlist> playlists, String filePath) {
        Gson gson = new Gson();

        String jsonPlaylists = gson.toJson(playlists);

        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(filePath, jsonPlaylists);
        editor.apply();
    }

    private List<Playlist> loadPlaylists_f(String filePath) {
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);
        String jsonPlaylists = sharedPreferences.getString(filePath, null);

        if (jsonPlaylists != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Playlist>>() {}.getType();
            return gson.fromJson(jsonPlaylists, type);
        }

        return new ArrayList<>();
    }


    /**
     * Ajoute une playlist à la liste existante de playlists.
     * Configure le chemin de l'image de la playlist en utilisant le chemin de vignette de la première chanson.
     * Ajoute la playlist à l'indice 0 de la liste des playlists, ce qui la place en haut de la liste.
     * Enregistre la liste mise à jour dans les préférences partagées.
     *
     * @param p La playlist à ajouter.
     */
    public void addPlaylist(Playlist p) {
        // Configure le chemin de l'image de la playlist en utilisant le chemin de vignette de la première chanson
        p.setPathToImage(p.getSongs().get(0).getThumbnailPath());
        setCoverPlaylist(p);

        // Ajoute la playlist à l'indice 0 de la liste des playlists, plaçant ainsi la nouvelle playlist en haut de la liste
        playlists.add(0, p);

        // Enregistre la liste mise à jour dans les préférences partagées
        savePlaylists();
    }


    /**
     * Ajoute une chanson à la playlist actuelle après la chanson en cours de lecture.
     * Si une playlist est actuellement sélectionnée, la fonction ajoute la chanson spécifiée
     * à l'index suivant de la playlist, après la chanson en cours de lecture.
     *
     * @param file La chanson à ajouter à la playlist.
     */
    public void playNext(Mp3file file) {
        // Vérifie si une playlist est actuellement sélectionnée
        if (currentPlaylist != null) {
            // Ajoute la chanson spécifiée à l'index suivant de la playlist,
            // après la chanson en cours de lecture
            currentPlaylist.getSongs().add(dataManager.getCurrentSongIndex() + 1, file);
        }else{
            Playlist temp = new Playlist();
            temp.setPathToImage(file.getThumbnailPath());
            temp.setNom(file.getTitle());
            List<Mp3file> songs = new ArrayList<>();
            songs.add(file);
            temp.setSongs(songs);
            playNextPlaylist(temp);
        }
        refreshLecture();

    }

    public void playNextPlaylist(Playlist p){
        if(currentPlaylist != null){
            currentPlaylist = currentPlaylist.clone();
            for(int i = 0; i < p.getSongs().size(); i++){
                Mp3file file = p.getSongs().get(i);
                currentPlaylist.getSongs().add(dataManager.getCurrentSongIndex() + i + 1, file);
            }

        }else{
            playPlaylist(p);
        }
        refreshLecture();

    }


    /**
     * Ajoute une sélection de chansons à la playlist actuelle après la chanson en cours de lecture.
     * Si une playlist est actuellement sélectionnée, la fonction parcourt la liste de chansons spécifiée
     * et les ajoute à la playlist à des positions consécutives après la chanson en cours de lecture.
     * Ensuite, elle nettoie la sélection des fichiers.
     *
     * @param selection La liste des chansons à ajouter à la playlist.
     */
    public void playNextSelection(List<Mp3file> selection) {
        // Vérifie si une playlist est actuellement sélectionnée
        if (currentPlaylist != null) {
            // Parcourt la liste de chansons spécifiée
            for (int i = 0; i < selection.size(); i++) {
                Mp3file file = selection.get(i);
                // Ajoute chaque chanson à la playlist à des positions consécutives après la chanson en cours de lecture
                currentPlaylist.getSongs().add(dataManager.getCurrentSongIndex() + i + 1, file);
            }
        }

        // Nettoie la sélection des fichiers après les avoir ajoutés à la playlist
        clearSelectedFiles();
        refreshLecture();
    }


    /**
     * Commence la lecture d'une playlist spécifiée en créant une nouvelle playlist avec les mêmes chansons.
     * Crée une nouvelle playlist et copie toutes les informations de la playlist spécifiée, y compris le nom,
     * le chemin de l'image et la liste de chansons. Ensuite, définit la nouvelle playlist comme la playlist en cours
     * et commence la lecture de la première chanson de la nouvelle playlist. Affiche également la notification associée.
     *
     * @param p La playlist à jouer.
     */
    public void playPlaylist(Playlist p) {
        // Crée une nouvelle playlist
        Playlist newPlaylist = p.clone();

        // Définit la nouvelle playlist comme la playlist en cours
        currentPlaylist = newPlaylist;

        // Commence la lecture de la première chanson de la nouvelle playlist
        playAudio(0);

        // Affiche la notification associée à la nouvelle playlist
        showNotif();
    }


    /**
     * Ajoute une chanson à une playlist spécifiée à l'indice spécifié.
     * Si la playlist spécifiée n'a pas de chemin d'image défini, utilise le chemin de vignette de la chanson ajoutée.
     * Enregistre la liste des playlists mise à jour dans les préférences partagées.
     *
     * @param file La chanson à ajouter à la playlist.
     * @param index L'indice de la playlist dans la liste des playlists.
     */
    public void addToPlaylist(Mp3file file, int index) {
        // Ajoute la chanson à la playlist spécifiée à l'indice spécifié
        playlists.get(index).getSongs().add(file);

        setCoverPlaylist(playlists.get(index));

        // Enregistre la liste des playlists mise à jour dans les préférences partagées
        savePlaylists();
    }

    public void setCoverPlaylist(Playlist p){
        if(p.getSongs().size() == 0){
            return;
        }
        p.setPathToImage(p.getSongs().get(0).getThumbnailPath());
        if(p.getCoverImageBitmap(this) == null){
            for(int i = 0; i < p.getSongs().size(); i++){
                Mp3file file = p.getSongs().get(i);
                Bitmap cover = file.getCoverImageBitmap(this);
                if(cover != null){
                    p.setPathToImage(file.getThumbnailPath());
                    break;
                }
            }
        }
    }


    /**
     * Ajoute une sélection de chansons à une playlist spécifiée à l'indice spécifié.
     * Si la playlist spécifiée n'a pas de chemin d'image défini, utilise le chemin de vignette de la première chanson de la sélection.
     * Enregistre la liste des playlists mise à jour dans les préférences partagées.
     *
     * @param selection La liste des chansons à ajouter à la playlist.
     * @param index L'indice de la playlist dans la liste des playlists.
     */
    public void addToPlaylistSelection(List<Mp3file> selection, int index) {
        // Récupère la liste de chansons de la playlist spécifiée
        List<Mp3file> playlistSongs = playlists.get(index).getSongs();

        // Si la playlist spécifiée n'a pas de chemin d'image défini, utilise le chemin de vignette de la première chanson de la sélection
        if (playlists.get(index).getPathToImage() == null) {
            playlists.get(index).setPathToImage(selection.get(0).getThumbnailPath());
        }

        // Ajoute chaque chanson de la sélection à la liste de chansons de la playlist spécifiée
        for (int i = 0; i < selection.size(); i++) {
            playlistSongs.add(selection.get(i));
        }

        // Enregistre la liste des playlists mise à jour dans les préférences partagées
        savePlaylists();
    }

    /**
     * Supprime une playlist spécifiée de la liste des playlists.
     * Enregistre la liste des playlists mise à jour dans les préférences partagées
     * et recharge la liste des playlists à partir des préférences partagées.
     *
     * @param p La playlist à supprimer.
     */
    public void deletePlayList(Playlist p) {
        // Supprime la playlist spécifiée de la liste des playlists
        playlists.remove(p);

        // Enregistre la liste des playlists mise à jour dans les préférences partagées
        savePlaylists();

        // Recharge la liste des playlists à partir des préférences partagées
        loadPlaylists();
    }


    public void setAccueilFragment(AccueilFragment accueilFragment) {
        this.accueilFragment = accueilFragment;
    }

    public AccueilFragment getAccueilFragment() {
        return accueilFragment;
    }

    private ExplorerFragment explorerFragment;

    public void setExplorerFragment(ExplorerFragment explorerFragment) {
        this.explorerFragment = explorerFragment;
    }

    public ExplorerFragment getExplorerFragment() {
        return explorerFragment;
    }

    public void setMainListener(MainListener mainListener) {
        this.mainListener = mainListener;
    }

    public MainListener getMainListener() {
        return mainListener;
    }

    private Lecture lecture;

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public void refreshLecture(){
        if(lecture != null){
            lecture.refreshUI();
        }

    }

    public void refreshAccueilFragment(){
        if(accueilFragment != null){
            accueilFragment.back();
            accueilFragment.loadPlaylist();
        }else{
            mainListener.recreateAccueilFragment();
        }
    }

    public void refreshMainListener(){
        if(mainListener != null){
            mainListener.refreshUI();
        }
    }

    public void refreshUI(){
        //refreshAccueilFragment();
        refreshMainListener();
        refreshLecture();
    }

    private BibliothequeFragment bibliothequeFragment;

    public void setBibliothequeFragment(BibliothequeFragment bibliothequeFragment) {
        this.bibliothequeFragment = bibliothequeFragment;
    }

    public BibliothequeFragment getBibliothequeFragment() {
        return bibliothequeFragment;
    }

    public void refreshBiblio(){
        if(bibliothequeFragment != null){
            bibliothequeFragment.updateUI();
        }

    }

    public void clearSelectedFiles(){
        selectedFiles = 0;
        for (int i = 0; i <  allFiles.size(); i++) {
            if (allFiles.get(i).isSelected()){
                allFiles.get(i).setSelected(false);
            }
        }
        refreshBiblio();
        getBibliothequeFragment().updateAdapter();
    }
    private int selectedFiles = 0;

    public void setSelectedFilesCount(int selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public int getSelectedFilesCount() {
        return selectedFiles;
    }

    public List<Mp3file> getSelectedFiles(Playlist p){
        List<Mp3file> selected = new ArrayList<>();
        for (int i = 0; i <  p.getSongs().size(); i++) {
            if (p.getSongs().get(i).isSelected()){
                p.getSongs().get(i).setSelected(false);
                selected.add(p.getSongs().get(i));
            }
        }
        return selected;
    }



    NotificationService notificationService;
    public void showNotif(){
        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    private int notification_color;

    public void setNotification_color(int notification_color) {
        this.notification_color = adjustColorBrightness(notification_color, 0.7f);
    }


    private int adjustColorBrightness(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        //System.out.println("COLOR : " + (red + green + blue));
        if( (red + blue + green) > 400){
            red = Math.min((int) (Color.red(color) * factor), 255);
            green = Math.min((int) (Color.green(color) * factor), 255);
            blue = Math.min((int) (Color.blue(color) * factor), 255);
        }


        return Color.argb(alpha, red, green, blue);
    }

    public int getNotification_color() {
        return notification_color;
    }

    public void saveLightMode(){
        // Obtient une référence aux préférences partagées avec le nom "Owl" en mode privé
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);

        // Initialise un éditeur pour les préférences partagées
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Enregistre la liste de fichiers en format JSON sous la clé "mp3List"
        editor.putBoolean("lightMode", lightMode);
        // Applique les modifications à l'éditeur des préférences partagées
        editor.apply();

    }

    private boolean lightMode = false;
    public boolean loadLightMode(){
        SharedPreferences sharedPreferences = getSharedPreferences("Owl", Context.MODE_PRIVATE);

        lightMode = sharedPreferences.getBoolean("lightMode", false);
        return lightMode;
    }

    public void setLightMode(boolean lightMode) {
        this.lightMode = lightMode;
    }

    public boolean isLightMode() {
        return lightMode;
    }

    public boolean isNight(Context context){
        if(!isLightMode()){
            return true;
        }

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;

            case Configuration.UI_MODE_NIGHT_NO:

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
        }
        return false;
    }

    public void changeMode(){
        if(!lightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public void setArtistPreview(Mp3file mp3file){
        String author = mp3file.getArtist();
        List<Playlist> artistsPlaylists = loadArtistPlaylists();
        if(author.length() == 0){
            return;
        }
        if (author.endsWith(" - Topic")) {
            author = author.substring(0, author.lastIndexOf(" - Topic"));
        } else if (author.endsWith("VEVO")) {
            author = author.substring(0, author.lastIndexOf("VEVO"));
        }

        author = author.replace(" ", "");

        Playlist artistPlaylist = null;
        for(int i = 0; i < artistsPlaylists.size(); i++){
            String playlistName = artistsPlaylists.get(i).getNom();
            if (playlistName.endsWith(" - Topic")) {
                playlistName = playlistName.substring(0, playlistName.lastIndexOf(" - Topic"));
            } else if (playlistName.endsWith("VEVO")) {
                playlistName = playlistName.substring(0, playlistName.lastIndexOf("VEVO"));
            }

            playlistName = playlistName.replace(" ", "");
            if(playlistName.equals(author)){
                artistPlaylist = artistsPlaylists.get(i);
                break;
            }
        }

        if(artistPlaylist == null){
            return;
        }
        getLecture().quitLecture();
        getMainListener().goExplorer();
        playlistTemp = artistPlaylist;

    }

    Playlist playlistTemp = null;

    public void setPlaylistTemp(Playlist playlistTemp) {
        this.playlistTemp = playlistTemp;
    }

    public Playlist getPlaylistTemp() {
        return playlistTemp;
    }

    public void setAnimationText(TextView textView){
        TextPaint textPaint = textView.getPaint();
        float textWidth = textPaint.measureText(textView.getText().toString());

        float translationDistance = textWidth;
        Random random = new Random();
        int animationDuration = 9000 - random.nextInt(200);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.ABSOLUTE, translationDistance-110,
                Animation.ABSOLUTE, -translationDistance,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(animationDuration);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);

        textView.startAnimation(animation);
    }
}