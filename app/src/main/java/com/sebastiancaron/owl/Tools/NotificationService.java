package com.sebastiancaron.owl.Tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.sebastiancaron.owl.MainActivity;
import com.sebastiancaron.owl.MainListener;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

public class NotificationService extends Service {


    private Owl owl;
    private static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "owl_mediaplayer";
    private static NotificationService instance;

    public static NotificationService getInstance() {
        return instance;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        owl = Owl.getInstance();
        instance = this;
        owl.setNotificationService(this);
    }

    private void showPersistentNotification() {
        RemoteViews remoteViews;
        Context context = this;
        boolean night = owl.isNight(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Owl Media Player", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainListener.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        if(owl.getCurrentSong().getCoverImageBitmap(context) != null){
            remoteViews.setImageViewBitmap(R.id.imageViewCover, owl.getCurrentSong().getCoverImageBitmap(context));
            remoteViews.setInt(R.id.imageViewCover, "setColorFilter", getColor(R.color.transparent));
        }else{
            remoteViews.setImageViewResource(R.id.imageViewCover, R.drawable.ic_note);
            if(night){
                remoteViews.setInt(R.id.imageViewCover, "setColorFilter", getColor(R.color.white));
            }

        }
        remoteViews.setInt(R.id.notification_background,"setBackgroundColor", owl.getNotification_color());
        remoteViews.setTextViewText(R.id.textViewTitre, owl.getCurrentSong().getTitle());
        remoteViews.setTextViewText(R.id.textViewArtist, owl.getCurrentSong().getArtist());
        if(owl.isNight(context)){
            remoteViews.setInt(R.id.textViewTitre, "setTextColor", getColor(R.color.text_night));
            remoteViews.setInt(R.id.textViewArtist, "setTextColor", getColor(R.color.text_night));
            remoteViews.setInt(R.id.imageViewPrevious, "setColorFilter", getColor(R.color.white));
            remoteViews.setInt(R.id.imageViewNext, "setColorFilter", getColor(R.color.white));
            remoteViews.setInt(R.id.imageViewPlay, "setColorFilter", getColor(R.color.white));
        }else{
            remoteViews.setInt(R.id.textViewTitre, "setTextColor", getColor(R.color.text));
            remoteViews.setInt(R.id.textViewArtist, "setTextColor", getColor(R.color.text));
        }
        if(owl.getMediaPlayer().isPlaying()){
            remoteViews.setImageViewResource(R.id.imageViewPlay, R.drawable.ic_pause);
        }else{
            remoteViews.setImageViewResource(R.id.imageViewPlay, R.drawable.ic_play);
        }

//        remoteViews.setImageViewResource(R.id.imageViewNext, R.drawable.ic_next);
//        remoteViews.setImageViewResource(R.id.imageViewPrevious, R.drawable.ic_previous);

        // Configurez les actions des boutons en utilisant des PendingIntents
        Intent actionIntent = new Intent(context, NotificationActionReceiver.class);

        // Action bouton précédent
        actionIntent.setAction(NotificationActionReceiver.ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.imageViewPrevious, previousPendingIntent);

        // Action bouton play
        actionIntent.setAction(NotificationActionReceiver.ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.imageViewPlay, playPendingIntent);

        // Action bouton suivant
        actionIntent.setAction(NotificationActionReceiver.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.imageViewNext, nextPendingIntent);

        remoteViews.setOnClickPendingIntent(R.id.notification_background, pendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.test)
                .setBadgeIconType(R.drawable.test)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.test))
                .setContent(remoteViews)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showPersistentNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    public void updateNotification(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        boolean night = owl.isNight(context);

        if(owl.getCurrentSong().getCoverImageBitmap(context) != null){
            remoteViews.setImageViewBitmap(R.id.imageViewCover, owl.getCurrentSong().getCoverImageBitmap(context));
            remoteViews.setInt(R.id.imageViewCover, "setColorFilter", getColor(R.color.transparent));
        }else{
            remoteViews.setImageViewResource(R.id.imageViewCover, R.drawable.ic_note);
            if(night){
                remoteViews.setInt(R.id.imageViewCover, "setColorFilter", getColor(R.color.white));
            }
        }

        remoteViews.setTextViewText(R.id.textViewTitre, owl.getCurrentSong().getTitle());
        remoteViews.setTextViewText(R.id.textViewArtist, owl.getCurrentSong().getArtist());
        remoteViews.setInt(R.id.notification_background,"setBackgroundColor", owl.getNotification_color());
        if(night){
            remoteViews.setInt(R.id.textViewTitre, "setTextColor", getColor(R.color.text_night));
            remoteViews.setInt(R.id.textViewArtist, "setTextColor", getColor(R.color.text_night));
            remoteViews.setInt(R.id.imageViewPrevious, "setColorFilter", getColor(R.color.white));
            remoteViews.setInt(R.id.imageViewNext, "setColorFilter", getColor(R.color.white));
            remoteViews.setInt(R.id.imageViewPlay, "setColorFilter", getColor(R.color.white));

        }else{
            remoteViews.setInt(R.id.textViewTitre, "setTextColor", getColor(R.color.text));
            remoteViews.setInt(R.id.textViewArtist, "setTextColor", getColor(R.color.text));
        }
        if(owl.getMediaPlayer().isPlaying()){
            remoteViews.setImageViewResource(R.id.imageViewPlay, R.drawable.ic_pause);
        }else{
            remoteViews.setImageViewResource(R.id.imageViewPlay, R.drawable.ic_play);
        }

//        remoteViews.setImageViewResource(R.id.imageViewNext, R.drawable.ic_next);
//        remoteViews.setImageViewResource(R.id.imageViewPrevious, R.drawable.ic_previous);
        owl.refreshUI();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.test)
                .setBadgeIconType(R.drawable.test)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.test))
                .setContent(remoteViews)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

}
