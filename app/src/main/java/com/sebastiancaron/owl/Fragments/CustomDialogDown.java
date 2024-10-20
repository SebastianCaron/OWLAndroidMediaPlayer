package com.sebastiancaron.owl.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.List;

public class CustomDialogDown extends Dialog {

    private final String title;
    private final String artist;
    private final Bitmap thumbnail;

    private ConstraintLayout addPlaylist, addEnsuite;
    private final Mp3file file;
    private Owl owl;

    public CustomDialogDown(@NonNull Context context, int style, Mp3file file) {
        super(context, style);
        this.file = file;
        this.artist = file.getArtist();
        this.thumbnail = file.getCoverImageBitmap(context);
        this.title = file.getTitle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owl = Owl.getInstance();
        // Enlever le titre du dialogue
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_down_layout, null);
        setContentView(view);

        TextView titleT = view.findViewById(R.id.textViewTitre);
        TextView artistT = view.findViewById(R.id.textViewArtist);
        ImageView thumI = view.findViewById(R.id.imageViewCoverDialog);

        addPlaylist = view.findViewById(R.id.constraintLayoutPlaylist);
        addEnsuite = view.findViewById(R.id.constraintLayoutEnsuite);

        addEnsuite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                owl.playNext(file);
                Toast.makeText(getContext(), getContext().getString(R.string.ajout_la_liste_de_lecture), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                CustomDialogPlaylist customDialog = new CustomDialogPlaylist(getContext(), R.style.CustomDialogTheme, file);
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();
            }
        });

        if(thumbnail != null){
            thumI.setImageBitmap(thumbnail);
        }

        titleT.setText(title);
        float textWidth = titleT.getPaint().measureText(titleT.getText().toString());
        if(textWidth > 800){
            owl.setAnimationText(titleT);
            titleT.setWidth((int) (textWidth + 400));
        }

        artistT.setText(artist);
        textWidth = artistT.getPaint().measureText(artistT.getText().toString());
        if(textWidth > 800){
            owl.setAnimationText(artistT);
            artistT.setWidth((int) (textWidth + 400));
        }

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        // Placer le dialogue en bas
        layoutParams.gravity = Gravity.BOTTOM;

        // Réglez la largeur et la hauteur
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = 500; // Hauteur fixée

        // Appliquer les paramètres à la fenêtre
        getWindow().setAttributes(layoutParams);
        //Config
    }

    @Override
    public void onStart() {
        super.onStart();
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
