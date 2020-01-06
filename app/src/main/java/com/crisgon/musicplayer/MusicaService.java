package com.crisgon.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.security.PublicKey;

/**
 * Created by @cristhian-jg on 30/12/2019.
 */
public class MusicaService extends Service {

    private final IBinder binder = new MusicaBinder();

    private MediaPlayer mediaPlayer;
    private Musica[] musicas = new Musica[3];
    private Integer pointer = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.cancion1);
        mediaPlayer.setVolume(100, 100);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                passNext();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        musicas = (Musica[]) intent.getSerializableExtra("miLista");
        return START_STICKY;
    }

    public boolean start(int position) {
        pointer = position;
        mediaPlayer = MediaPlayer.create(this, musicas[pointer].getRutaCancion());
        mediaPlayer.start();
        return true;
    }

    public boolean pause() {
        mediaPlayer.pause();
        return true;
    }

    public boolean stop() {
        mediaPlayer.stop();
        return true;
    }


    public boolean passNext() {
        pointer++;
        if (pointer > 2) pointer = 0;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(getApplication(), musicas[pointer].getRutaCancion());
        start(pointer);
        return true;
    }

    public boolean passBack() {
        pointer--;
        if (pointer < 0) pointer = 2;
        mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(getApplication(), musicas[pointer].getRutaCancion());
        start(pointer);
        return true;
    }

    public void seekToPosition(int position){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(position);
        }
    }

    public String getCurrentTitle() {
        return musicas[pointer].getTitulo();
    }

    public int getCurrentCaratula() {
        return musicas[pointer].getRutaCaratula();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MusicaBinder extends Binder {
        MusicaService getService() {
            return MusicaService.this;
        }
    }
}
