package com.sebastiancaron.owl.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.ArrayList;
import java.util.List;

public class CustomDialogCreatePlaylistSelection extends Dialog{

    private final Context context;
    private Owl owl;
    private final List<Mp3file> selection;

    public CustomDialogCreatePlaylistSelection(@NonNull Context context, List<Mp3file> selection){
        super(context);
        this.context = context;
        this.selection = selection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_create_playlist_layout, null);
        setContentView(view);
        owl = Owl.getInstance();
        EditText editText = view.findViewById(R.id.editTextTitre);
        Button cancel = view.findViewById(R.id.buttonCancel);
        Button creer = view.findViewById(R.id.buttonCreer);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        creer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().length()!= 0){
                    // Creer Playlist
                    Playlist p = new Playlist();
                    p.setNom(editText.getText().toString());
                    p.setSongs(selection);
                    owl.addPlaylist(p);
                    Toast.makeText(getContext(), context.getString(R.string.playlist_ajout_e), Toast.LENGTH_SHORT).show();
                    owl.refreshAccueilFragment();
                    dismiss();
                }else{
                    Toast.makeText(context, context.getString(R.string.choisissez_un_titre), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
