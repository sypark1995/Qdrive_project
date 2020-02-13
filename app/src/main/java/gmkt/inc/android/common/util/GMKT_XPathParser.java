package gmkt.inc.android.common.util;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * XML Parsing Class
 * - XPath사용을 위한 Util Class
 *
 * @author wontae
 * @version 1.0.0
 * @date 2011. 07. 27
 */

public class GMKT_XPathParser {

    // 1. XML Document 변환
    // Param : InputStream, Return : Document

    /**
     * InputStream의 내용을 XML Document로 변환
     *
     * @param inputStream 변환할 InputStream 객체
     * @throws Exception 변환시 Exception 발생 가능
     * @return XML Document 객체
     */
    public static Document transformXMLDocument(InputStream inputStream) throws Exception {
        long start = System.currentTimeMillis();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document document = builder.parse(inputStream);

        long end = System.currentTimeMillis() - start;
        Log.i("GMKT", "실행 시간: " + end);

        return document;
    }

    public static Document transformXMLDocumentWithXMLFilePath(String strFilePath) throws Exception {
        long start = System.currentTimeMillis();
        FileInputStream in = new FileInputStream(strFilePath);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document document = builder.parse(in);

        long end = System.currentTimeMillis() - start;
        Log.i("GMKT", "실행 시간: " + end);

        return document;
    }

    // Param : String, Return : Document

    /**
     * XML String를 XML Document로 변환
     *
     * @param strXML XML형식의 문자열
     * @throws Exception 변환시 Exception 발생 가능
     * @return XML Document 객체
     */
    public static Document transformXMLDocument(String strXML) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Document document = builder.parse(new ByteArrayInputStream(strXML.getBytes()));

