package com.company.intellihome;

public class Entities {
    protected String Host = "192.168.0.101";

    protected enum Provincias {
        SAN_JOSE, ALAJUELA, CARTAGO, HEREDIA, GUANACASTE, PUNTARENAS, LIMON
    }

    protected Provincias INProvincia(double lat, double lon) {

        if (lat >= 9.7400 && lat <= 10.0700 && lon >= -84.2500 && lon <= -83.8600) {
            return Provincias.SAN_JOSE;
        } else if (lat >= 9.86444 && lat <= 10.01625 && lon >= -84.21163 && lon <= -83.91944) {
            return Provincias.ALAJUELA;
        } else if (lat >= 9.7400 && lat <= 9.93333 && lon >= -84.08333 && lon <= -83.91944) {
            return Provincias.CARTAGO;
        } else if (lat >= 9.86444 && lat <= 10.00236 && lon >= -84.11651 && lon <= -83.91944) {
            return Provincias.HEREDIA;
        } else if (lat >= 10.01625 && lat <= 10.63504 && lon >= -85.43772 && lon <= -84.21163) {
            return Provincias.GUANACASTE;
        } else if (lat >= 9.3674 && lat <= 9.97691 && lon >= -84.8379 && lon <= -83.69713) {
            return Provincias.PUNTARENAS;
        } else if (lat >= 9.86444 && lat <= 9.99074 && lon >= -83.91944 && lon <= -83.03596) {
            return Provincias.LIMON;
        } else {
            return null; // Retorna null si no está en ninguna provincia
        }
    }
}
