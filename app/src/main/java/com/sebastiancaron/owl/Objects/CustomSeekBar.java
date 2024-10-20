package com.sebastiancaron.owl.Objects;


import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.widget.SeekBar;

public class CustomSeekBar {

    public static void setCustomSeekBar(SeekBar seekBar, int backgroundColor, int progressColor) {
        Drawable backgroundDrawable = createBackgroundDrawable(backgroundColor);
        Drawable progressDrawable = createProgressDrawable(progressColor);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, progressDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        seekBar.setProgressDrawable(layerDrawable);

    }

    private static Drawable createBackgroundDrawable(int backgroundColor) {
        return createShapeDrawable(backgroundColor);
    }

    private static Drawable createProgressDrawable(int progressColor) {
        Drawable progressDrawable = createShapeDrawable(progressColor);
        ClipDrawable clipDrawable = new ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL);

        return clipDrawable;
    }

    private static Drawable createShapeDrawable(int color) {
        android.graphics.drawable.GradientDrawable shapeDrawable = new android.graphics.drawable.GradientDrawable();
        shapeDrawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shapeDrawable.setCornerRadius(2000);
        shapeDrawable.setColor(color);

        return shapeDrawable;
    }
}

