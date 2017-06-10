package de.test.aufbruchstellen_ms;

/**
 * Created by Steffen on 09.06.2017.
 */



import android.annotation.TargetApi;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Steffen
 */
public class Aufbruchstellen_Controller {

    Aufbruchstellen aufbruchstellen;
    AufbruchstellenCollection aufbruchstellenCollection;

    // CSV einlesen
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void getGML() {
        URL url;
        try {
            url = new URL("https://www.stadt-muenster.de/ows/mapserv621/odaufgrabserv?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TYPENAME=aufgrabungen&EXCEPTIONS=XML&MAXFEATURES=5000&SRSNAME=EPSG:4326");

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);}

            String gml = response.toString();
            System.out.println(gml);

            InputSource source = new InputSource(new StringReader(gml));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);


            //Durchläuft alle featureMember
            String featm ="gml:featureMember";
            NodeList featmList = document.getElementsByTagName(featm);

            aufbruchstellenCollection = new AufbruchstellenCollection();

            for(int i=0;i<featmList.getLength();i++) {
                Node featmNode = featmList.item(i);
                Element featmElement = (Element) featmNode;


                aufbruchstellen = new Aufbruchstellen();

                //Und dann alle Positionslisten der einzelnen featureMember
                String posl = "gml:posList";
                NodeList poslList = featmElement.getElementsByTagName(posl);

                // Muss hier vor die for-Schleife noch ne Liste aller Aufbruchstellen wo dann einzelne Aufbruchstellen hinzugefügt werden?
                for (int j = 0; j < poslList.getLength(); j++) {
                    Node poslNode = poslList.item(j);
                    Element poslElement = (Element) poslNode;
                    String pos = poslElement.getTextContent();
                    // muss jetzt als geometrie gesetzt werden bzw vorher noch Methode geschrieben werden die das ins richtige Format (PolygonOptions oder Polygon) konvertiert
                    PolygonOptions po= createPolygonOptions(createLatLngArray(pos));
                    aufbruchstellen.addGeometrie(po);
                }

                // ID
                String id = getValue("ms:id",0,featmElement);
                aufbruchstellen.setId(Integer.parseInt(id));

                // Traeger
                String traeger = getValue("ms:vtraeger",0,featmElement);
                aufbruchstellen.setTraeger(traeger);

                // Beginn
                String beginn = getValue("ms:beginn",0,featmElement);
                aufbruchstellen.setBeginn(beginn);

                // Spuren
                String spuren = getValue("ms:spuren",0,featmElement);
                aufbruchstellen.setSpuren(spuren);

                // Strassen
                String strassen = getValue("ms:strassen",0,featmElement);
                aufbruchstellen.setStrassen(strassen);

                aufbruchstellenCollection.addAufbruchstelle(aufbruchstellen);

            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(Aufbruchstellen_Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Aufbruchstellen_Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Aufbruchstellen_Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Aufbruchstellen_Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public LatLng[] createLatLngArray(String coordinates){

        String[] coord_string = coordinates.split(" ");

        LatLng[] point = new LatLng[coord_string.length/2];

        int count=0;
        for(int i=1; i<coord_string.length;i+=2){
            point[count] = new LatLng(Double.parseDouble(coord_string[i-1]),Double.parseDouble(coord_string[i]));
            count++;
        }
        return point;
    }

    public PolygonOptions createPolygonOptions(LatLng[] latlng){
        return new PolygonOptions().add(latlng);
    }

    public String getValue(String string, int i, Element featmElement){

        NodeList nodeList = featmElement.getElementsByTagName(string);
        Node node = nodeList.item(i);
        Element element = (Element) node;
        String res = element.getTextContent();
        return res;
    }

 }
