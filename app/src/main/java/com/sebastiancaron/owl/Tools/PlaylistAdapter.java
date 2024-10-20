package com.sebastiancaron.owl.Tools;

import static android.provider.MediaStore.Images.Media.getBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sebastiancaron.owl.Fragments.AccueilFragment;
import com.sebastiancaron.owl.Fragments.ExplorerFragment;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final Context context;
    private final List<Playlist> playlists;
    private ExplorerFragment explorerFragment;
    private AccueilFragment accueilFragment;

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    public void setAccueilFragment(AccueilFragment accueilFragment) {
        this.accueilFragment = accueilFragment;
    }

    public void setExplorerFragment(ExplorerFragment explorerFragment) {
        this.explorerFragment = explorerFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setExplorerFragment(this.explorerFragment);
        viewHolder.setAccueilFragment(this.accueilFragment);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        if(playlist.getPathToImage() == null){
            Drawable drawable = context.getDrawable(R.drawable.ic_note);
            if(Owl.getInstance().isNight(context)){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
            //Glide.with(context).load(drawable).into(holder.imageViewCover);
            holder.imageViewCover.setImageDrawable(drawable);
        }else{
            Bitmap cover = playlist.getCoverImageBitmap(context);
            if(cover == null){
                Drawable drawable = context.getDrawable(R.drawable.ic_note);
                if(Owl.getInstance().isNight(context)){
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                }
                //Glide.with(context).load(drawable).into(holder.imageViewCover);
                holder.imageViewCover.setImageDrawable(drawable);
            }else{
                Glide.with(context).load(cover)
                        .into(holder.imageViewCover);
                //holder.imageViewCover.setImageBitmap(playlist.getCoverImageBitmap(context));
            }

        }
        holder.textViewTitre.setText(playlist.getNom());
        holder.setPlaylistPreview(playlists, position);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCover;
        TextView textViewTitre;
        ExplorerFragment explorerFragment;
        AccueilFragment accueilFragment;
        View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageViewCover = itemView.findViewById(R.id.imageViewCover);
            textViewTitre = itemView.findViewById(R.id.textViewTitre);
        }

        public void setAccueilFragment(AccueilFragment accueilFragment) {
            this.accueilFragment = accueilFragment;
        }

        public void setExplorerFragment(ExplorerFragment explorerFragment) {
            this.explorerFragment = explorerFragment;
        }

        void setPlaylistPreview(List<Playlist> playlists, int index){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(accueilFragment != null){
                        accueilFragment.setPlaylistPreview(playlists, index);
                    }else if(explorerFragment != null){
                        explorerFragment.setPlaylistPreview(playlists.get(index));
                    }
                }
            });
        }
    }
}
