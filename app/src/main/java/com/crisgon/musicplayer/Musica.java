package com.crisgon.musicplayer;

import java.io.Serializable;

/**
 * Created by @cristhian-jg on 05/01/2020.
 *
 * Modelo que contiene la información básica de
 * la música, así como su titulo, la ruta de la
 * caratula y la del archivo.
 */
public class Musica implements Serializable {

    private String titulo;
    private int caratula;
    private int archivo;

    public Musica(String titulo, int caratula, int archivo) {
        this.titulo = titulo;
        this.archivo = archivo;
        this.caratula = caratula;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCaratula() {
        return caratula;
    }

    public int getArchivo() {
        return archivo;
    }

    @Override
    public String toString() {
        return "Musica{" +
                "titulo='" + titulo + '\'' +
                ", caratula=" + caratula +
                ", archivo=" + archivo +
                '}';
    }
}
