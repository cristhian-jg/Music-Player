package com.crisgon.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crisgon.musicplayer.utils.ImageHelper;

public class MainActivity extends AppCompatActivity {

    private ImageView ivCaratula;
    private TextView tvTitulo;
    private ImageButton btnPlayPause;

    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    Intent intent = new Intent(getBaseContext(), MusicaService.class);
                    startService(intent);
                    btnPlayPause.setImageResource(R.drawable.pausa);
                } else if (isPlaying) {
                    btnPlayPause.setImageResource(R.drawable.jugar);
                }
            }
        });

    }

    public Bitmap redondear(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ivCaratula.getDrawable();
        Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(bitmapDrawable.getBitmap(), 25);
        return Bitmap.createScaledBitmap(bitmap, 850, 850, false);
    }
}
