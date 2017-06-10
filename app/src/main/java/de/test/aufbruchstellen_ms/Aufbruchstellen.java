package de.test.aufbruchstellen_ms;

import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

/**
 * Created by Steffen on 09.06.2017.
 */

public class Aufbruchstellen {

    // Arraylist in Konstruktor/Getter/Setter fehlt noch

    int id;
    ArrayList<PolygonOptions> geometrie;
    String traeger;
    String beginn;
    String spuren;
    String strassen;


    public Aufbruchstellen(int id, ArrayList<PolygonOptions> geometrie, String traeger, String beginn, String spuren, String strassen) {
        this.id = id;
        this.geometrie=  geometrie;
        this.traeger = traeger;
        this.beginn = beginn;
        this.spuren = spuren;
        this.strassen = strassen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTraeger() {
        return traeger;
    }

    public void setTraeger(String traeger) {
        this.traeger = traeger;
    }

    public String getBeginn() {
        return beginn;
    }

    public void setBeginn(String beginn) {
        this.beginn = beginn;
    }

    public String getSpuren() {
        return spuren;
    }

    public void setSpuren(String spuren) {
        this.spuren = spuren;
    }

    public String getStrassen() {
        return strassen;
    }

    public void setStrassen(String strassen) {
        this.strassen = strassen;
    }

    public ArrayList<PolygonOptions> getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(ArrayList<PolygonOptions> geometrie) {
        this.geometrie = geometrie;
    }
}
