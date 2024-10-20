package com.sebastiancaron.owl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sebastiancaron.owl.Fragments.CustomDialogCreatePlaylistSelection;
import com.sebastiancaron.owl.Fragments.CustomDialogDownSelection;
import com.sebastiancaron.owl.Fragments.CustomDialogPlaylistSelection;
import com.sebastiancaron.owl.Objects.CustomSeekBar;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Tools.DataManager;
import com.sebastiancaron.owl.Tools.Mp3ListAdapter;
import com.sebastiancaron.owl.Tools.Mp3RecyclerViewAdapter;

import java.io.IOException;
import java.util.ArrayList;

public class Lecture extends AppCompatActivity {

    private ImageView album, dropdown, play, next, previous, background;
    private TextView titre, artiste, duration, actualDuration;
    private RecyclerView titrenext;
    private SeekBar progressBar;

    private Mp3ListAdapter adapter;


    DataManager dataManager;
    private MediaPlayer mediaPlayer;
    private Owl owl;

    final Handler handler = new Handler(); // MAJ DE LA SEEK BAR
    Runnable runnable;

    private final int limitToLoad = 15;
    private int progressBarHeight = 0;
    private ConstraintLayout.LayoutParams layoutParams;
    private Drawable oldProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);
        getExtras();
        owl.setLecture(this);

        //Initialisation
        dropdown = findViewById(R.id.imageViewDropdown);
        album = findViewById(R.id.imageViewAlbum);
        play = findViewById(R.id.imageViewPlay);
        next = findViewById(R.id.imageViewNext);
        previous = findViewById(R.id.imageViewPrevious);
        background = findViewById(R.id.imageViewBackground);

        titre = findViewById(R.id.textViewTitre);
        artiste = findViewById(R.id.textViewArtiste);
        duration = findViewById(R.id.textViewDuration);
        actualDuration = findViewById(R.id.textViewActualDuration);

        titrenext = findViewById(R.id.recyclerViewNext);

        progressBar = findViewById(R.id.seekBar);
        oldProgress = progressBar.getProgressDrawable();
        layoutParams = (ConstraintLayout.LayoutParams)  progressBar.getLayoutParams();
        progressBarHeight = layoutParams.height;

        ImageView add = findViewById(R.id.imageViewAdd);
        Context context = this;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogPlaylistSelection dialog = new CustomDialogPlaylistSelection(context, R.style.CustomDialogTheme, new ArrayList<>(owl.getCurrentPlaylist().getSongs()));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

        initializeComponents();





        dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitLecture();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    next();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    previous();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){

                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        titrenext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                owl.playAudio((dataManager.getCurrentSongIndex() + position + 1) % owl.getCurrentPlaylist().getSongs().size());
//
//                initializeComponents();
//            }
//        });


        ImageView repeat, shuffle;

        repeat = findViewById(R.id.imageViewRepeat);
        shuffle = findViewById(R.id.imageViewShuffle);

        if(!owl.isLooping()){
            if(owl.isNight(context)){
                repeat.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }else{
                repeat.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            }
        }else{
            if(owl.isNight(context)){
                repeat.setColorFilter(getColor(R.color.accent_night));
            }else{
                repeat.setColorFilter(getColor(R.color.accent));
            }
        }
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owl.isLooping()){
                    if(owl.isNight(context)){
                        repeat.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }else{
                        repeat.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    }
                }else{
                    if(owl.isNight(context)){
                        repeat.setColorFilter(getColor(R.color.accent_night));
                    }else{
                        repeat.setColorFilter(getColor(R.color.accent));
                    }
                }
                owl.setLooping(!owl.isLooping());
            }
        });

        if(!owl.isShuffle()){
            if(owl.isNight(context)){
                shuffle.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }else{
                shuffle.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            }
        }else{
            if(owl.isNight(context)){
                shuffle.setColorFilter(getColor(R.color.accent_night));
            }else{
                shuffle.setColorFilter(getColor(R.color.accent));
            }
        }
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owl.isShuffle()){
                    //Toast.makeText(context, "Lecture aléatoire desactivée !", Toast.LENGTH_SHORT).show();
                    if(owl.isNight(context)){
                        shuffle.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    }else{
                        shuffle.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    }
                }else{
                    //Toast.makeText(context, "Lecture aléatoire activée !", Toast.LENGTH_SHORT).show();
                    if(owl.isNight(context)){
                        shuffle.setColorFilter(getColor(R.color.accent_night));
                    }else{
                        shuffle.setColorFilter(getColor(R.color.accent));
                    }
                }
                owl.setShuffle(!owl.isShuffle());
            }
        });



    }

    private void getExtras(){
        dataManager = Owl.getInstance().getDataManager();
        owl = Owl.getInstance();
        mediaPlayer = owl.getMediaPlayer();

    }



    private void initializeComponents(){

        Mp3file currentSong = owl.getCurrentSong();
        Mp3RecyclerViewAdapter adapter = new Mp3RecyclerViewAdapter(this, owl.getSubPlaylist());
        adapter.setOnItemClickListener(position -> {
            owl.playAudio((dataManager.getCurrentSongIndex() + position + 1) % owl.getCurrentPlaylist().getSongs().size());
            initializeComponents();
        });
        titrenext.setAdapter(adapter);
        titrenext.setLayoutManager(new LinearLayoutManager(this));

        if(mediaPlayer.isPlaying()){
            play.setImageDrawable(getDrawable(R.drawable.ic_pause));
        }else{
            play.setImageDrawable(getDrawable(R.drawable.ic_play));
        }
        Bitmap cover_bitmap = currentSong.getCoverImageBitmap(this);



        if(cover_bitmap != null){
            album.setImageBitmap(cover_bitmap);
            if(owl.isNight(this)){
                background.setVisibility(View.VISIBLE);

                Palette.from(cover_bitmap).generate(palette -> {
                    int vibrantColor = palette.getDominantColor(ContextCompat.getColor(this, android.R.color.transparent));
                    owl.setNotification_color(vibrantColor);
                    createGradient(vibrantColor, Color.TRANSPARENT);
                });
            }
        }else{
            if(owl.isNight(this)){
                owl.setNotification_color(getResources().getColor(R.color.background_night));
            }else{
                owl.setNotification_color(getResources().getColor(R.color.background));
            }
            Drawable drawable = this.getDrawable(R.drawable.ic_note);
            background.setVisibility(View.INVISIBLE);
            if(owl.isNight(this)){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
            album.setImageDrawable(drawable);


        }

        if(cover_bitmap == null && owl.isNight(this)){
            layoutParams.height = progressBarHeight;
            progressBar.setLayoutParams(layoutParams);
            progressBar.setProgressDrawable(oldProgress);


            ConstraintLayout.LayoutParams layoutParams1 = (ConstraintLayout.LayoutParams) duration.getLayoutParams();
            layoutParams1.topMargin = 0;
            duration.setLayoutParams(layoutParams1);
            ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) actualDuration.getLayoutParams();
            layoutParams2.topMargin = 0;
            actualDuration.setLayoutParams(layoutParams2);
        }
        titre.setText(currentSong.getTitle());
        artiste.setText(currentSong.getArtist());

        artiste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                owl.setArtistPreview(owl.getCurrentSong());
            }
        });

        progressBar.setMax(mediaPlayer.getDuration());
        duration.setText(dataManager.getFormattedDuration(mediaPlayer.getDuration()/1000));
        actualDuration.setText(dataManager.getFormattedDuration(mediaPlayer.getCurrentPosition()));
        //progressBar.setProgress(mediaPlayer.getCurrentPosition());


        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    try {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        actualDuration.setText(dataManager.getFormattedDuration(currentPosition/1000));
                        progressBar.setProgress(currentPosition);
                    }catch (IllegalStateException e){

                    }


                }
                handler.postDelayed(this, 100); // Répétez toutes les 0.5 seconde
            }
        };
        handler.postDelayed(runnable, 100);
