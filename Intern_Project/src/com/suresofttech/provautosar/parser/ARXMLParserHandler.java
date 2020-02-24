package com.suresofttech.provautosar.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//XML ������ ������ �νĵǾ��� �� �߻��ϴ� �̺�Ʈ�� ó��
public class ARXMLParserHandler extends DefaultHandler{
	private boolean isTaskParsingStarted = false;
	private boolean isSwcParsingStarted = false;
	private boolean isCheckedSection = false;
	private String elementName = "";
	private String globalTaskName="";
	private StringBuffer sBuffer = new StringBuffer();
	private int Taskcount=0;
	private int Swccount;
	
	private Map <String,List<String>> listOfTaskRelationMap = new HashMap<String,List<String>>();
	private List<String> listOfSwc = new ArrayList<String>();
	private List<String> listOfTask = new ArrayList<String>();
	private List<String> listOfTree = new ArrayList<String>();
	
	/*public void setDocumentLocator(Locator loc){
		System.out.println("[����] "+loc.getSystemId());
	}*/
	
	public void startDocument(){
		System.out.println("Start Document!");
		System.out.println("");
		//listOfTask.clear();
	}
	
	//XML ������ ���� �νĵǾ��� �� �߻��ϴ� �̺�Ʈ�� ó��
	public void endDocument(){
		System.out.println("End Document!");
		System.out.println("");
		
		//listOfTask�� ���� Task �̸����� ��ü������ ���Ͽ� �ߺ��Ǵ� ���� �����ϴ� ����
		for(int i=0; i<listOfTask.size();i++){
			//System.out.println(listOfTask.size());
			String DuplicatedCheck = listOfTask.get(i);
			for(int j=0; j<listOfTask.size();j++){
				if(DuplicatedCheck.equals(listOfTask.get(j))&& i!=j){
					String a = listOfTask.remove(j);
					//System.out.println("������  Task �̸� : "+a+", ������ ����Ʈ�� �ε��� ��ȣ : "+j);
						if(a.length()>0){
							j-=1;
							continue;
						}
				}
			}
		}
		
		for(String a : listOfTask){
			//System.out.println(a);
			Taskcount++;
		}
		
		for(String a : listOfSwc){
			//System.out.println(a);
			Swccount++;
		}
		System.out.println(Taskcount);
			for(String key : listOfTaskRelationMap.keySet()){
				System.out.println("key : "+key+"/ value : "+listOfTaskRelationMap.get(key));
			}
			
			
			//���� ����Ʈ�鿡 �����͸� ����� �ʱ� ���� �ʱ�ȭ 
			listOfTask.clear();
			listOfSwc.clear();
		}

	
	//Element�� ������ �ν����� �� �߻��ϴ� �̺�Ʈ�� ó��
	public void startElement(String uri,String localName,String qname,Attributes attr){
			//System.out.println("Start element, Name : "+qname);
			if(qname == "VALUE-REF")
				isTaskParsingStarted = true;
			if(qname == "SHORT-NAME")
				isSwcParsingStarted = true;
			
			//���ǿ� ���� �� listOfTree�� element �̸����� ��� �߰�(���ǿ� �� �Ŀ��� �߰����� ����)
			if(!isCheckedSection){	
			listOfTree.add(qname);
			//listOfTree�� ������ �ε��� ���� SHORT-NAME�̰� �� ���� ELEMENT�� ���ʴ�� ECUC-CONTAINER-VALUE, CONTAINERS�� ��� (���� SHORT-NAME �� Swc �̸��� ������ �ִ� SHORT-NAME ELEMENT�� ��󳻱� ����)
			
			//if(qname == "SHORT-NAME") �� ����
			if(listOfTree.get(listOfTree.size()-1) == "SHORT-NAME"){
				if(listOfTree.get(listOfTree.size()-2) == "ECUC-CONTAINER-VALUE" && listOfTree.get(listOfTree.size()-3) == "CONTAINERS"){
						isCheckedSection = true;
					}
				}
			}
		/*for(int i=0;i<attr.getLength();i++){
			System.out.println("Attributes : "+attr.getQName(i)+"="+attr.getValue(i));
			
		}*/
	}
	//Element��  ���� �ν����� �� �߻��ϴ� �̺�Ʈ�� ó��
	public void endElement(String uri,String localName,String qname){
		//System.out.println("End element, Name : "+qname);
		isTaskParsingStarted = false;
		isSwcParsingStarted = false;
		//ECUC-CONTAINER-VALUE�� ������ ������ Element�� ���  ������ ��Ÿ���� isCheckedSection = false�� ���� �� listOfTree�� SHORT-NAME, ECUC-CONTAINER-VALUE�� ����(���ο� ������ Element�� �ޱ� ����) 
		if(qname == "ECUC-CONTAINER-VALUE" && isCheckedSection){
			isCheckedSection = false;
			
			//listOfTree ����� ���� 2�� Element(ECU-CONTAINER-VALUE�� SHORT-NAME�� ������ . Why ? ���ο� Section�� �����ϱ� ����)
			int index = listOfTree.size();
			listOfTree.remove(index-1);
			listOfTree.remove(index-2);
		}
	}
	