        return document;
    }

    // Param : Node, Return : String

    /**
     * XML Node(Document포함)를 String으로 변환
     *
     * @param node String으로 변환할 XML Node
     * @throws Exception 변환시 Exception 발생 가능
     * @return 변환된 String
     */
    public static String XML2String(Node node) throws Exception {
        // TODO : 테스트 해봐야함
        Source source = new DOMSource(node);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(source, result);

        return stringWriter.getBuffer().toString();
    }


    // 2. XML Document에서 XPath 쿼리를 이용해서 값 가져오기
    // Param : XPath 쿼리 String, Return : NodeList

    /**
     * XPath 쿼리를 이용하여 Node의 값 가져오기 (NodeList)
     *
     * @param strXPathQuery XPath 쿼리
     * @param node          검색할 XML Node
     * @throws Exception
     * @return 검색된 NodeList
     */
    public static NodeList searchNodeList(String strXPathQuery, Node node) throws Exception {
        long start = System.currentTimeMillis();

        XPath xpath = XPathFactory.newInstance().newXPath();
        Object result = xpath.evaluate(strXPathQuery, node, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        long end = System.currentTimeMillis() - start;
        Log.i("GMKT", "실행 시간: " + end);

        return nodes;
    }


    // Param : XPath 쿼리 String, Return : String

    /**
     * XPath 쿼리를 이용하여 Node의 값 가져오기 (NodeList)
     *
     * @param strXPathQuery XPath 쿼리
     * @param node          검색할 XML Node
     * @throws Exception 검색시 Exception 발생 가능
     * @return 검색된 NodeList
     */
    public static String searchStringValue(String strXPathQuery, Node node) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        strXPathQuery += "/text()";
        Object result = xpath.evaluate(strXPathQuery, node, XPathConstants.STRING);

        return ((String) result).trim();
    }


    /**
     * 서버에서 전달받은 XML Parsing
     *
     * @param targetDoc    전달받은 XML Document
     * @param rootNodeName Root Node 이름
     * @throws Exception
     * @return Parsing 결과 Data [ ArrayList<HashMap<String, String>> ]
     */
    public static ArrayList<HashMap<String, String>> parse(Document targetDoc, String rootNodeName) throws Exception {
        ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();

        // XML Parsing
        NodeList nodes = GMKT_XPathParser.searchNodeList("//" + rootNodeName, targetDoc);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);
            HashMap<String, String> resultHashMap = new HashMap<String, String>();

            NodeList childNodeList = currentNode.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeType() != Node.TEXT_NODE) {
                    String strNodeName = childNodeList.item(j).getNodeName();
                    String strNodeValue = "";
                    if (childNodeList.item(j).getFirstChild() != null)
                        strNodeValue = childNodeList.item(j).getFirstChild().getNodeValue();

                    resultHashMap.put(strNodeName, strNodeValue);

                    Log.i("GMKT", "Node Name: " + strNodeName + " Value: " + strNodeValue);
                }
            }
            arrList.add(resultHashMap);
        }

        return arrList;
    }

    /**
     * 서버에서 전달받은 XML Parsing
     *
     * @param targetDoc    전달받은 XML Document
     * @param rootNodeName Root Node 이름
     * @throws Exception
     * @return Parsing 결과 Data [ ArrayList<HashMap<String, String>> ]
     */
    public static ArrayList<HashMap<String, String>> parseReturnArrString(Document targetDoc, String rootNodeName) throws Exception {
        ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();

        // XML Parsing
        NodeList nodes = GMKT_XPathParser.searchNodeList("//" + rootNodeName, targetDoc);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);

            NodeList childNodeList = currentNode.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeType() != Node.TEXT_NODE) {
                    String strNodeName = childNodeList.item(j).getNodeName();
                    String strNodeValue = "";
                    if (childNodeList.item(j).getFirstChild() != null)
                        strNodeValue = childNodeList.item(j).getFirstChild().getNodeValue();

                    HashMap<String, String> resultHashMap = new HashMap<String, String>();
                    resultHashMap.put(strNodeName, strNodeValue);
                    arrList.add(resultHashMap);

                    Log.i("GMKT", "Node Name: " + strNodeName + " Value: " + strNodeValue);
                }
            }
        }

        return arrList;
    }


    /**
     * 서버에서 전달받은 XML Parsing
     *
     * @param targetDoc 전달받은 XML Document
     * @param xpathExpr XPath Expression
     * @throws Exception
     * @return Parsing 결과 Data [ ArrayList<HashMap<String, String>> ]
     */
    public static ArrayList<HashMap<String, String>> parseByXPathExpr(Document targetDoc, String xpathExpr) throws Exception {
        ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();

        // XML Parsing
        NodeList nodes = GMKT_XPathParser.searchNodeList(xpathExpr, targetDoc);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);
            HashMap<String, String> resultHashMap = new HashMap<String, String>();

            NodeList childNodeList = currentNode.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeType() != Node.TEXT_NODE) {
                    String strNodeName = childNodeList.item(j).getNodeName();
                    String strNodeValue = "";
                    if (childNodeList.item(j).getFirstChild() != null)
                        strNodeValue = childNodeList.item(j).getFirstChild().getNodeValue();

                    resultHashMap.put(strNodeName, strNodeValue);
                }
            }
            arrList.add(resultHashMap);
        }

        return arrList;
    }


    /**
     * local xml file parsing
     *
     * @param xmlFilePath
     * @param xpathExpr
     * @return Parsing 결과 Data [ ArrayList<HashMap<String, String>> ]
     * @throws Exception
     */
    public static ArrayList<HashMap<String, String>> parseByXPathExpr(String xmlFilePath, String xpathExpr) throws Exception {
        FileInputStream in = new FileInputStream(xmlFilePath);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(in));

        ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();
        arrList = GMKT_XPathParser.parseByXPathExpr(document, xpathExpr);

        in.close();
        return arrList;
    }


    /**
     * local xml file parsing
     *
     * @param xmlFilePath
     * @param rootNodeName
     * @return Parsing 결과 Data [ ArrayList<HashMap<String, String>> ]
     * @throws Exception
     */
    public static ArrayList<HashMap<String, String>> parse(String xmlFilePath, String rootNodeName) throws Exception {
        FileInputStream in = new FileInputStream(xmlFilePath);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(in));

        ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();
        arrList = GMKT_XPathParser.parse(document, rootNodeName);

        in.close();
        return arrList;
    }

}
