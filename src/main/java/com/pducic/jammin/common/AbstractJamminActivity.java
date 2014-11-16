package com.pducic.jammin.common;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import com.pducic.jammin.common.config.MainConfiguration;
import com.pducic.jammin.common.model.Direction;
import com.pducic.jammin.common.model.PlayingSound;
import com.pducic.jammin.common.model.SoundGesture;
import com.pducic.jammin.common.model.SoundsConfiguration;

/**
 * Created by pducic on 02.11.14.
 */
public abstract class AbstractJamminActivity extends Activity implements SensorEventListener {

    protected static final int sensorType = Sensor.TYPE_GYROSCOPE;
    protected int pauseThreshold = MainConfiguration.IGNORE_EVENTS_AFTER_SOUND;
    protected float sensorThreshold = MainConfiguration.POSITIVE_COUNTER_THRESHOLD;
    protected SoundsConfiguration soundsConfiguration;
    protected SoundPool soundPool;
    protected Button leftConfigButton;
    protected Button rightConfigButton;
    protected long lastShake = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        soundPool = new SoundPool(MainConfiguration.MAX_STREAMS, AudioManager.STREAM_MUSIC, MainConfiguration.SRC_QUALITY);
        soundsConfiguration = MainConfiguration.getDefaultSoundConfiguration();
        soundsConfiguration.init(this, soundPool);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = sensorMgr.getDefaultSensor(sensorType);
        sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.i("Delay", String.valueOf(sensor.getMinDelay()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SensorManager sensorMgr = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensorType) {
            Log.v("XYZ", Float.toString(event.values[0]) + "   " + Float.toString(event.values[2]) + "   ");

            long curTime = System.currentTimeMillis();
            if (lastShake != 0 && (curTime - lastShake) < pauseThreshold) return;

            float x = event.values[0];
            float z = event.values[2];

            if(Math.abs(x) < sensorThreshold && Math.abs(z) < sensorThreshold){
                return;
            }

            PlayingSound playingSound = resolveSound(x, z);
            if(playingSound == null){
                return;
            }

            Log.d("PLAYING", Float.toString(x) + "   " + Float.toString(z) + "   ");

            onPlayingSound(playingSound);
            playSound(new PlayingSound[]{playingSound}, curTime);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    protected abstract void onPlayingSound(PlayingSound playingSound);

    protected PlayingSound resolveSound(float x, float z){
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? mapSound(Direction.UP, x) : mapSound(Direction.DOWN, x);
        } else {
            return z > 0 ? mapSound(Direction.LEFT, z) : mapSound(Direction.RIGHT, z);
        }
    }

    private PlayingSound mapSound(Direction direction, float amplitude) {
        SoundGesture.ConfigurationButtonId buttonId;
        if(leftConfigButton.isPressed()){
            buttonId = SoundGesture.ConfigurationButtonId.LEFT;
        }
        else if(rightConfigButton.isPressed()){
            buttonId = SoundGesture.ConfigurationButtonId.RIGHT;
        }
        else{
            return null;
        }
        String soundId = soundsConfiguration.getSoundId(direction, buttonId);
        if(soundId == null){
            return null;
        }
        return new PlayingSound(soundId, amplitude);
    }

    protected void playSound(PlayingSound[] playingSound, Long currentTime) {
        for (PlayingSound sound : playingSound) {
            Log.i("Playing", sound.toString());
            Integer soundPoolId = soundsConfiguration.getSoundPoolId(sound.getSoundId());
            soundPool.play(soundPoolId, 1, 1, 1, 0, 1);
        }

        if (currentTime != null)
            lastShake = currentTime;
    }
}
