package com.crisgon.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Created by @cristhian-jg on 30/12/2019.
 *
 * Servicio de la aplicación que trabaja con otra clase MediaPlayer
 * la cual gestiona la música.
 */
public class MusicaService extends Service  implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    /**
     * Binder que nos será de utilidad para
     * comunicar la clase Main con el servicio.
     */
    private final IBinder binder = new MusicaBinder();

    /**
     * MediaPlayer que estará trabajando con
     * la música de la aplicación
     */
    private MediaPlayer mediaPlayer;

    /**
     * Array que contiene las músicas
     * que se van a estar ejecutando en la aplicación.
     */
    private Musica[] musicas = new Musica[3];

    /**
     * Como su nombre indica, es un puntero que
     * me ayudará a situarme atraves de las canciones.
     */
    private int puntero = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Le asigno un valor al MediaPlayer para que no esté
         * a null y poder empezar a trabajar con él.
         * Siento que esta parte no es muy correcta, pero no se me ha ocurrido otra forma,
         * al hacer = new MediaPlayer() me encontraba con errores.
         */
        mediaPlayer = MediaPlayer.create(this, R.raw.cancion1);

        /**
         * Le aplico a MediaPlayer el WakeMode Partial_Wake_Lock, con lo
         * que se controlará mejor la energía, además se ha añadido al archivo
         * Manifiesto los correspondientes permisos.
         */
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        /**
         * Le aplico el volumen a la música tanto por el lado izquierdo
         * como por el lado derecho.
         */
        mediaPlayer.setVolume(100, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /**
         * Mediante un intent en la clase main he pasado el arreglo de músicas, ya que
         * pensé que sería mejor que las músicas se crearan en Main, y así
         * poder trabajar mejor con el RecyclerView.
         */
        musicas = (Musica[]) intent.getSerializableExtra("miLista");

        /**
         * He aplicado los listener para que el servicio sepa cuando hay un error, la música
         * está preparada y se ha completado.
         */
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        return START_STICKY;
    }

    /**
     * Metodo que sirve para reproducir cierta musica
     * respecto a la pocisión pasada.
     * @param position
     */
    public void start(int position) {
        puntero = position;
        mediaPlayer = MediaPlayer.create(this, musicas[puntero].getArchivo());
        resume();
    }

    /**
     * Metodo que empieza la música o
     * la reanuda si se ha detenido. (Se puede usar en la clase Main al haber una conexión)
     */
    public void resume() {
        mediaPlayer.start();
    }

    /**
     * Metodo que detiene la música, se puede
     * reanudar. (Se puede usar en la clase Main al haber una conexión)
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Metodo que para completamente la
     * música. (Se puede usar en la clase Main al haber una conexión)
     */
    public void stop() {
        mediaPlayer.stop();
    }

    /**
     * Metodo que pasa a la siguiente canción del Array de musicas.
     * Si se pasa estando en la ultima canción volverá al primer elemento.
     * @return
     */
    public boolean passNext() {
        puntero++;
        if (puntero > 2) puntero = 0;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(getApplication(), musicas[puntero].getArchivo());
        start(puntero);
        return true;
    }

    /**
     * Metodo que pasa a la anterior canción del Array de musicas.
     * Si se pasa estando en la primera canción volverá al ultimo elemento.
     * @return
     */
    public boolean passBack() {
        puntero--;
        if (puntero < 0) puntero = 2;
        mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(getApplication(), musicas[puntero].getArchivo());
        start(puntero);
        return true;
    }

    /**
     * Metodo el cual reanuda la canción en otro
     * punto indicado.
     * @param position
     */
    public void seekToPosition(int position) {
        if (mediaPlayer.isPlaying()) mediaPlayer.seekTo(position);
    }

    /**
     * Getter que obtiene el titulo de la cancion que está sonando actualmente.
     * @return
     */
    public String getCurrentTitle() {
        return musicas[puntero].getTitulo();
    }

    /**
     * Getter que obtiene la imagen de la canción que está sonando actualmente.
     * @return
     */
    public int getCurrentCover() {
        return musicas[puntero].getCaratula();
    }

    /**
     * Getter que obtiene la duración de la canción.
     * @return
     */
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * Getter que obtiene el punto actual de la canción.
     * @return
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getMusicPosition() {
        return puntero;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Metodo que se ejecuta cuando se ha completado la música.
     * Por causas que no he podido resolver no llega a ejecutarse, por lo que es un punto que
     * no tengo correcto, ya que si llega la canción al final no pasa nada, pero lo que se intenta
     * es pasar a la siguiente canción con el metodo passNext().
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("MusicaService", "OnCompletion");
        passNext();
    }

    /**
     * Metodo que se ejecuta cuando hay un error.
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("MusicaService", "OnError");
        return false;
    }

    /**
     * Metodo que se ejecuta al hacer un prepareAsync(), me daba algunos errores
     * que no he podido resolver. Por lo que no lo llego a utilizar.
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("MusicaService", "OnPrepared");
        mediaPlayer = MediaPlayer.create(getApplication(), musicas[puntero].getArchivo());
    }

    public class MusicaBinder extends Binder {
        MusicaService getService() {
            return MusicaService.this;
        }
    }
}
