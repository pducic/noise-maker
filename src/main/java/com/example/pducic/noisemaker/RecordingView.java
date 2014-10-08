package com.example.pducic.noisemaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pducic on 07.10.14.
 *
 * TODO proučiti zašto se onDraw poziva samo za textView. ne radi dispatchDraw niti etWillNotDrawEnabled(false)
 */
public class RecordingView extends TextView {
    public static final int SOUND_SHAPE_RADIUS = 20;
    private Drawable mDrawable;
    private String name;
    private long songLength;
    private List<PlayingSound> playingSounds;
    private SoundsConfiguration soundsConfiguration;

    public RecordingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = new Drawable[playingSounds.size()];
        for (int i = 0; i < playingSounds.size(); i++) {
            ShapeDrawable soundDrawable = new ShapeDrawable(new OvalShape());

            soundDrawable.getPaint().setColor(getResources().getColor(soundsConfiguration.getSound(playingSounds.get(i).getSoundId()).getSoundPreview().getColor()));
            int x = (int)(1.0 * getWidth() * playingSounds.get(i).getTime() / songLength);
            int y = 0;
            soundDrawable.setBounds(x - SOUND_SHAPE_RADIUS, y, x, y + SOUND_SHAPE_RADIUS);
            Log.d("Sound", "x:" + x + ",y:" + y);
            drawables[i] = soundDrawable;
        }
        mDrawable = new LayerDrawable(drawables);
        mDrawable.draw(canvas);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public void setContent(String name, long songLength, List<PlayingSound> playingSounds, SoundsConfiguration soundsConfiguration) {
        this.songLength = songLength;
        this.name = name;
        this.playingSounds = playingSounds;
        this.soundsConfiguration = soundsConfiguration;
    }
}
