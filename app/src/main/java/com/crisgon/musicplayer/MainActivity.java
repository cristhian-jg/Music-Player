package com.crisgon.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crisgon.musicplayer.utils.ImageHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements IMusicaListener{

    private MusicaService mService;
    private boolean enlazado = false;
    private Handler handler = new Handler();

    private Musica[] musicas = new Musica[3];

    private RecyclerView recyclerView;

    private ImageView ivCaratula;
    private TextView tvTitulo;

    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private ImageButton btnBack;

    private SeekBar seekBar;

    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Se rellena el array con las tres
         * canciones que habrán en la aplicación.
         */
        updateMusica();

        /**
         * Se hace referencia al RecyclerView para así poder rellenarlo con
         * las músicas mediante el adaptador, al cual se le pasa el array
         * y el escuchador (la propia clase que implementa IMusicaListener)
         */
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new MusicaAdapter(musicas, this));
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        /**
         * Se hace referencia a la SeekBar y mediante la clase Executors
         * creo un nuevo hilo de ejecución que irá actualizando el progreso de la seekbar
         * cada 200 milisegundos gracias al manejador.
         */
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        ScheduledExecutorService executors = Executors.newSingleThreadScheduledExecutor();
        executors.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(mService != null){
                    seekBar.setProgress(mService.getCurrentPosition());
                }
                handler.postDelayed(this, 200);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        /**
         * Setteo un nuevo escuchador a la SeekBar para que al cambiar de posición la SeekBar
         * esta haga un cambio tambien en la música.
         */
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * Se ejecuta al cambiar el progreso del SeekBar, este se encarga de
             * cambiar la posición de la música mediante un metodo en el servicio .seekToPosition();
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mService.seekToPosition(progress);
                    seekBar.setProgress(progress);
                    mService.resume();
                    btnPlayPause.setImageResource(R.drawable.pausa);
                    isPlaying = true;
                }

            }

            /**
             * Se ejecuta al tocar la SeekBar y al cambia de posición respecto a
             * la posición de la música gracias al metodo del servicio .getCurrentPosition()
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //seekBar.setProgress(mService.getCurrentPosition());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //seekBar.setProgress(mService.getCurrentPosition());
            }
        });

        /**
         * Se hace referencia al ImageView de la caratula y mediante un metodo
         * la transformo en una
         */
        ivCaratula = (ImageView) findViewById(R.id.ivCaratula);
        Bitmap caratulaRedondeada = redondear(ivCaratula);
        ivCaratula.setImageBitmap(caratulaRedondeada);

        tvTitulo = (TextView) findViewById(R.id.tvTitulo);
        tvTitulo.setSelected(true);

        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayStop);
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.pausa);
                    isPlaying = true;

                    if (enlazado) {
                        mService.resume();
                        seekBar.setMax(mService.getDuration());
                        //seekBar.setProgress(mService.getCurrentPosition());
                    }

                } else {
                    btnPlayPause.setImageResource(R.drawable.play);
                    if (enlazado) {
                        mService.pause();
                        //seekBar.setProgress(mService.getCurrentPosition());
                    }
                    isPlaying = false;
                }
            }
        });

        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enlazado) {
                    if(mService.passNext()){
                        tvTitulo.setText(mService.getCurrentTitle());
                        changeCover();
                    }
                }
            }
        });

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enlazado) {
                    if(mService.passBack()) {
                        tvTitulo.setText(mService.getCurrentTitle());
                        changeCover();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getBaseContext(), MusicaService.class);
        intent.putExtra("miLista", musicas);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    /**
     * Metodo que transforma la imagen en un Bitmap con bordes redondeados.
     * @param imageView
     * @return
     */
    public Bitmap redondear(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(bitmapDrawable.getBitmap(), 25);
        return Bitmap.createScaledBitmap(bitmap, 850, 850, false);
    }

    public void changeCover() {
        ivCaratula.setImageResource(mService.getCurrentCover());
        Bitmap caratulaRedondeada = redondear(ivCaratula);
        ivCaratula.setImageBitmap(caratulaRedondeada);
    }

    /**
     * Metodo que rellena el array con las músicas.
     */
    public void updateMusica() {
        musicas[0] = new Musica(
                "Lil Nas X - Old Town Road (feat. Billy Ray Cyrus) [Remix]",
                R.drawable.caratula1,
                R.raw.cancion1
        );

        musicas[1] = new Musica(
                "Post Malone, Swae Lee - Sunflower (Spider-Man Into the Spider-Verse)",
                R.drawable.caratula2,
                R.raw.cancion2
        );

        musicas[2] = new Musica(
                "지코 (ZICO) - BERMUDA TRIANGLE (Feat. Crush, DEAN)",
                R.drawable.caratula3,
                R.raw.cancion3
        );
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MusicaService.MusicaBinder binder = (MusicaService.MusicaBinder) service;
            mService = binder.getService();
            enlazado = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            enlazado = false;
        }
    };

    @Override
    public void onSelectedMusica(int position) {

        btnPlayPause.setImageResource(R.drawable.pausa);
        mService.stop();
        mService.start(position);
        tvTitulo.setText(mService.getCurrentTitle());
        changeCover();
        seekBar.setMax(mService.getDuration());
        seekBar.setProgress(mService.getCurrentPosition());

        isPlaying = true;
    }
}
