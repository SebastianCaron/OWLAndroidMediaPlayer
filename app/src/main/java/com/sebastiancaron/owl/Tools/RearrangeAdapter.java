package com.sebastiancaron.owl.Tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

import java.util.Collections;
import java.util.List;

public class RearrangeAdapter extends RecyclerView.Adapter<RearrangeAdapter.ViewHolder> {
    private final Context context;
    private final List<Mp3file> dataList;
    private final LayoutInflater inflater;

    public RearrangeAdapter(Context context, List<Mp3file> dataList) {
        this.context = context;
        this.dataList = dataList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.media_item_rearrange_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();

    }


    public void rearrangeItems(int fromPosition, int toPosition) {
        Mp3file item = dataList.remove(fromPosition);
        dataList.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView cover;
        ImageView drag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleText);
            artist = itemView.findViewById(R.id.artistText);
            cover = itemView.findViewById(R.id.coverImage);
            drag = itemView.findViewById(R.id.buttonDrag);
        }

        void bind(int position) {
            title.setText(dataList.get(position).getTitle());
            artist.setText(dataList.get(position).getArtist());
            if (dataList.get(position).getCoverImageBitmap(context) != null) {
                cover.setImageBitmap(dataList.get(position).getCoverImageBitmap(context));
            }else{
                Drawable drawable = context.getDrawable(R.drawable.ic_note);
                if(Owl.getInstance().isNight(context)){
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                }
                cover.setImageDrawable(drawable);
            }
            drag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
    }
}
