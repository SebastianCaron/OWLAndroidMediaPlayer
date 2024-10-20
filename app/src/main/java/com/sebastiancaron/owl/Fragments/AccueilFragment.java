package com.sebastiancaron.owl.Fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastiancaron.owl.MainListener;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;
import com.sebastiancaron.owl.Tools.Mp3RecyclerAdapter;
import com.sebastiancaron.owl.Tools.PlaylistAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AccueilFragment extends Fragment {

    private MainListener mainActivity;
    private List<Playlist> playlists = new ArrayList<>();

    private ConstraintLayout accueil, playlistPreview;
    private Owl owl;

    private ImageView back, more, cover;
    private TextView titre;
    private ImageButton play;
    private RecyclerView playlistSongs;


    private AccueilFragment accueilFragment;
    private RecyclerView recyclerViewPlaylists;
    private RecyclerView recyclerViewSuggestions;
    private NestedScrollView scrollView;

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_accueil, container, false);
        mainActivity = (MainListener) getActivity();
        owl = Owl.getInstance();
        accueilFragment = this;
        owl.setAccueilFragment(this);

        recyclerViewPlaylists = rootView.findViewById(R.id.recyclerViewPlaylists);
        accueil = rootView.findViewById(R.id.accueil);
        playlistPreview = rootView.findViewById(R.id.playlistPreview);
        back = rootView.findViewById(R.id.imageViewBack);
        more = rootView.findViewById(R.id.imageViewPlaylistMore);
        cover = rootView.findViewById(R.id.imageViewPlaylistCover);
        titre = rootView.findViewById(R.id.textViewPlaylistTitre);
        play = rootView.findViewById(R.id.imageButtonPlaylistPlay);
        playlistSongs = rootView.findViewById(R.id.recyclerViewPlaylistSongs);

        scrollView = rootView.findViewById(R.id.scrollPreview);

        accueil.setVisibility(View.VISIBLE);
        playlistPreview.setVisibility(View.INVISIBLE);
        playlistSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPlaylist();

        recyclerViewSuggestions = rootView.findViewById(R.id.recyclerViewSuggestions);

        ImageView refresh = rootView.findViewById(R.id.imageViewRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSelection();
            }
        });

        playlistSongs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(scrollView.getScrollY() < 1165){
                    scrollView.scrollTo(0,scrollView.getScrollY() + dy);
                }
            }
        });


        refreshSelection();


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadPlaylist();
    }

    public void loadPlaylist(){
        playlists = new ArrayList<>();
        Playlist p = new Playlist();
        p.setNom("Tous");
        p.setSongs(Owl.getInstance().getFiles());
        if(p.getSongs() != null && p.getSongs().size() > 0){
            p.setPathToImage(p.getSongs().get(0).getThumbnailPath());
        }
        playlists.add(0,p);
        playlists.addAll(owl.getPlaylists());

        PlaylistAdapter adapter = new PlaylistAdapter(getContext(), playlists);
        adapter.setAccueilFragment(this);

        recyclerViewPlaylists.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);

        recyclerViewPlaylists.setLayoutManager(layoutManager);
    }

    public void back(){
        accueil.setVisibility(View.VISIBLE);
        playlistPreview.setVisibility(View.INVISIBLE);
    }

    public void refreshSelection(){
        Playlist playlist = new Playlist();
        List<Mp3file> disponibles = new ArrayList<>(owl.getFiles());
        Collections.shuffle(disponibles, new Random());
        playlist.setNom(getString(R.string.selection_rapide));
        int size = Math.min(20, disponibles.size());
        disponibles = disponibles.subList(0, size);
        if(disponibles.size() > 0){
            playlist.setPathToImage(disponibles.get(0).getThumbnailPath());
        }
        playlist.setSongs(disponibles);

        Mp3RecyclerAdapter adapter = new Mp3RecyclerAdapter(getContext(), playlist);
        recyclerViewSuggestions.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4, GridLayoutManager.HORIZONTAL, false);
        recyclerViewSuggestions.setLayoutManager(layoutManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        owl.setAccueilFragment(null);
    }

    public void setPlaylistPreview(List<Playlist> playlists, int index){
        if(index == 0){
            mainActivity.goBiblio();
            return;
        }
        Playlist playlist = playlists.get(index);

        accueil.setVisibility(View.INVISIBLE);
        playlistPreview.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EDIT ETC...
                CustomDialogMorePlaylist morePlaylist = new CustomDialogMorePlaylist(getContext(), R.style.CustomDialogTheme, playlist);
                morePlaylist.setCanceledOnTouchOutside(true);
                morePlaylist.show();
            }
        });

        if (playlist.getCoverImageBitmap(getContext()) != null) {
            cover.setImageBitmap(playlist.getCoverImageBitmap(getContext()));
        } else {
            Drawable drawable = getContext().getDrawable(R.drawable.ic_note);
            if(owl.isNight(getContext())){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
            cover.setImageDrawable(drawable);

        }

        titre.setText(playlist.getNom());

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PLAY PLAYLIST
                if (playlist.getSongs().size() > 0) {
                    owl.playPlaylist(playlist);
                    owl.refreshMainListener();
                }
            }
        });

        Mp3RecyclerAdapter adapter = new Mp3RecyclerAdapter(getContext(), playlist);
        playlistSongs.setAdapter(adapter);
        RecyclerView.Adapter listadp = playlistSongs.getAdapter();
        int totalHeight = 0;
        int measuredHeight = 232;
        totalHeight += measuredHeight * Math.min(8,playlist.getSongs().size());

        totalHeight += measuredHeight;
        ViewGroup.LayoutParams params = playlistSongs.getLayoutParams();
        params.height = totalHeight + ((listadp.getItemCount() - 1));
        playlistSongs.setLayoutParams(params);
        playlistSongs.requestLayout();
        playlistSongs.setAdapter(adapter);
    }
}
