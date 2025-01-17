package com.suresofttech.provautosar.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//XML 문서의 시작이 인식되었을 때 발생하는 이벤트를 처리
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
		System.out.println("[파일] "+loc.getSystemId());
	}*/
	
	public void startDocument(){
		System.out.println("Start Document!");
		System.out.println("");
		//listOfTask.clear();
	}
	
	//XML 문서의 끝이 인식되었을 때 발생하는 이벤트를 처리
	public void endDocument(){
		System.out.println("End Document!");
		System.out.println("");
		
		//listOfTask에 담은 Task 이름들을 전체적으로 비교하여 중복되는 값은 제거하는 과정
		for(int i=0; i<listOfTask.size();i++){
			//System.out.println(listOfTask.size());
			String DuplicatedCheck = listOfTask.get(i);
			for(int j=0; j<listOfTask.size();j++){
				if(DuplicatedCheck.equals(listOfTask.get(j))&& i!=j){
					String a = listOfTask.remove(j);
					//System.out.println("삭제된  Task 이름 : "+a+", 삭제된 리스트의 인덱스 번호 : "+j);
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
			
			
			//기존 리스트들에 데이터를 덮어쓰지 않기 위해 초기화 
			listOfTask.clear();
			listOfSwc.clear();
		}

	
	//Element의 시작을 인식했을 때 발생하는 이벤트를 처리
	public void startElement(String uri,String localName,String qname,Attributes attr){
			//System.out.println("Start element, Name : "+qname);
			if(qname == "VALUE-REF")
				isTaskParsingStarted = true;
			if(qname == "SHORT-NAME")
				isSwcParsingStarted = true;
			
			//섹션에 들어가기 전 listOfTree에 element 이름들을 계속 추가(섹션에 들어간 후에는 추가하지 않음)
			if(!isCheckedSection){	
			listOfTree.add(qname);
			//listOfTree의 마지막 인덱스 값이 SHORT-NAME이고 그 위로 ELEMENT가 차례대로 ECUC-CONTAINER-VALUE, CONTAINERS인 경우 (많은 SHORT-NAME 중 Swc 이름만 가지고 있는 SHORT-NAME ELEMENT를 골라내기 위함)
			
			//if(qname == "SHORT-NAME") 도 가능
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
	//Element의  끝을 인식했을 때 발생하는 이벤트를 처리
	public void endElement(String uri,String localName,String qname){
		//System.out.println("End element, Name : "+qname);
		isTaskParsingStarted = false;
		isSwcParsingStarted = false;
		//ECUC-CONTAINER-VALUE가 섹션이 끝나는 Element일 경우  섹션을 나타내는 isCheckedSection = false로 설정 후 listOfTree의 SHORT-NAME, ECUC-CONTAINER-VALUE를 삭제(새로운 섹션의 Element를 받기 위함) 
		if(qname == "ECUC-CONTAINER-VALUE" && isCheckedSection){
			isCheckedSection = false;
			
			//listOfTree 요소중 끝의 2개 Element(ECU-CONTAINER-VALUE와 SHORT-NAME을 삭제함 . Why ? 새로운 Section에 진입하기 위함)
			int index = listOfTree.size();
			listOfTree.remove(index-1);
			listOfTree.remove(index-2);
		}
	}
	
	//각 Element의 값(인식된 문자의 각 세그먼트에 대해서 호출)
	public void characters(char[] ch,int start,int length) throws SAXException{
		//System.out.println(isParsingStarted);
		String strValue = "";	
		String TaskName = "";
		//System.out.println("isParsingStarted = "+isParsingStarted);
		
		//Task이름을 담은 VALUE-REF에서 Task 이름을  파싱
		if(isTaskParsingStarted){
			//Element의 값을 구하기 위해서는 Buffer에 인식된 각 문자를 start에서 length만큼 append한다.
			sBuffer.append(new String(ch,start,length));
			strValue = sBuffer.toString().trim();
			
		
			if(strValue != null && strValue.length()!=0 && !strValue.equals("\n")){
				if(strValue.contains("Os/OsTask")){		//Task 이름에 부합할 시
					TaskName = strValue.substring(12);
					listOfTask.add(TaskName);
					
					//구간에 진입한 상태의 Task일때
					if(isCheckedSection){
							//처음 섹션 구간에서의 Task, Swc 매핑 작업 할때
							if(listOfTaskRelationMap.isEmpty() || listOfSwc.size() == 1){		
								List<String> listOfSwcBox = new ArrayList<String>(); //임시 저장 리스트
								
									//Map에 처음 섹션 구간에서 Task와 Swc를 매핑시킬 때
									if(listOfTaskRelationMap.isEmpty()){
										//섹션에 진입했으면 listOfTree의 마지막 인덱스에 추가해놓은 Swc 이름을 가져옴
										listOfSwcBox.add(listOfSwc.get(listOfSwc.size()-1));
										listOfTaskRelationMap.put(TaskName,listOfSwcBox);
									}
									
									//Map에 첫 매핑 이후 아직 더 Task가 첫번째 구간에 남아 있을 때
									if(listOfSwc.size() == 1){
										Iterator<String> keys = listOfTaskRelationMap.keySet().iterator();
										while(keys.hasNext()){
											String key = keys.next();
											
											//한 섹션 내에 동일한 Task가 발견될 경우 
											if(key == TaskName)
												continue;
											else{
												listOfSwcBox.add(listOfSwc.get(listOfSwc.size()-1));
												listOfTaskRelationMap.put(TaskName,listOfSwcBox);
											}
										}
									}
							}
							
							//두번 째 섹션부터 Task와 Swc를 매핑 시킬 때
							else{						
								Map<String, List<String>> tempMap = new HashMap<>();
								List<String> listOfSwcBox = new ArrayList<String>(); //임시 저장 리스트
											Iterator<String> keys = listOfTaskRelationMap.keySet().iterator();
											String key = null;
											while(keys.hasNext()){
												try{
													key = keys.next();													
												}catch(Exception e){
													System.out.println("error" + e);
												}

												
												//앞서서 Swc와 매핑된 Task일 경우
												if(key == TaskName){
													listOfSwcBox = listOfTaskRelationMap.get(key);
													Iterator<String> values = listOfSwcBox.iterator();
													while(values.hasNext()){
														String value = values.next();
														//만약 한 섹션 내에 동일한 Task에 발견 될 경우에 이미 매핑되있는 것인지 검사(매핑이 되어 있지 않고 그냥 중복된 Task이면 매핑하지 않고 break
														if(value == listOfSwc.get(listOfSwc.size()-1))	//단순히 한 섹션내에서 중복된 Task가 발견될 경우
															break;
														//이미 다른 Swc와 매핑이 되어있는 Task의 경우 맵에서 get으로 매핑된 리스트를 가져와서 현재 섹션의 Swc를 추가
														else
															listOfTaskRelationMap.get(key).add(listOfSwc.get(listOfSwc.size()-1));
													}
												}
												else{
//											 		2. 이미 매핑 된 Task가 없는 경우 새로운 ArrayList 생성하여 Task와 매핑시킴 
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
		
		//SWC 이름 을 파싱
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