package de.test.aufbruchstellen_ms;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steffen and Sven on 24.06.2017.
 *
 * Contains methods to draw the polygons from Aufbruchstellen
 */

public class PolygonController {

    // Attributes
    private static ArrayList<Aufbruchstelle> aufbruchstellenList;
    private static ArrayList<Polygon> removeList = new ArrayList<>();
    private static HashMap<Polygon, String> polygonValues = new HashMap<>();


    /**
     * Method creates the polygons and adds them to the map
     *
     * @param xmlString
     * @throws Exception
     */
    public static void buildPolygons(String xmlString, GoogleMap googleMap) throws Exception {
        Document document = AufbruchstellenController.parseStringtoDocument(xmlString);
        aufbruchstellenList = AufbruchstellenController.getAufbruchstellenList(document);

        // Alle Polygone loeschen und Liste leeren
        removeAllPolygons();
        removeList.clear();

        for (int i = 0; i < aufbruchstellenList.size(); i++) {
            for (int j = 0; j < aufbruchstellenList.get(i).getGeometrie().size(); j++) {
                Polygon polygon = googleMap.addPolygon(aufbruchstellenList.get(i).getGeometrie().get(j).clickable(true));

                polygonValues.put(polygon, "ID: " + aufbruchstellenList.get(i).getId() + "\n" +
                        "Träger: " + aufbruchstellenList.get(i).getTraeger() + "\n" +
                        "Straßen: " + aufbruchstellenList.get(i).getStrassen() + "\n" +
                        "Spuren: " + aufbruchstellenList.get(i).getSpuren() + "\n" +
                        "Beginn: " + aufbruchstellenList.get(i).getBeginn() + "\n");

                removeList.add(polygon);
            }
        }
    }

    /**
     * Method deletes all polygons
     */
    public static void removeAllPolygons() {
        for (Polygon polygon : removeList) {
            polygon.remove();
        }
    }

    // Getter-Methods
    public static ArrayList<Aufbruchstelle> getAufbruchstellenList() {
        return aufbruchstellenList;
    }

    public static HashMap<Polygon, String> getPolygonValues() {
        return polygonValues;
    }

}
