package de.test.aufbruchstellen_ms;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Steffen on 18.06.2017.
 */

public class AufbruchstellenController2 {

    public static Document parseStringtoDocument(String xml) throws Exception {
        InputSource source = new InputSource(new StringReader(xml));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(source);

        return document;

    }

    public static ArrayList<Aufbruchstellen> getAufbruchstellenList(Document document){
        //Durchlaeuft alle featureMember
        //Aufbruchstelle anlegen

        ArrayList<Aufbruchstellen> aufbruchstellenList = new ArrayList<>();
        String featm = "gml:featureMember";
        NodeList featmList = document.getElementsByTagName(featm);

        for (int i = 0; i < featmList.getLength(); i++) {
            Aufbruchstellen aufbruchstellen = new Aufbruchstellen();
            Node featmNode = featmList.item(i);
            Element featmElement = (Element) featmNode;

            //Und dann alle Positionslisten der einzelnen featureMember
            String posl = "gml:posList";
            NodeList poslList = featmElement.getElementsByTagName(posl);



            // Muss hier vor die for-Schleife noch ne Liste aller Aufbruchstellen wo dann einzelne Aufbruchstellen hinzugefügt werden?
            for (int j = 0; j < poslList.getLength(); j++) {
                Node poslNode = poslList.item(j);
                Element poslElement = (Element) poslNode;
                String pos = poslElement.getTextContent();
                // muss jetzt als geometrie gesetzt werden bzw vorher noch Methode geschrieben werden die das ins richtige Format (PolygonOptions oder Polygon) konvertiert
                PolygonOptions po = createPolygonOptions(createLatLngArray(pos));
                aufbruchstellen.addGeometrie(po);
            }
//
            // ID
            String id = getValue("ms:id", 0, featmElement);
            aufbruchstellen.setId(Integer.parseInt(id));


            // Traeger
            String traeger = getValue("ms:vtraeger", 0, featmElement);
            aufbruchstellen.setTraeger(traeger);

            // Beginn
            String beginn = getValue("ms:beginn", 0, featmElement);
            aufbruchstellen.setBeginn(beginn);

            // Spuren
            String spuren = getValue("ms:spuren", 0, featmElement);
            aufbruchstellen.setSpuren(spuren);


            // Strassen
            String strassen = getValue("ms:strassen", 0, featmElement);
            aufbruchstellen.setStrassen(strassen);

            aufbruchstellenList.add(aufbruchstellen);
        }

        return aufbruchstellenList;
    }

    private static LatLng[] createLatLngArray(String coordinates) {
        String[] coord_string = coordinates.split(" ");

        LatLng[] point = new LatLng[coord_string.length / 2];

        int count = 0;
        for (int i = 1; i < coord_string.length; i += 2) {
            point[count] = new LatLng(Double.parseDouble(coord_string[i - 1]), Double.parseDouble(coord_string[i]));
            count++;
        }
        return point;
    }

    private static PolygonOptions createPolygonOptions(LatLng[] latlng) {
        PolygonOptions po = new PolygonOptions();
        po.add(latlng);


        return new PolygonOptions().add(latlng);
    }

    private static String getValue(String string, int i, Element featmElement) {
        NodeList nodeList = featmElement.getElementsByTagName(string);
        Node node = nodeList.item(i);
        Element element = (Element) node;
        String res = element.getTextContent();
        return res;
    }

}
