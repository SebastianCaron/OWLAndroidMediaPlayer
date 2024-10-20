package com.sebastiancaron.owl.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

public class CustomDialogMorePlaylist extends Dialog {

    public CustomDialogMorePlaylist(@NonNull Context context, int style, Playlist p){
        super(context, style);
        this.p = p;
        this.context = context;
    }
    private final Playlist p;
    private Owl owl;
    private ConstraintLayout edit, delete, next;
    private TextView titre, nTitre;
    private ImageView cover;
    private final Context context;
    private String from = "";

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owl = Owl.getInstance();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_more_playlist_layout, null);
        setContentView(view);
        edit = view.findViewById(R.id.constraintLayoutEdit);
        delete = view.findViewById(R.id.constraintLayoutDelete);
        next = view.findViewById(R.id.constraintLayoutNext);

        if(from.equals("explorer")){
            edit.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
        }

        titre = view.findViewById(R.id.textViewTitre);
        nTitre = view.findViewById(R.id.textViewnTitres);
        cover = view.findViewById(R.id.imageViewCoverDialog);

        if(p.getCoverImageBitmap(context) != null){
            cover.setImageBitmap(p.getCoverImageBitmap(context));
        }else{
            Drawable drawable = getContext().getDrawable(R.drawable.ic_note);
            if(owl.isNight(getContext())){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
            cover.setImageDrawable(drawable);
        }
        titre.setText(p.getNom());
        nTitre.setText(p.getSongs().size() + " " + context.getString(R.string.titres));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                owl.playNextPlaylist(p);
                Toast.makeText(getContext(), getContext().getString(R.string.ajout_la_liste_de_lecture), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owl.isProcessPlaylistRunning()){
                    Toast.makeText(owl, owl.getString(R.string.wait), Toast.LENGTH_SHORT).show();
                    return;
                }
                CustomDialogEdit customDialogEdit = new CustomDialogEdit(context, R.style.CustomDialogTheme, p);
                customDialogEdit.show();
                dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owl.isProcessPlaylistRunning()){
                    Toast.makeText(owl, owl.getString(R.string.wait), Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustomDefault);
                builder.setMessage(context.getString(R.string.supprimer_la_playlist_q))
                        .setPositiveButton(context.getString(R.string.delete), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                owl.deletePlayList(p);
                                owl.refreshAccueilFragment();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.annuler), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                dismiss();
            }
        });

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        layoutParams.gravity = Gravity.BOTTOM;

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Hauteur fixée

        getWindow().setAttributes(layoutParams);
        //Config


    }

    @Override
    public void onStart() {
        super.onStart();
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
