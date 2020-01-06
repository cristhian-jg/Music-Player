package com.crisgon.musicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by @cristhian-jg on 05/01/2020.
 */
public class MusicaAdapter extends RecyclerView.Adapter<MusicaAdapter.MusicaViewHolder> {

    private Musica[] musicas;
    private IMusicaListener listener;
    private int selectedPos = 0;
    public MusicaAdapter(Musica[] musicas, IMusicaListener listener) {
        this.musicas = musicas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.itemlist_musica, parent, false);
        MusicaViewHolder viewHolder = new MusicaViewHolder(itemView, listener);
        return viewHolder;
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


    public class MusicaViewHolder extends RecyclerView.ViewHolder implements View

            .OnClickListener{

        private TextView tvTitle;
        private IMusicaListener listener;

        public MusicaViewHolder(@NonNull View itemView, IMusicaListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTituloMusica);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        public void bindMusica(Musica musica){
            tvTitle.setText(musica.getTitulo());
        }

        @Override
        public void onClick(View v) {
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            notifyItemChanged(selectedPos);
            listener.onSelectedMusica(getAdapterPosition());
        }
    }

}
