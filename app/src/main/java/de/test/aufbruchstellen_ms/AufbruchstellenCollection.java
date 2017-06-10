package de.test.aufbruchstellen_ms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sven Beyel on 10.06.2017.
 */

public class AufbruchstellenCollection {
    
    List<Aufbruchstellen> aufbruchstellenListe;

    public AufbruchstellenCollection() {
        aufbruchstellenListe = new ArrayList<>();
    }

    public List<Aufbruchstellen> getAufbruchstellenListe() {
        return aufbruchstellenListe;
    }

    public void addAufbruchstelle(Aufbruchstellen aufbruchstelle) {
        aufbruchstellenListe.add(aufbruchstelle);
    }
}
