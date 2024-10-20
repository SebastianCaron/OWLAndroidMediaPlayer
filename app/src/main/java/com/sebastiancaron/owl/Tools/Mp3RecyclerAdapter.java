package com.sebastiancaron.owl.Tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sebastiancaron.owl.Fragments.CustomDialogDown;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.List;

public class Mp3RecyclerAdapter extends RecyclerView.Adapter<Mp3RecyclerAdapter.ViewHolder> {

    private final List<Mp3file> mp3FilesList;

    private final Context context;

    public Mp3RecyclerAdapter(Context context, Playlist playlist) {
        this.context = context;
        this.playlist = playlist;
        this.mp3FilesList = playlist.getSongs();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item_layout, parent, false);
        return new ViewHolder(itemView, playlist);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mp3file mp3file = mp3FilesList.get(position);
        holder.itemView.setSelected(mp3file.isSelected());
        if (mp3file.getCoverImageBitmap(context) != null) {
            Glide.with(context).load(mp3file.getCoverImageBitmap(context))
                    .into(holder.coverImage);
        } else {
            Drawable drawable = context.getDrawable(R.drawable.ic_note);
            if(Owl.getInstance().isNight(context)){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
//            Glide.with(context).load(drawable)
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                    .into(holder.coverImage);
            holder.coverImage.setImageDrawable(drawable);
        }

        holder.titleText.setText(mp3file.getTitle());
        float textWidth = holder.titleText.getPaint().measureText(holder.titleText.getText().toString());
        if (textWidth > 800) {
            //holder.titleText.startAnimation(AnimationUtils.loadAnimation(context, R.anim.text));
            Owl.getInstance().setAnimationText(holder.titleText);
            holder.titleText.setWidth((int) (textWidth + 400));
        }

        holder.artistText.setText(mp3file.getArtist() + " - " + mp3file.getFormattedDuration());
        textWidth = holder.artistText.getPaint().measureText(holder.artistText.getText().toString());
        if (textWidth > 800) {

            //holder.artistText.startAnimation(AnimationUtils.loadAnimation(context, R.anim.text));
            Owl.getInstance().setAnimationText(holder.artistText);
            holder.artistText.setWidth((int) (textWidth + 400));
        }

        holder.optionsButton.setOnClickListener(view -> {
            CustomDialogDown customDialog = new CustomDialogDown(context, R.style.CustomDialogTheme, mp3file);
            customDialog.setCanceledOnTouchOutside(true);
            customDialog.show();

        });

        if(holder.itemView.isSelected()){
            if(Owl.getInstance().isNight(context)){
                holder.itemView.setBackgroundColor(context.getColor(R.color.secondary_night));
            }
            else {
                holder.itemView.setBackgroundColor(context.getColor(R.color.secondary));
            }
        }
        else{
            holder.itemView.setBackgroundColor(context.getColor(R.color.transparent));
        }

    }

    @Override
    public int getItemCount() {
        return mp3FilesList.size();
    }
    public Playlist playlist;
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView titleText;
        TextView artistText;
        ImageView optionsButton;

        public ViewHolder(@NonNull View itemView, Playlist playlist) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Owl owl = Owl.getInstance();
                    if(owl.getSelectedFilesCount() > 0){
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Mp3file item = playlist.getSongs().get(adapterPosition);
                            item.setSelected(!item.isSelected());
                            if(item.isSelected()){
                                Owl.getInstance().setSelectedFilesCount(Owl.getInstance().getSelectedFilesCount() + 1);
                            }else{
                                Owl.getInstance().setSelectedFilesCount(Owl.getInstance().getSelectedFilesCount() - 1);
                            }
                            Owl.getInstance().refreshBiblio();
                            notifyItemChanged(adapterPosition);
                        }
                    }else{
                        owl.setCurrentPlaylist(playlist.clone());
                        owl.playAudio(getAdapterPosition());
                    }


                }
            });
            if(Owl.getInstance().getBibliothequeFragment() != null && Owl.getInstance().getLecture() == null){
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(Owl.getInstance().getSelectedFilesCount() == 0){
                            int adapterPosition = getAdapterPosition();
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                Mp3file item = playlist.getSongs().get(adapterPosition);
                                item.setSelected(!item.isSelected());
                                if(item.isSelected()){
                                    Owl.getInstance().setSelectedFilesCount(Owl.getInstance().getSelectedFilesCount() + 1);
                                }else{
                                    Owl.getInstance().setSelectedFilesCount(Owl.getInstance().getSelectedFilesCount() - 1);
                                }
                                Owl.getInstance().refreshBiblio();
                                notifyItemChanged(adapterPosition);
                            }

                        }

                        return false;
                    }
                });
            }


            coverImage = itemView.findViewById(R.id.coverImage);
            titleText = itemView.findViewById(R.id.titleText);
            artistText = itemView.findViewById(R.id.artistText);
            optionsButton = itemView.findViewById(R.id.optionsButton);
        }
    }
}
