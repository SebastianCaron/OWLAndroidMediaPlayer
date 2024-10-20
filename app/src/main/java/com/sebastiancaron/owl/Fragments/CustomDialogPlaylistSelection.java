package com.sebastiancaron.owl.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;
import com.sebastiancaron.owl.Tools.PlaylistDialogAdapterSelection;

import java.util.List;

public class CustomDialogPlaylistSelection extends Dialog {

    private final Context context;
    private Owl owl;
    private final List<Mp3file> selection;
    public CustomDialogPlaylistSelection(@NonNull Context context, int style, List<Mp3file> selection) {
        super(context, style);
        this.context = context;
        this.selection = selection;
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
                CustomDialogCreatePlaylistSelection customDialog = new CustomDialogCreatePlaylistSelection(getContext(), selection);
                customDialog.show();
            }
        });

        ListView listView = view.findViewById(R.id.listViewPlaylist);
        PlaylistDialogAdapterSelection adapter = new PlaylistDialogAdapterSelection(context, owl.getPlaylists(),selection);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, context.getString(R.string.titre_ajout) + owl.getPlaylists().get(position).getNom(), Toast.LENGTH_SHORT).show();
                owl.addToPlaylistSelection(selection, position);
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
