package com.suresofttech.car.provautosar.core.main;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
public class XMLParserMain {

	public static void main(String[] args) {
		Document document = null;
		String filePath = ".\\templates\\Ecud_Rte.xml";
		if(filePath == null || filePath.isEmpty()){
			return;
		}
		try {
			File file = new File(filePath);
			SAXReader reader = new SAXReader();
			document = reader.read(file);
			
			/*Element swSystemConsts = document.getRootElement().element("SW-SYSTEMS").element("SW-SYSTEM").element("SW-DATA-DICTIONARY-SPEC").element("SW-SYSTEMCONSTS");
			
			Element element = document.getRootElement().element("AR-PACKAGES").element("AR-PACKAGES");
			String StringswSystemConstrs = swSystemConsts.getStringValue();
			System.out.println(StringswSystemConstrs);
			List attributes = swSystemConsts.attributes();*/
//			Element root = document.getRootElement().element("AR-PACKAGES").element("AR-PACKAGE").element("SHORT-NAME");
			
//			String name = root.getText();
			
			String name = strGetNodeText(document,"//VALUE");

			System.out.println(name);

			document = null;
		} catch (DocumentException e) {
			document = null;
			return;
		} catch (Exception e){
			document = null;
			return;
		}
	}
	  /**
     * Document에서 Xpath로 결과값  추출
     * @param Document
     * @param Xpath
     * @return
     */
    public static String strGetNodeText(Document doc, String strXpath)
    {
        try {
            Node node = doc.selectSingleNode(strXpath);
            return node.getText();
        } catch (NullPointerException ne) {
            return "NullPointerException";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
