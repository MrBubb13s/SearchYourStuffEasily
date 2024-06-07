package com.example.searchyourstuffeasily;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class OpenApI extends AsyncTask<Void, Void, String> {
    private String url;
    public OpenApI(String url){
        this.url = url;
    }

    @Override
    protected String doInBackground(Void... voids) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try{
            dBuilder = dbFactory.newDocumentBuilder();
        }catch (ParserConfigurationException e){
            e.printStackTrace();
        }
        Document doc = null;

        try{
            doc = dBuilder.parse(url);
        }catch (IOException | SAXException e){
            e.printStackTrace();
        }
        dbFactory.setIgnoringElementContentWhitespace(true);
        //root tag
        doc.getDocumentElement().normalize();

        //파싱할 tag
        NodeList nList = doc.getElementsByTagName("row id=\"0\"");
        for (int temp = 0; temp<nList.getLength(); temp++){
            Node nNode = nList.item(temp);
            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                Element eElement = (Element) nNode;

                Log.d("오픈",getTagValue("PRDT_NM", eElement));
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
    private String getTagValue(String tag, Element eElement){
        NodeList nList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node)nList.item(0);

        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }
}
