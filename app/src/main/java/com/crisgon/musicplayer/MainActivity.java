package com.crisgon.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.crisgon.musicplayer.utils.ImageHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements IMusicaListener {

    private boolean PREFERENCE_NIGHT_MODE = true;
    private boolean PREFERENCE_SEQUENCE_MODE = false;
    private boolean PREFERENCE_REPEAT_MODE = false;

    private MusicaService mService;
    private boolean enlazado = false;
    private Handler handler = new Handler();

    private Toolbar toolbar;

    private ConstraintLayout constraintLayout;


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

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        constraintLayout = findViewById(R.id.constraintLayout);


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
                if (mService != null) {
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
             * hacer los respectivos cambios como la posición de la música, la 'anchura'
             * de la seekbar respecto a la duración de la canción y la reproduce en el punto deseado.
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mService.seekToPosition(progress);
                    seekBar.setProgress(progress);
                    seekBar.setMax(mService.getDuration());
                    mService.resume();
                    btnPlayPause.setImageResource(R.drawable.pausa);
                    isPlaying = true;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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


        /**
         * Se hace referencia al botón correcpondiente, se le asigna un
         * listener para que al pulsarlo hace una cosa u otra.
         */
        btnPlayPause = (ImageButton) findViewById(R.id.btnPlayStop);
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Si al presionar el botón la música se está reproduciendo
                 * pondrá la imagen de pausar y ejecutará el metodo del servicio resume() que
                 * inicia o reanuda la música.
                 */
                if (!isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.pausa);
                    isPlaying = true;

                    if (enlazado) {
                        mService.resume();
                        seekBar.setProgress(mService.getCurrentPosition());
                        seekBar.setMax(mService.getDuration());
                    }

                    /**
                     * Si al presionar el botón la música no se está reproduciendo
                     * pondrá la imagen de reproducir y ejecutará el metodo del servicio pausa();
                     */
                } else {
                    btnPlayPause.setImageResource(R.drawable.play);
                    if (enlazado) {
                        mService.pause();
                    }
                    isPlaying = false;
                }
            }
        });

        /**
         * Se hace referencia al botón correspondiente y se le asigna
         * un listener para que al pulsarlo pase a la siguiente canción mediante
         * el metodo del servicio passNext(), y cambia la información de la música.
         */
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enlazado) {
                    if (mService.passNext()) {
                        tvTitulo.setText(mService.getCurrentTitle());
                        changeCover();
                    }
                }
            }
        });

        /**
         * Se hace referencia al botón correspondiente y se le asigna
         * un listener para que al pulsarlo pase a la anterior canción mediante
         * el metodo del servicio passBack(), y cambia la información de la música.
         */
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enlazado) {
                    if (mService.passBack()) {
                        tvTitulo.setText(mService.getCurrentTitle());
                        changeCover();
                    }
                }
            }
        });
    }

    /**
     * Al ejecutarse el metodo onStart se crea el Intent para iniciar
     * el servicio con startService() y iniciaremos el enlace con bindService()
     * para poder usar los metodos del servicio.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getBaseContext(), MusicaService.class);
        /**
         * Paso el Array de musica a traves de un
         * putExtra, para luego leerlo en el servicio.
         */
        intent.putExtra("miLista", musicas);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(connection);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("mode-preference", PREFERENCE_NIGHT_MODE);
        editor.putBoolean("sequence-preference", PREFERENCE_SEQUENCE_MODE);
        editor.putBoolean("repeat-preference", PREFERENCE_REPEAT_MODE);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        PREFERENCE_NIGHT_MODE = preferences.getBoolean("sequence-preference", false);
        PREFERENCE_REPEAT_MODE = preferences.getBoolean("repeat-preference", false);
        PREFERENCE_SEQUENCE_MODE = preferences.getBoolean("mode-preference", true);

        if (PREFERENCE_NIGHT_MODE) {
            constraintLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        } else {
            constraintLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        }
    }

    /**
     * Metodo que transforma la imagen en un Bitmap con bordes redondeados.
     *
     * @param imageView
     * @return
     */
    public Bitmap redondear(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(bitmapDrawable.getBitmap(), 25);
        return Bitmap.createScaledBitmap(bitmap, 850, 850, false);
    }

    /**
     * Cambia la caratula por la correspondiente de cada canción y
     * le añade bordes redondeados.
     */
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

    /**
     * Me permite enlazar el servicio
     * con la clase actual para así poder usar sus metodos.
     */
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

    /**
     * Metodo que se ha de sobreescribir al hacer que esta clase implemente
     * IMusicaListener, lo que permite que al pulsar en un item del RecyclerView
     * haga las siguientes operaciones de reporducción.
     *
     * @param position
     */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_preferences:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
