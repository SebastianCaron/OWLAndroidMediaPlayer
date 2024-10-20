package com.sebastiancaron.owl.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;
import com.sebastiancaron.owl.Tools.PlaylistDialogAdapter;

public class CustomDialogPlaylist extends Dialog {

    private final Context context;
    private Owl owl;
    private final Mp3file file;
    public CustomDialogPlaylist(@NonNull Context context, int style, Mp3file file) {
        super(context, style);
        this.context = context;
        this.file = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owl = Owl.getInstance();
        // Enlever le titre du dialogue
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_playlist_layout, null);
        setContentView(view);

        ImageView clear = view.findViewById(R.id.imageViewClearDialog);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button creerPlaylist = findViewById(R.id.buttonCreerPlaylist);
        creerPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                CustomDialogCreatePlaylist customDialog = new CustomDialogCreatePlaylist(getContext(), file);
                customDialog.show();
            }
        });

        ListView listView = view.findViewById(R.id.listViewPlaylist);
        PlaylistDialogAdapter adapter = new PlaylistDialogAdapter(context, owl.getPlaylists(),file);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, context.getString(R.string.titre_ajout) + owl.getPlaylists().get(position).getNom(), Toast.LENGTH_SHORT).show();
                owl.addToPlaylist(file, position);
                dismiss();
            }
        });

        listView.setAdapter(adapter);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        // Placer le dialogue en bas
        layoutParams.gravity = Gravity.BOTTOM;

        // Réglez la largeur et la hauteur
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

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
