package com.crisgon.musicplayer;

import java.io.Serializable;

/**
 * Created by @cristhian-jg on 05/01/2020.
 */
public class Musica implements Serializable {

    private String titulo;
    private Integer rutaCaratula;
    private Integer rutaCancion;

    public Musica(String titulo, Integer rutaCaratula, Integer ruta) {
        this.titulo = titulo;
        this.rutaCancion = ruta;
        this.rutaCaratula = rutaCaratula;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getRutaCaratula() {
        return rutaCaratula;
    }

    public Integer getRutaCancion() {
        return rutaCancion;
    }

}
