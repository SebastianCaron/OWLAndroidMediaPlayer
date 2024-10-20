package com.sebastiancaron.owl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.sebastiancaron.owl.Fragments.AccueilFragment;
import com.sebastiancaron.owl.Fragments.BibliothequeFragment;
import com.sebastiancaron.owl.Fragments.ExplorerFragment;
import com.sebastiancaron.owl.Fragments.SettingsFragment;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Tools.DataManager;
import com.sebastiancaron.owl.databinding.ActivityMainListenerBinding;

import java.io.IOException;
import java.security.Key;
import java.util.List;

public class MainListener extends AppCompatActivity {

    private DataManager dataManager;
    private BottomNavigationView bottomNavigationView;
    public List<Mp3file> allFiles;

    private Owl owl;

    // Composants PLAYER UI
    public ConstraintLayout playBar;
    public ImageView cover, play;
    public TextView titre, artist;

    ProgressBar progressBar;
    Handler handler = new Handler(); // Handler pour mettre à jour la Progress Bar
    Runnable runnable;

    private final Fragment[] selectedFragment = {null};
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_listener);
        owl = Owl.getInstance();
        owl.setMainListener(this);
        //owl.createMediaPlayer();
        dataManager = Owl.getInstance().getDataManager();
        allFiles = Owl.getInstance().getFiles();
        Owl.getInstance().setAllFiles(allFiles);

        setPlayBar();

        // NAVIGATION EN BAS DE L'ECRAN
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                id = item.getItemId();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Masquer tous les fragments existants
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    transaction.hide(fragment);
                }
                Fragment oldFrag = selectedFragment[0];
                if (id == R.id.action_accueil) {
                    selectedFragment[0] = getSupportFragmentManager().findFragmentByTag("TAG_ACCUEIL_FRAGMENT");
                    if (selectedFragment[0] == null) {
                        selectedFragment[0] = new AccueilFragment();
                        transaction.add(R.id.fragment_container, selectedFragment[0], "TAG_ACCUEIL_FRAGMENT");
                    }else{
                        if(oldFrag != null && oldFrag.getTag().equals("TAG_ACCUEIL_FRAGMENT")){
                            owl.getAccueilFragment().back();
                        }
                    }
                } else if (id == R.id.action_explorer) {
                    selectedFragment[0] = getSupportFragmentManager().findFragmentByTag("TAG_EXPLORER_FRAGMENT");
                    if (selectedFragment[0] == null) {
                        selectedFragment[0] = new ExplorerFragment();
                        transaction.add(R.id.fragment_container, selectedFragment[0], "TAG_EXPLORER_FRAGMENT");
                    }
                    else{
                        if(oldFrag.getTag().equals("TAG_EXPLORER_FRAGMENT")){
                            owl.getExplorerFragment().back();
                        }
                    }
                } else if (id == R.id.action_settings) {
                    selectedFragment[0] = getSupportFragmentManager().findFragmentByTag("TAG_SETTINGS_FRAGMENT");
                    if (selectedFragment[0] == null) {
                        selectedFragment[0] = new SettingsFragment();
                        transaction.add(R.id.fragment_container, selectedFragment[0], "TAG_SETTINGS_FRAGMENT");
                    }
                } else {
                    // Vérifier si le fragment de la bibliothèque existe déjà
                    selectedFragment[0] = getSupportFragmentManager().findFragmentByTag("TAG_BIBLIOTHEQUE_FRAGMENT");
                    if (selectedFragment[0] == null) {
                        // Créer et attacher un nouveau fragment de la bibliothèque
                        selectedFragment[0] = new BibliothequeFragment();
                        transaction.add(R.id.fragment_container, selectedFragment[0], "TAG_BIBLIOTHEQUE_FRAGMENT");
                    }
                }

                // Montrer (ou attacher) le fragment sélectionné
                transaction.show(selectedFragment[0]);

                transaction.commitAllowingStateLoss();

                return true;
            }

        });
        // Afficher le fragment par défaut (Accueil)
        bottomNavigationView.setSelectedItemId(R.id.action_accueil);
    }

    public void goBiblio(){
        bottomNavigationView.setSelectedItemId(R.id.action_bibliotheque);
    }
    public void goExplorer(){
        bottomNavigationView.setSelectedItemId(R.id.action_explorer);
    }



    public void recreateAccueilFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new AccueilFragment(), "TAG_ACCUEIL_FRAGMENT");
    }

    private void setPlayBar() {
        playBar = findViewById(R.id.playBar);
        cover = findViewById(R.id.imageViewCover);
        play = findViewById(R.id.imageViewPlay);
        titre = findViewById(R.id.textViewTitre);
        artist = findViewById(R.id.textViewArtist);
        playBar.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progressBar3);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (owl.getMediaPlayer().isPlaying()) {
                    owl.pauseAudio();
                    play.setImageDrawable(getDrawable(R.drawable.ic_play));
                } else {
                    owl.continueAudio();
                    play.setImageDrawable(getDrawable(R.drawable.ic_pause));
                }
            }
        });


        playBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainListener.this, Lecture.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_up, R.anim.no_anim);
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = owl.getMediaPlayer();
                if (mediaPlayer != null) {
                    try {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        progressBar.setMax(owl.getMediaPlayer().getDuration());
                        progressBar.setProgress(currentPosition);
                    }catch (IllegalStateException e){

                    }


                }
                handler.postDelayed(this, 1000); // Répéter toutes les 1 seconde
            }
        };
        handler.postDelayed(runnable, 1000);

        refreshPlayBar();
    }

    void refreshPlayBar(){
        if(owl.getCurrentSong() != null){
            playBar.setVisibility(View.VISIBLE);
            if(owl.getMediaPlayer() != null && owl.getMediaPlayer().isPlaying()){
                play.setImageDrawable(getDrawable(R.drawable.ic_pause));
            }else{
                play.setImageDrawable(getDrawable(R.drawable.ic_play));
            }
            titre.setText(owl.getCurrentSong().getTitle());
            artist.setText(owl.getCurrentSong().getArtist());

            if(owl.getCurrentSong().getCoverImageBitmap(getApplicationContext()) == null){
                Drawable drawable = this.getDrawable(R.drawable.ic_note);
                if(owl.isNight(this)){
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                }
                cover.setImageDrawable(drawable);
                //cover.setImageDrawable(getDrawable(R.drawable.ic_note));
            }else{
                cover.setImageBitmap(owl.getCurrentSong().getCoverImageBitmap(getApplicationContext()));
            }
        }else{
            playBar.setVisibility(View.INVISIBLE);
        }
        float textWidth = titre.getPaint().measureText(titre.getText().toString());
        titre.clearAnimation();
        if(textWidth > 800){
            owl.setAnimationText(titre);
            titre.setWidth((int) (textWidth + 400));
        }else{
            titre.setAnimation(null);
        }
        textWidth = artist.getPaint().measureText(artist.getText().toString());
        artist.clearAnimation();
        if(textWidth > 800){
            owl.setAnimationText(artist);
            artist.setWidth((int) (textWidth + 400));
        }else{
            artist.setAnimation(null);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        MediaPlayer mediaPlayer = owl.getMediaPlayer();
        if(mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                play.setImageDrawable(getDrawable(R.drawable.ic_pause));
            } else {
                play.setImageDrawable(getDrawable(R.drawable.ic_play));

            }
        }
        refreshUI();
    }

    public void refreshUI(){
        MediaPlayer mediaPlayer = owl.getMediaPlayer();
        if(mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                play.setImageDrawable(getDrawable(R.drawable.ic_pause));
            } else {
                play.setImageDrawable(getDrawable(R.drawable.ic_play));

            }
        }
        refreshPlayBar();

    }

    /*

     */
    @Override
    protected void onDestroy() {
        owl.terminate();
        super.onDestroy();
    }


    /*
    Actualise les differents fragments de l'activité.
     */
    public void refreshFragment(){
        if (id == R.id.action_accueil) {
            owl.getAccueilFragment().loadPlaylist();
        } else if (id == R.id.action_explorer) {
            selectedFragment[0] = new ExplorerFragment();
        } else {
            if(owl.getBibliothequeFragment() != null){
                selectedFragment[0] = owl.getBibliothequeFragment();
            }else{
                selectedFragment[0] = new BibliothequeFragment();
            }

        }

        if (selectedFragment[0] != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, selectedFragment[0]);
            transaction.commit();
        }
    }



    /**
     *  Le code suivant permet, lorsque l'utilisateur est sur l'activité principale, de changer la musique lorsque
     *  l'on reste appuyé un certain delay sur les boutons volume + et -
     */
