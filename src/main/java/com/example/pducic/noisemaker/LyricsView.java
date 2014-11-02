package com.example.pducic.noisemaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by pducic on 07.10.14.
 *
 */
public class LyricsView extends TextView {

    private final int soundShapeRadiusPixels = (int) getResources().getDimension(R.dimen.lyricsTextSize);
    private final Pattern controlParamsPattern = Pattern.compile("\\{.*?\\}");
    private Drawable mDrawable;
    private String content;
    private int takt;
    private SoundsConfiguration soundsConfiguration;
    private int pixelsPerTakt;

    public LyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    public void setContent(String content, int takt, SoundsConfiguration soundsConfiguration) {
        this.content = content;
        this.takt = takt;
        this.soundsConfiguration = soundsConfiguration;
        setText(content);
        init();
    }

    private void init(){

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        Paint paint = new Paint();
        float lyricsTextHeight = getResources().getDimension(R.dimen.lyricsTextSize);
        paint.setTextSize(lyricsTextHeight);
        float lyricsCanvasHeight = lyricsTextHeight * 2;

        float maxTextPixelsWidthPerTakt = 0f;

        String[] split = content.split("\\|");
        for (String s : split) {
            String cleanedText = cleanupLyricString(s);
            float textWidth = paint.measureText(cleanedText);
            Log.d("Text len", s+": "+textWidth);
            if(textWidth > maxTextPixelsWidthPerTakt){
                maxTextPixelsWidthPerTakt = textWidth;
            }
        }
        this.pixelsPerTakt = (int) (maxTextPixelsWidthPerTakt/takt);
        Log.d("Longest text", "" + maxTextPixelsWidthPerTakt);
        int totalPixelsWidth = (int) (split.length * maxTextPixelsWidthPerTakt) + screenWidth;
        setWidth(totalPixelsWidth);

        LinkedList<Drawable> drawables = new LinkedList<Drawable>();

        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            Bitmap textBitmap = Bitmap.createBitmap((int) maxTextPixelsWidthPerTakt, (int) lyricsCanvasHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas textCanvas = new Canvas(textBitmap);
            BitmapDrawable textBitmapDrawable = new BitmapDrawable(getResources(), textBitmap);
            textBitmapDrawable.setBounds((int) (i*maxTextPixelsWidthPerTakt), 0, (int) ((i+1)*maxTextPixelsWidthPerTakt), (int) lyricsCanvasHeight);
            textCanvas.drawText(cleanupLyricString(s), 0, lyricsTextHeight, paint);
            drawables.add(textBitmapDrawable);

            Matcher matcher = controlParamsPattern.matcher(s);
            int matchesCount = 0;
            while(matcher.find()) {
                ShapeDrawable soundDrawable = new ShapeDrawable(new OvalShape());

                String soundId = matcher.group().replace("{","").replace("}", "");
                SoundPreview soundPreview = soundsConfiguration.getSoundPreview(soundId);
                if(soundPreview == null){
                    throw new IllegalStateException("Sound preview not defined " + soundId);
                }
                soundDrawable.getPaint().setColor(getResources().getColor(soundPreview.getColor()));

                int x = (int) (i*maxTextPixelsWidthPerTakt + (maxTextPixelsWidthPerTakt*matchesCount/takt));
                int y = (int) lyricsCanvasHeight;
                Rect rectum = new Rect(x, y, x + soundShapeRadiusPixels, y + soundShapeRadiusPixels);
                soundDrawable.setBounds(rectum);
                Log.d("Sound", "x:" + x + ",y:" + y);
                drawables.add(soundDrawable);

                if(soundPreview.getIconResourceId()!= null) {
                    Drawable drawable = getResources().getDrawable(soundPreview.getIconResourceId());
                    drawable.setBounds(rectum);
                    drawables.add(drawable);
                }
                matchesCount++;
            }
        }
        mDrawable = new LayerDrawable(drawables.toArray(new Drawable[drawables.size()]));
    }

    public int getPixelsPerTakt(){
        return pixelsPerTakt;
    }

    private static String cleanupLyricString(String s) {
        return s.replaceAll("\\{.*?\\}", "");
    }

}
