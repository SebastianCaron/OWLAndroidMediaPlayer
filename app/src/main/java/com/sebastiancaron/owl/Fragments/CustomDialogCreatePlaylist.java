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

public class CustomDialogCreatePlaylist extends Dialog{

    private final Context context;
    private Owl owl;
    private final Mp3file mp3file;

    public CustomDialogCreatePlaylist(@NonNull Context context, Mp3file mp3file){
        super(context);
        this.context = context;
        this.mp3file = mp3file;
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
                    List<Mp3file> list = new ArrayList<>();
                    list.add(mp3file);
                    p.setSongs(list);
                    owl.addPlaylist(p);
                    Toast.makeText(getContext(), "Playlist ajoutée !", Toast.LENGTH_SHORT).show();
                    owl.refreshAccueilFragment();
                    dismiss();
                }else{
                    Toast.makeText(context, "Choisissez un Titre", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