//    private boolean volumeUpPressed = false;
//    private long volumeUpPressStartTime = 0;
//    private boolean volumeDownPressed = false;
//    private long volumeDownPressStartTime = 0;
//    private final int delay = 2000; // VARIABLE A MODIFIER POUR CHANGER LE DELAY
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                if (!volumeUpPressed) {
//                    volumeUpPressed = true;
//                    volumeUpPressStartTime = System.currentTimeMillis();
//                    return false;
//                }
//            }
//
//            return true;
//        }
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                if (!volumeDownPressed) {
//                    volumeDownPressed = true;
//                    volumeDownPressStartTime = System.currentTimeMillis();
//                    return false;
//                }
//            }
//
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//            if (event.getAction() == KeyEvent.ACTION_UP) {
//                volumeUpPressed = false;
//                long pressDuration = System.currentTimeMillis() - volumeUpPressStartTime;
//                if (pressDuration >= delay) {
//                    owl.nextSong();
//                    owl.refreshUI();
//                    return true;
//                }
//            }
//            return false;
//        }
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
//            if (event.getAction() == KeyEvent.ACTION_UP) {
//                volumeDownPressed = false;
//                long pressDuration = System.currentTimeMillis() - volumeDownPressStartTime;
//                if (pressDuration >= delay) {
//                    owl.previousSong();
//                    owl.refreshUI();
//                    return true;
//                }
//            }
//            return false;
//        }
//        return super.onKeyUp(keyCode, event);
//
//    }

}