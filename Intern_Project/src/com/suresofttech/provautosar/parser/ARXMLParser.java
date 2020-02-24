package com.suresofttech.provautosar.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

import com.suresofttech.provautosar.parser.ARXMLParserHandler;

public class ARXMLParser {
	public ARXMLParser(){}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			File file = new File("./templates/Ecud_Rte.arxml");
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			
			SAXParser sp = spf.newSAXParser();
			
			ARXMLParserHandler parserHandler = new ARXMLParserHandler();
			sp.parse(file, parserHandler);
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(SAXException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
