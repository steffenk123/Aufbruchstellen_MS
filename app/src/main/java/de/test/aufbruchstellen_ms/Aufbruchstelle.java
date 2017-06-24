package de.test.aufbruchstellen_ms;

import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

/**
 * Created by Steffen and Sven on 09.06.2017.
 * <p>
 * Represent an object of an Aufbruchstelle, which contains the appropriate attributes
 */

public class Aufbruchstelle {

    // Attributes
    private int id;
    private ArrayList<PolygonOptions> geometrie;
    private String traeger;
    private String beginn;
    private String spuren;
    private String strassen;

    // Constructor
    public Aufbruchstelle() {
        this.geometrie = new ArrayList<>();
    }

    // Getter- / Setter-Methods
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

    public void addGeometrie(PolygonOptions geometrie) {
        this.geometrie.add(geometrie);
    }
}
