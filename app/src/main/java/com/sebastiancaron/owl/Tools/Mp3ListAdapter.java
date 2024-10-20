package com.sebastiancaron.owl.Tools;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sebastiancaron.owl.Fragments.CustomDialogDown;
import com.sebastiancaron.owl.MainActivity;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;


import java.util.Arrays;
import java.util.List;

public class Mp3ListAdapter extends BaseAdapter {

    private final List<Mp3file> mp3FilesList;
    private final Context context;
    private final Owl owl;

    public Mp3ListAdapter(Context context, List<Mp3file> mp3FilesList) {
        this.context = context;
        this.mp3FilesList = mp3FilesList;
        owl = Owl.getInstance();
    }


    @Override
    public int getCount() {
        return mp3FilesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3FilesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.media_item_layout, parent, false);
            holder = new ViewHolder();

            holder.coverImage = convertView.findViewById(R.id.coverImage);
            holder.artistText = convertView.findViewById(R.id.artistText);
            holder.optionsButton = convertView.findViewById(R.id.optionsButton);
            holder.titleText = convertView.findViewById(R.id.titleText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        Mp3file Mp3file = mp3FilesList.get(position);
        Bitmap cover = Mp3file.getCoverImageBitmap(context);
        if (cover != null) {
            Glide.with(context).load(cover)
                    //.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.coverImage);
        } else {
            Drawable drawable = context.getDrawable(R.drawable.ic_note);
            if(owl.isNight(context)){
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
//            Glide.with(context).load(drawable)
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                    .into(holder.coverImage);
            holder.coverImage.setImageDrawable(drawable);
        }


        holder.titleText.setText(Mp3file.getTitle());
        float textWidth = holder.titleText.getPaint().measureText(holder.titleText.getText().toString());
        if(textWidth > 800){
            Owl.getInstance().setAnimationText(holder.titleText);
            holder.titleText.setWidth((int) (textWidth + 400));
            //holder.titleText.setMaxLines(1);
        }

        holder.artistText.setText(Mp3file.getArtist() + " - " + Mp3file.getFormattedDuration());
        textWidth = holder.artistText.getPaint().measureText(holder.artistText.getText().toString());
        if(textWidth > 800){
            Owl.getInstance().setAnimationText(holder.artistText);
            holder.artistText.setWidth((int) (textWidth + 400));
        }


        holder.optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialogDown customDialog = new CustomDialogDown(context, R.style.CustomDialogTheme, Mp3file);
                customDialog.setCanceledOnTouchOutside(true);
                customDialog.show();
            }
        });


        return convertView;
    }

    private static class ViewHolder {
        ImageView coverImage;
        TextView titleText;
        TextView artistText;
        TextView durationText;

        ImageView optionsButton;
    }

}
