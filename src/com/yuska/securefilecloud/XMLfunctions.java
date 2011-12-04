package com.yuska.securefilecloud;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XMLfunctions is a utility class containing all necessary functions for obtaining and parsing XML feed from server.
 * 
 * @author Chris Yuska
 *
 */
public class XMLfunctions {

	/** Creates and returns XML document from XML-formatted string
	 * 
	 * @param xml string to be parsed as XML
	 * @return Document comprised of data from input XML string.
	 */
	public final static Document XMLfromString(String xml){
		
		Document doc = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        doc = db.parse(is); 
	        
		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}
		       
        return doc;
        
	}
	
	/** Returns element value
	  * @param elem element (it is XML tag)
	  * @return Element value otherwise empty String
	  */
	 public final static String getElementValue( Node elem ) {
	     Node kid;
	     if( elem != null){
	         if (elem.hasChildNodes()){
	             for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
	                 if( kid.getNodeType() == Node.TEXT_NODE  ){
	                     return kid.getNodeValue();
	                 }
	             }
	         }
	     }
	     return "";
	 }
	 
	 /** Retrieves XML feed (as a String) from web server.
	  * 
	  * @param user user for which to encrypt XML feed retrieved
	  * @return encrypted XML feed
	  */
	 public static String getXML(String user){
			String line = null;

			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				
				HttpPost httpPost = new HttpPost("http://chrisyuska.com/cse651/list.php?user="+user);
				
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				line = EntityUtils.toString(httpEntity);
			} catch (UnsupportedEncodingException e) {
				line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
			} catch (MalformedURLException e) {
				line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
			} catch (IOException e) {
				line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
			}

			return line;
	}
	
	 /** Returns the number of results in the XML document.
	  * 
	  * @param doc the Document to count results in
	  * @return number of results in doc 
	  */
	public static int numResults(Document doc){		
		Node results = doc.getDocumentElement();
		int res = -1;
		
		try{
			res = Integer.valueOf(results.getAttributes().getNamedItem("count").getNodeValue());
		}catch(Exception e ){
			res = -1;
		}
		
		return res;
	}

	/** Returns the value of an element (in our case, the file name).
	 * 
	 * @param item Document Element to get value from
	 * @param str the tag name within the element to obtain value for
	 * @return value of Element within the provided Element, asked for by name str
	 */
	public static String getValue(Element item, String str) {		
		NodeList n = item.getElementsByTagName(str);		
		return XMLfunctions.getElementValue(n.item(0));
	}
}
