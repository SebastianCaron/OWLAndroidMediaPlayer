package com.sebastiancaron.owl.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.List;

public class CustomDialogDownSelection extends Dialog {

    private final Context context;
    private final int style;
    private final List<Mp3file> selection;
    private Owl owl;

    public CustomDialogDownSelection(@NonNull Context context, int style, List<Mp3file> selection){
        super(context, style);
        this.context = context;
        this.style = style;
        this.selection = selection;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owl = Owl.getInstance();
        // Enlever le titre du dialogue
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_down_selection_layout, null);
        setContentView(view);

        ConstraintLayout addPlaylist, addEnsuite;

        addPlaylist = view.findViewById(R.id.constraintLayoutPlaylist);
        addEnsuite = view.findViewById(R.id.constraintLayoutEnsuite);

        addEnsuite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                owl.playNextSelection(selection);
                dismiss();
            }
        });

        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                CustomDialogPlaylistSelection customDialog = new CustomDialogPlaylistSelection(getContext(), R.style.CustomDialogTheme, selection);
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();
            }
        });

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
