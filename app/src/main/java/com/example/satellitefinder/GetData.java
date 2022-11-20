package com.example.satellitefinder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GetData {

    public static String[] x, y, z, latitude, longitude, time, radialLength;
    public static String apiEndpoint = "https://sscweb.gsfc.nasa.gov/WS/sscr/2/locations";

    public static String[] getFromDocument(URL url, String tag) throws IOException, ParserConfigurationException, SAXException {
        Document document = getDocumentFromUrl(url);
        NodeList nodeList = document.getElementsByTagName(tag);
        String[] data = new String[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            data[i] = nodeList.item(i).getTextContent();
        }
        return data;
    }

    public static Document getDocumentFromUrl(URL url) throws IOException, SAXException, ParserConfigurationException {
        URLConnection connection = url.openConnection();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(connection.getInputStream());
        document.getDocumentElement().normalize();
        return document;
    }

    public static void getData(String satelliteName) throws ParserConfigurationException, IOException, SAXException {
        URL url = new URL(apiEndpoint + "/" + satelliteName + "/" + currentUTCDate() + "T" + currentUTCTime() + "Z," + currentUTCDate() + "T" + currentUTCTimePlusThirtyMinutes() + "Z/geo/");
        latitude = getFromDocument(url, "Latitude");
        longitude = getFromDocument(url, "Longitude");
        radialLength = getFromDocument(url, "RadialLength");
        x = getFromDocument(url, "X");
        y = getFromDocument(url, "Y");
        z = getFromDocument(url, "Z");
        time = getFromDocument(url, "Time");
        setData();
    }

    public static String currentUTCDate() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(currentDate);
    }

    public static String currentUTCTime() {
        Date currentTime = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(currentTime);
    }

    public static String currentUTCTimePlusThirtyMinutes() {
        Date currentTimePlusTwoMinutes = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(currentTimePlusTwoMinutes);
    }

    public static void setData() {
        MainPage.latitude.setText("LATITUDE: " + latitude[0] + "°");
        MainPage.longitude.setText("LONGITUDE: " + longitude[0] + "°");
        MainPage.radial_length.setText("RADIAL LENGTH: " + radialLength[0] + " KM");
        MainPage.x.setText("X: " + x[0] + " KM");
        MainPage.y.setText("Y: " + y[0] + " KM");
        MainPage.z.setText("Z: " + z[0] + " KM");
        MainPage.data_up_to_date_on.setText("DATA UP TO DATE ON: " + time[0].replace("T", " ").replace("Z", "").substring(0, 19) + " (UTC)");
    }

}