//        if(titrenext.getCount() > 0){
//            Adapter listadp = titrenext.getAdapter();
//            int totalHeight = 0;
//            View listItem = listadp.getView(0, null, titrenext);
//            listItem.measure(0, 0);
//            int measuredHeight = listItem.getMeasuredHeight();
//            totalHeight += titrenext.getCount() * measuredHeight;
//            ViewGroup.LayoutParams params = titrenext.getLayoutParams();
//            params.height = totalHeight + (titrenext.getDividerHeight() * (listadp.getCount() - 1));
//            titrenext.setLayoutParams(params);
//            titrenext.requestLayout();
//        }




    }

    private void play(){
        if(mediaPlayer.isPlaying()){
            owl.pauseAudio();
            play.setImageDrawable(getDrawable(R.drawable.ic_play));
        }else{
            owl.continueAudio();
            play.setImageDrawable(getDrawable(R.drawable.ic_pause));
        }
    }

    private void next() throws IOException {
        owl.nextSong();
        initializeComponents();
    }

    private void previous() throws IOException {
        owl.previousSong();
        initializeComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        owl.setLecture(null);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        quitLecture();

    }

    public void quitLecture(){
        handler.removeCallbacks(runnable);
        owl.setLecture(null);
        finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_down);
    }

    public void refreshUI(){
        initializeComponents();
    }

    private GradientDrawable createGradient(int topColor, int bottomColor) {
        int color = topColor;
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{color, bottomColor}
        );
        background.setImageDrawable(gradientDrawable);
        changeProgressBar(topColor);
        // NB : ASYNC
        return gradientDrawable;
    }

    private void changeProgressBar(int color){
        int filledColor = adjustColorBrightness(color, 3f);
        int unfilledColor = adjustColorBrightness(color, 0.5f);

        layoutParams.height = progressBarHeight / 2;
        progressBar.setLayoutParams(layoutParams);

        ConstraintLayout.LayoutParams layoutParams1 = (ConstraintLayout.LayoutParams) duration.getLayoutParams();
        layoutParams1.topMargin = (int) getResources().getDimension(R.dimen.margin_progress_bar);
        duration.setLayoutParams(layoutParams1);
        ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) actualDuration.getLayoutParams();
        layoutParams2.topMargin = (int) getResources().getDimension(R.dimen.margin_progress_bar);
        actualDuration.setLayoutParams(layoutParams2);

        CustomSeekBar.setCustomSeekBar(progressBar, unfilledColor, filledColor);
    }

    private int adjustColorBrightness(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Math.min((int) (Color.red(color) * factor), 255);
        int green = Math.min((int) (Color.green(color) * factor), 255);
        int blue = Math.min((int) (Color.blue(color) * factor), 255);

        return Color.argb(alpha, red, green, blue);
    }
}