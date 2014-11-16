package com.pducic.jammin.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.example.pducic.noisemaker.R;
import com.pducic.jammin.common.model.PlayingSound;
import com.pducic.jammin.common.model.SoundPreview;
import com.pducic.jammin.common.model.SoundsConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by pducic on 07.10.14.
 *
 * TODO proučiti zašto se onDraw poziva samo za textView. ne radi dispatchDraw niti etWillNotDrawEnabled(false)
 */
public class RecordingView extends TextView {
    private final int SOUND_SHAPE_RADIUS = (int) getResources().getDimension(R.dimen.lyricsTextSize);
    private Drawable mDrawable;
    private long songLength;
    private List<PlayingSound> playingSounds;
    private SoundsConfiguration soundsConfiguration;
    private Set<String> correctlyPlayed;

    public RecordingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!isInEditMode()) {
            if (soundsConfiguration == null || playingSounds == null) {
                return;
            }
            LinkedList<Drawable> drawables = new LinkedList<Drawable>();
            for (int i = 0; i < playingSounds.size(); i++) {
                ShapeDrawable soundDrawable = new ShapeDrawable(new OvalShape());

                SoundPreview soundPreview = soundsConfiguration.getSoundPreview(playingSounds.get(i).getSoundId());
                if(correctlyPlayed!=null && correctlyPlayed.contains(playingSounds.get(i).getSoundId())){
                    soundDrawable.getPaint().setColor(getResources().getColor(android.R.color.holo_green_light));
                }
                else {
                    soundDrawable.getPaint().setColor(getResources().getColor(soundPreview.getColor()));
                }
                int x = (int) (1.0 * getWidth() * playingSounds.get(i).getTime() / songLength);
                int y = 0;
                Rect rectum = new Rect(x - SOUND_SHAPE_RADIUS, y, x, y + SOUND_SHAPE_RADIUS);
                soundDrawable.setBounds(rectum);
                Log.d("Sound", "x:" + x + ",y:" + y);
                drawables.add(soundDrawable);

                if(soundPreview.getIconResourceId()!= null) {
                    Drawable drawable = getResources().getDrawable(soundPreview.getIconResourceId());
                    drawable.setBounds(rectum);
                    drawables.add(drawable);
                }
            }
            mDrawable = new LayerDrawable(drawables.toArray(new Drawable[drawables.size()]));
            mDrawable.draw(canvas);
        }
    }

    public void setContent(long songLength, List<PlayingSound> playingSounds, Set<String> correctlyPlayed, SoundsConfiguration soundsConfiguration) {
        this.songLength = songLength;
        this.playingSounds = playingSounds;
        this.soundsConfiguration = soundsConfiguration;
        this.correctlyPlayed = correctlyPlayed;
    }
}
