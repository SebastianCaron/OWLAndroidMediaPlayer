package com.sebastiancaron.owl.Tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_NEXT = "action_next";
    private Owl owl;
    @Override
    public void onReceive(Context context, Intent intent) {
        owl = Owl.getInstance();
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case ACTION_PREVIOUS:
                    // Fonction à exécuter lorsque le bouton précédent est cliqué
                    //showToast(context, "Previous button clicked");
                    owl.previousSong();
                    owl.getNotificationService().updateNotification(context);
                    break;

                case ACTION_PLAY:
                    // Fonction à exécuter lorsque le bouton play est cliqué
                    //showToast(context, "Play button clicked");
                    if(owl.getMediaPlayer().isPlaying()){
                        owl.pauseAudio();
                        owl.getNotificationService().updateNotification(context);
                    }else{
                        owl.continueAudio();
                        owl.getNotificationService().updateNotification(context);
                    }

                    break;

                case ACTION_NEXT:
                    // Fonction à exécuter lorsque le bouton suivant est cliqué
                    //showToast(context, "Next button clicked");
                    owl.nextSong();
                    owl.getNotificationService().updateNotification(context);
                    break;
            }
        }
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


}
