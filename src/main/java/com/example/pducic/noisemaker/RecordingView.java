package com.example.pducic.noisemaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by pducic on 07.10.14.
 */
public class RecordingView extends View {
    private Drawable mDrawable;
    private String name;
    private long songLength;

    public RecordingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);
        mDrawable = new ShapeDrawable(new OvalShape());
        int x = 10;
        int y = 10;
        int width = 300;
        int height = 20;
        mDrawable.setBounds(x, y, x + width, y + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    public void setContent(String name, long songLength, List<Sound> sounds) {
        this.songLength = songLength;
        this.name = name;
        Drawable[] drawables = new Drawable[sounds.size()];
        for (int i = 0; i < sounds.size(); i++) {
            ShapeDrawable soundDrawable = new ShapeDrawable(new OvalShape());
            soundDrawable.getPaint().setColor(0xff74AC23);
            int x = (int)(1.0 * getWidth() * sounds.get(i).getTime() / songLength);
            int y = 10;
            soundDrawable.setBounds(x, y, x + 20, y + 20);
            drawables[i] = soundDrawable;
        }
        mDrawable = new LayerDrawable(drawables);
    }
}
