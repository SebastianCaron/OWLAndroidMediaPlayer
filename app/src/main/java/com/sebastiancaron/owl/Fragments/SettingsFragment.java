package com.sebastiancaron.owl.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sebastiancaron.owl.MainActivity;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

public class SettingsFragment extends Fragment {


    private Button reloadFiles;
    private Owl owl;
    private Switch switchMode;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_layout, container, false);
        reloadFiles = rootView.findViewById(R.id.buttonReloadFiles);
        owl = Owl.getInstance();
        switchMode = rootView.findViewById(R.id.switchMode);

        switchMode.setChecked(owl.isLightMode());
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                owl.setLightMode(isChecked);
                owl.saveLightMode();
                owl.changeMode();
            }
        });

        reloadFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReloadDialog(rootView.getContext());
            }
        });
        return rootView;
    }


    // Fonction pour afficher la boîte de dialogue de confirmation
    private void showReloadDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustomDefault);

        builder.setTitle("Recharger les données")
                .setMessage("Êtes-vous sûr de vouloir recharger l'ensemble des données ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Appel de la fonction pour gérer le rechargement des données

                        owl.clear();
                        owl.savePlaylists();
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                        owl.terminate();

                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ne rien faire si l'utilisateur clique sur "Non" (la boîte de dialogue se fermera)
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        // Affichage de la boîte de dialogue
        dialog.show();
    }
}
