package com.sebastiancaron.owl.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;
import com.sebastiancaron.owl.Tools.RearrangeAdapter;

import java.util.Collections;

public class CustomDialogEdit extends Dialog {

    private final Playlist p;
    private Owl owl;
    private EditText nom;
    private RecyclerView listView;
    private RearrangeAdapter adapter;
    private final Context context;



    public CustomDialogEdit(@NonNull Context context, int style, Playlist p){
        super(context, style);
        this.p = p;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owl = Owl.getInstance();
        // Enlever le titre du dialogue
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog_edit, null);
        setContentView(view);

        EditText editText = findViewById(R.id.editTextNom);
        editText.setText(p.getNom());
        Button ok = findViewById(R.id.buttonOk);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(editText.getText().length() > 0){
                    p.setNom(editText.getText().toString());
                }
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owl.isProcessPlaylistRunning()){
                    Toast.makeText(owl, owl.getString(R.string.wait), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(editText.getText().length() > 0){
                    p.setNom(editText.getText().toString());
                }
                owl.setCoverPlaylist(p);
                owl.savePlaylists();
                owl.refreshAccueilFragment();
                dismiss();
            }
        });

        listView = findViewById(R.id.listView);
        adapter = new RearrangeAdapter(context, p.getSongs());
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(listView);


        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();


        layoutParams.gravity = Gravity.BOTTOM;


        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        getWindow().setAttributes(layoutParams);

    }
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(p.getSongs(), fromPosition, toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

}
