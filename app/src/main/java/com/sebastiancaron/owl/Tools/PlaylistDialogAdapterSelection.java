package com.sebastiancaron.owl.Tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.R;

import java.util.List;


public class PlaylistDialogAdapterSelection extends BaseAdapter {

    private final Context context;
    private final List<Playlist> playlists;
    private final List<Mp3file> selection;

    public PlaylistDialogAdapterSelection(Context context, List<Playlist> playlists, List<Mp3file> selection){
        this.context = context;
        this.playlists = playlists;
        this.selection = selection;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Playlist getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.playlist_item_layout, parent, false);
        }
        ImageView cover = convertView.findViewById(R.id.coverImage);
        TextView titre = convertView.findViewById(R.id.titleText);
        TextView nText = convertView.findViewById(R.id.nText);

        nText.setText(getItem(position).getSongs().size() + " Titres");
        if(playlists.get(position).getCoverImageBitmap(context) != null){
            cover.setImageBitmap(playlists.get(position).getCoverImageBitmap(context));
        }

        titre.setText(playlists.get(position).getNom());

        return convertView;
    }
}
