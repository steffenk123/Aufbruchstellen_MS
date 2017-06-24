package de.test.aufbruchstellen_ms;

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

/**
 * Created by Steffen and Sven on 18.06.2017.
 *
 * Contains methods to get an ArrayList of type Aufbruchstelle based on a xml-String
 */

public class AufbruchstellenController {

    /**
     * This method converts a String to a Document
     *
     * @param xml as String
     * @return a Document
     * @throws Exception
     */
    public static Document parseStringtoDocument(String xml) throws Exception {
        InputSource source = new InputSource(new StringReader(xml));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(source);

        return document;
    }

    /**
     * Method creates objects of type Aufbruchstelle based on the tags of the document and collects them in an ArrayList
     *
     * @param document
     * @return an ArrayList of type Aufbruchstelle
     */
    public static ArrayList<Aufbruchstelle> getAufbruchstellenList(Document document) {
        ArrayList<Aufbruchstelle> aufbruchstellenList = new ArrayList<>();
        String featm = "gml:featureMember";
        NodeList featmList = document.getElementsByTagName(featm);

        for (int i = 0; i < featmList.getLength(); i++) {
            Aufbruchstelle aufbruchstelle = new Aufbruchstelle();
            Node featmNode = featmList.item(i);
            Element featmElement = (Element) featmNode;

            String posl = "gml:posList";
            NodeList poslList = featmElement.getElementsByTagName(posl);

            for (int j = 0; j < poslList.getLength(); j++) {
                Node poslNode = poslList.item(j);
                Element poslElement = (Element) poslNode;
                String pos = poslElement.getTextContent();

                PolygonOptions po = createPolygonOptions(createLatLngArray(pos));
                aufbruchstelle.addGeometrie(po);
            }

            // ID
            String id = getValue("ms:id", 0, featmElement);
            aufbruchstelle.setId(Integer.parseInt(id));

            // Traeger
            String traeger = getValue("ms:vtraeger", 0, featmElement);
            aufbruchstelle.setTraeger(traeger);

            // Beginn
            String beginn = getValue("ms:beginn", 0, featmElement);
            aufbruchstelle.setBeginn(beginn);

            // Spuren
            String spuren = getValue("ms:spuren", 0, featmElement);
            aufbruchstelle.setSpuren(spuren);

            // Strassen
            String strassen = getValue("ms:strassen", 0, featmElement);
            aufbruchstelle.setStrassen(strassen);

            aufbruchstellenList.add(aufbruchstelle);
        }

        return aufbruchstellenList;
    }

    /**
     * Method converts the coordinates of a polygon from String to LatLng[]
     *
     * @param coordinates as String
     * @return coordinates as LatLng[]
     */
    private static LatLng[] createLatLngArray(String coordinates) {
        String[] coordString = coordinates.split(" ");
        LatLng[] point = new LatLng[coordString.length / 2];

        int count = 0;
        for (int i = 1; i < coordString.length; i += 2) {
            point[count] = new LatLng(Double.parseDouble(coordString[i - 1]), Double.parseDouble(coordString[i]));
            count++;
        }
        return point;
    }

    /**
     * Method creates polygons based on LatLng[]
     *
     * @param latlng as LatLng[]
     * @return Polygons as PolygonOptions
     */
    private static PolygonOptions createPolygonOptions(LatLng[] latlng) {
        PolygonOptions po = new PolygonOptions();
        po.add(latlng);

        return new PolygonOptions().add(latlng);
    }

    /**
     * Method returns the values of specified tags of an element
     *
     * @param tag          as String
     * @param i            as Integer
     * @param featmElement as Element
     * @return
     */
    private static String getValue(String tag, int i, Element featmElement) {
        NodeList nodeList = featmElement.getElementsByTagName(tag);
        Node node = nodeList.item(i);
        Element element = (Element) node;

        return element.getTextContent();
    }

}
