package com.crisgon.musicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by @cristhian-jg on 05/01/2020.
 *
 * Adaptador de la lista de música que
 * hay disponible en la aplicación, en la cual se puede
 * seleccionar un elemento para cambiar de música.
 */
public class MusicaAdapter extends RecyclerView.Adapter<MusicaAdapter.MusicaViewHolder> {

    private Musica[] musicas;
    private IMusicaListener listener;
    private int position = 0;

    public MusicaAdapter(Musica[] musicas, IMusicaListener listener) {
        this.musicas = musicas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.itemlist_musica, parent, false);
        return new MusicaViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicaViewHolder holder, int position) {
        Musica musica = musicas[position];
        holder.bindMusica(musica);
    }

    @Override
    public int getItemCount() {
        return musicas.length;
    }


    public class MusicaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvTitle;
        private IMusicaListener listener;

        public MusicaViewHolder(@NonNull View itemView, IMusicaListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTituloMusica);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        public void bindMusica(Musica musica) {
            tvTitle.setText(musica.getTitulo());
        }

        @Override
        public void onClick(View v) {
            listener.onSelectedMusica(getAdapterPosition());
        }
    }
}
