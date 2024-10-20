package com.sebastiancaron.owl.Fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExplorerFragment extends Fragment {
    private Owl owl;
    private List<Playlist> auteurs;
    private List<Playlist> albums;

    private MainListener mainActivity;
    private ExplorerFragment explorerFragment;

    private ScrollView explorer;
    private ConstraintLayout playlistPreview;
    private ImageView back, more, cover;
    private TextView titre;
    private ImageButton play;
    private RecyclerView playlistSongs;

    private LinearLayout linearLayoutMore;
    private TextView textViewTitreMore;
    private RecyclerView recyclerViewMore;

    private Button moreAlbums, moreArtists;
    private ImageView backMore;

    private List<Playlist> artistsPlaylists;
    private List<Playlist> albumsPlaylists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);


        mainActivity = (MainListener) getActivity();
        explorerFragment = this;


        explorer = rootView.findViewById(R.id.explorer_view);
        playlistPreview = rootView.findViewById(R.id.playlistPreview);
        back = rootView.findViewById(R.id.imageViewBack);
        more = rootView.findViewById(R.id.imageViewPlaylistMore);
        cover = rootView.findViewById(R.id.imageViewPlaylistCover);
        titre = rootView.findViewById(R.id.textViewPlaylistTitre);
        play = rootView.findViewById(R.id.imageButtonPlaylistPlay);
        playlistSongs = rootView.findViewById(R.id.recyclerViewPlaylistSongs);

        explorer.setVisibility(View.VISIBLE);
        playlistPreview.setVisibility(View.INVISIBLE);
        playlistSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        owl = Owl.getInstance();
        owl.setExplorerFragment(this);

        artistsPlaylists = owl.loadArtistPlaylists();
        albumsPlaylists = owl.loadAlbumPlaylists();

        auteurs = new ArrayList<>(artistsPlaylists.subList(0, Math.min(artistsPlaylists.size(), 20)));
        albums = new ArrayList<>(albumsPlaylists.subList(0, Math.min(albumsPlaylists.size(), 20)));

        Collections.sort(artistsPlaylists, (a, b) -> a.getNom().compareToIgnoreCase(b.getNom()));
        Collections.sort(albumsPlaylists, (a, b) -> a.getNom().compareToIgnoreCase(b.getNom()));



        linearLayoutMore = rootView.findViewById(R.id.linearLayoutMore);
        textViewTitreMore = rootView.findViewById(R.id.textViewTitleMore);
        recyclerViewMore = rootView.findViewById(R.id.recyclerViewMore);
        moreAlbums = rootView.findViewById(R.id.buttonPlusAlbums);
        moreArtists = rootView.findViewById(R.id.buttonPlusAuteurs);
        backMore = rootView.findViewById(R.id.imageViewBackMore);

        linearLayoutMore.setVisibility(View.INVISIBLE);

        moreAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore(getContext().getString(R.string.albums), albumsPlaylists);
            }
        });
        moreArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore(getContext().getString(R.string.artistes), artistsPlaylists);
            }
        });

        backMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        RecyclerView recyclerViewAuteurs = rootView.findViewById(R.id.recyclerViewAuteurs);
        RecyclerView recyclerViewAlbums = rootView.findViewById(R.id.recyclerViewAlbums);

        // Adapter pour les Auteurs
        PlaylistAdapter auteursAdapter = new PlaylistAdapter(requireContext(), auteurs);
        auteursAdapter.setExplorerFragment(this);
        // Adapter pour les Albums
        PlaylistAdapter albumsAdapter = new PlaylistAdapter(requireContext(), albums);
        albumsAdapter.setExplorerFragment(this);

        recyclerViewAuteurs.setAdapter(auteursAdapter);
        recyclerViewAlbums.setAdapter(albumsAdapter);

        GridLayoutManager layoutManagerAuteurs = new GridLayoutManager(rootView.getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        GridLayoutManager layoutManagerAlbums = new GridLayoutManager(rootView.getContext(), 2, GridLayoutManager.HORIZONTAL, false);

        recyclerViewAuteurs.setLayoutManager(layoutManagerAuteurs);
        recyclerViewAlbums.setLayoutManager(layoutManagerAlbums);

        if(owl.getPlaylistTemp() != null){
            setPlaylistPreview(owl.getPlaylistTemp());
            owl.setPlaylistTemp(null);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(owl.getPlaylistTemp() != null){
            setPlaylistPreview(owl.getPlaylistTemp());
            owl.setPlaylistTemp(null);
        }
    }

    public void setPlaylistPreview(Playlist playlist){

        explorer.setVisibility(View.INVISIBLE);
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
                morePlaylist.setFrom("explorer");
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
        totalHeight += measuredHeight * playlist.getSongs().size();

        totalHeight += measuredHeight;
        ViewGroup.LayoutParams params = playlistSongs.getLayoutParams();
        params.height = totalHeight + ((listadp.getItemCount() - 1));
        playlistSongs.setLayoutParams(params);
        playlistSongs.requestLayout();
        playlistSongs.setAdapter(adapter);
    }

    public void back(){
        if(linearLayoutMore.getVisibility() == View.VISIBLE && playlistPreview.getVisibility() == View.VISIBLE){
            playlistPreview.setVisibility(View.INVISIBLE);
        }else{
            explorer.setVisibility(View.VISIBLE);
            playlistPreview.setVisibility(View.INVISIBLE);
            linearLayoutMore.setVisibility(View.INVISIBLE);
        }

    }

    private void showMore(String Titre, List<Playlist> playlists){
        linearLayoutMore.setVisibility(View.VISIBLE);
        explorer.setVisibility(View.INVISIBLE);

        textViewTitreMore.setText(Titre);
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(getContext(), playlists);
        playlistAdapter.setExplorerFragment(this);

        // Configurer le RecyclerView avec un GridLayoutManager
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewMore.setLayoutManager(layoutManager);

        // Appliquer l'adaptateur au RecyclerView
        recyclerViewMore.setAdapter(playlistAdapter);

    }



}