	//�� Element�� ��(�νĵ� ������ �� ���׸�Ʈ�� ���ؼ� ȣ��)
	public void characters(char[] ch,int start,int length) throws SAXException{
		//System.out.println(isParsingStarted);
		String strValue = "";	
		String TaskName = "";
		//System.out.println("isParsingStarted = "+isParsingStarted);
		
		//Task�̸��� ���� VALUE-REF���� Task �̸���  �Ľ�
		if(isTaskParsingStarted){
			//Element�� ���� ���ϱ� ���ؼ��� Buffer�� �νĵ� �� ���ڸ� start���� length��ŭ append�Ѵ�.
			sBuffer.append(new String(ch,start,length));
			strValue = sBuffer.toString().trim();
			
		
			if(strValue != null && strValue.length()!=0 && !strValue.equals("\n")){
				if(strValue.contains("Os/OsTask")){		//Task �̸��� ������ ��
					TaskName = strValue.substring(12);
					listOfTask.add(TaskName);
					
					//������ ������ ������ Task�϶�
					if(isCheckedSection){
							//ó�� ���� ���������� Task, Swc ���� �۾� �Ҷ�
							if(listOfTaskRelationMap.isEmpty() || listOfSwc.size() == 1){		
								List<String> listOfSwcBox = new ArrayList<String>(); //�ӽ� ���� ����Ʈ
								
									//Map�� ó�� ���� �������� Task�� Swc�� ���ν�ų ��
									if(listOfTaskRelationMap.isEmpty()){
										//���ǿ� ���������� listOfTree�� ������ �ε����� �߰��س��� Swc �̸��� ������
										listOfSwcBox.add(listOfSwc.get(listOfSwc.size()-1));
										listOfTaskRelationMap.put(TaskName,listOfSwcBox);
									}
									
									//Map�� ù ���� ���� ���� �� Task�� ù��° ������ ���� ���� ��
									if(listOfSwc.size() == 1){
										Iterator<String> keys = listOfTaskRelationMap.keySet().iterator();
										while(keys.hasNext()){
											String key = keys.next();
											
											//�� ���� ���� ������ Task�� �߰ߵ� ��� 
											if(key == TaskName)
												continue;
											else{
												listOfSwcBox.add(listOfSwc.get(listOfSwc.size()-1));
												listOfTaskRelationMap.put(TaskName,listOfSwcBox);
											}
										}
									}
							}
							
							//�ι� ° ���Ǻ��� Task�� Swc�� ���� ��ų ��
							else{						
								Map<String, List<String>> tempMap = new HashMap<>();
								List<String> listOfSwcBox = new ArrayList<String>(); //�ӽ� ���� ����Ʈ
											Iterator<String> keys = listOfTaskRelationMap.keySet().iterator();
											String key = null;
											while(keys.hasNext()){
												try{
													key = keys.next();													
												}catch(Exception e){
													System.out.println("error" + e);
												}

												
												//�ռ��� Swc�� ���ε� Task�� ���
												if(key == TaskName){
													listOfSwcBox = listOfTaskRelationMap.get(key);
													Iterator<String> values = listOfSwcBox.iterator();
													while(values.hasNext()){
														String value = values.next();
														//���� �� ���� ���� ������ Task�� �߰� �� ��쿡 �̹� ���ε��ִ� ������ �˻�(������ �Ǿ� ���� �ʰ� �׳� �ߺ��� Task�̸� �������� �ʰ� break
														if(value == listOfSwc.get(listOfSwc.size()-1))	//�ܼ��� �� ���ǳ����� �ߺ��� Task�� �߰ߵ� ���
															break;
														//�̹� �ٸ� Swc�� ������ �Ǿ��ִ� Task�� ��� �ʿ��� get���� ���ε� ����Ʈ�� �����ͼ� ���� ������ Swc�� �߰�
														else
															listOfTaskRelationMap.get(key).add(listOfSwc.get(listOfSwc.size()-1));
													}
												}
												else{
//											 		2. �̹� ���� �� Task�� ���� ��� ���ο� ArrayList �����Ͽ� Task�� ���ν�Ŵ 
													listOfSwcBox.add(listOfSwc.get(listOfSwc.size()-1));
													listOfTaskRelationMap.put(TaskName,listOfSwcBox);
													System.out.println(listOfTaskRelationMap);
												}
											}
							}
					}	//isCheckedSection
				}
					sBuffer.setLength(0);
					//isTaskParsingStarted = false;
			}
	
		}
		
		//SWC �̸� �� �Ľ�
		if(isSwcParsingStarted){
			String strValue1;
			sBuffer.append(new String(ch,start,length));
			strValue1 = sBuffer.toString().trim();
			
			
			if(strValue1 != null && strValue1.length()!=0 && !strValue1.equals("\n")){
				if(strValue1.contains("Instance_")){
					listOfSwc.add(strValue1);
					//System.out.println("elementName : "+elementName+", strValue : "+TaskName);
					}
					sBuffer.setLength(0);
					//isSwcParsingStarted = false;
				}
			}
		
		}	//characters method
}