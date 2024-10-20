package com.sebastiancaron.owl.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sebastiancaron.owl.Fragments.CustomDialogDown;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.List;

public class Mp3RecyclerViewAdapter extends RecyclerView.Adapter<Mp3RecyclerViewAdapter.ViewHolder> {

    private final List<Mp3file> mp3FilesList;
    private final Context context;
    private final Owl owl;
    private OnItemClickListener onItemClickListener;

    public Mp3RecyclerViewAdapter(Context context, List<Mp3file> mp3FilesList) {
        this.context = context;
        this.mp3FilesList = mp3FilesList;
        owl = Owl.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mp3file mp3file = mp3FilesList.get(position);

        Bitmap cover = mp3file.getCoverImageBitmap(context);
        if (cover != null) {
            Glide.with(context).load(cover).into(holder.coverImage);
        } else {
            Drawable drawable = context.getDrawable(R.drawable.ic_note);
            if (owl.isNight(context)) {
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
            holder.coverImage.setImageDrawable(drawable);
        }

        holder.titleText.setText(mp3file.getTitle());
        float textWidth = holder.titleText.getPaint().measureText(holder.titleText.getText().toString());
        if (textWidth > 800) {
            Owl.getInstance().setAnimationText(holder.titleText);
            holder.titleText.setWidth((int) (textWidth + 400));
        }

        holder.artistText.setText(mp3file.getArtist() + " - " + mp3file.getFormattedDuration());
        textWidth = holder.artistText.getPaint().measureText(holder.artistText.getText().toString());
        if (textWidth > 800) {
            Owl.getInstance().setAnimationText(holder.artistText);
            holder.artistText.setWidth((int) (textWidth + 400));
        }

        holder.optionsButton.setOnClickListener(view -> {
            CustomDialogDown customDialog = new CustomDialogDown(context, R.style.CustomDialogTheme, mp3file);
            customDialog.setCanceledOnTouchOutside(true);
            customDialog.show();
        });

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = (OnItemClickListener) listener;
    }


    @Override
    public int getItemCount() {
        return mp3FilesList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView titleText;
        TextView artistText;
        ImageView optionsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.coverImage);
            titleText = itemView.findViewById(R.id.titleText);
            artistText = itemView.findViewById(R.id.artistText);
            optionsButton = itemView.findViewById(R.id.optionsButton);
        }
    }
}


