package com.suresofttech.car.provautosar.core.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

public class Dom4jParserMain {

    public static void main(String[] args)
    {
    	Dom4jParserMain parse = new Dom4jParserMain();
        parse.run();
    }

    public void run()
    {
    	ArrayList EntireFilepath = new ArrayList<>();	//검사하려는 arxml 파일들의 경로를 담을 리스트
    	String odinPath = "C:\\Users\\슈어_공용\\Downloads\\3.분석대상파일";	//OdinPath를 담고 있는 문자열
			
    		//검사하려는 arxml 파일들의 경로를 추가하는 과정(경로 마지막의 폴더 안에는 최종적으로 파일만 있어야함. 만약 최종 경로의 폴더안에 파일과 폴더가 함께 존재할 경우 FileNotFoundException이 발생)
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Configuration\\System\\Swcd_Bsw"));
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Generated\\Bsw_Output\\swcd"));
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Generated\\Bsw_Output\\bswmd"));
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Configuration\\System\\Swcd_App"));
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Configuration\\System\\Bswmd"));
			EntireFilepath.addAll(GetEachFilepathList(odinPath + "\\Configuration\\System\\Composition"));
			
			//검사하려는 arxml 파일들의 경로들이 재대로 추가됬나 확인
			/*Iterator iter = EntireFilepath.iterator();
					while(iter.hasNext()){
						String a = ((File) iter.next()).getPath();
						System.out.println(a);
					}*/
			
			GetTaskRunnableMap(EntireFilepath);
    }
    
    /**
     * Task와 Runnable의 관계를 매핑한 맵을 반환하는 함수(Key : Task 이름, Value : Runnable 이름)
     * @param list
     * @return Map<String,<ArrayList<ArrayList>>
     */
    public static Map GetTaskRunnableMap(ArrayList list){
    	Map<String,ArrayList> TaskRunnableMap = new HashMap<String,ArrayList>();	//Task와 Runnable 관계를 나타낸 맵(최종적으로 반환할 맵)
    	Map<String,ArrayList> TaskSwcRelationMap = GetTaskSwcRelationMap();		//Task와 Swc이름을 매핑한 맵
    	Map<String,ArrayList> SwcBswRunnableMap = GetSwcBswRunnableMap(list);	//Swc이름과 Runnable 이름을 매핑한 맵
    	
    	//TaskSwcRelationMap의 Swc이름(Value값)과 SwcNameList의 Swc 이름(Key값)과 비교해서 Task 이름과 Runnable 이름을 매핑하는 과정
    	Iterator Taskiter = TaskSwcRelationMap.keySet().iterator();
    	while(Taskiter.hasNext()){
    		String TaskName = (String) Taskiter.next();
    		ArrayList<String> SwcNameList = TaskSwcRelationMap.get(TaskName);
    		ArrayList<String> Box = new ArrayList<>();	//TaskRunnableMap의 Value값으로 Put할 때 사용하는 임시 리스트
    		
    		for(String T : SwcNameList){
    			Iterator Swciter = SwcBswRunnableMap.keySet().iterator();
    			while(Swciter.hasNext()){
    				String SwcName = (String) Swciter.next();
    				ArrayList<String> RunnableList = SwcBswRunnableMap.get(SwcName);
    				if(T.equals(SwcName)){
    					for(String R : RunnableList){
    						Box.add(R);
    					}
    				}
    			}
    		}
    		TaskRunnableMap.put(TaskName, Box);
    	}
    	
    	//TaskRunnableMap에 맞게 매핑됬는지 출력
    	for ( String key : TaskRunnableMap.keySet() ) {
	    System.out.println("Task 이름 : " + key +" || Runnable 이름 : " + TaskRunnableMap.get(key));
		}
    	return TaskRunnableMap;
    }
    
    /**
     * Swc,Bsw와 Runnable의 관계를 나타낸 Map을 반환
     * @param list
     * @return Map<String,ArrayList<ArrayList>>
     */
    public static Map GetSwcBswRunnableMap(ArrayList list){
    	Map<String,ArrayList> SwcBswRunnableMap = new HashMap<String,ArrayList>();		//최종적으로 반환할 맵
    	Map<String,ArrayList> SwcRunnableMap = GetSwcRunnableMap(list);		//Swc이름과 Runnable들을 매핑한 맵
    	Map<String,ArrayList> BswRunnableMap = GetBswRunnableMap(list);		//Bsw의 Swc이름과 Runnable들을 매핑한 맵
    	ArrayList<String> Duplicated = new ArrayList<>();	//이미 매핑된 Swc를 제외하기 위해 사용하는 리스트(중복 제거)
    	SwcBswRunnableMap.putAll(SwcRunnableMap);	//반환할 맵에 먼저 SwcRunnableMap을 삽입
    	
    	/*SwcRunnableMap과 BswRunnableMap의 같은 Key(SwcRunnableMap에 Swc 이름이 SwcInstance_***,BswInstance_*** 가 모두 존재하기 때문)중
    	다른 Value값들이 존재함. 그대로 putAll을 하게 된다면 일부 Value값들이 덮어써져서 사라지기 때문에 직접 검사해서 넣는식으로 메서드를 구성*/
    	Iterator iter = SwcRunnableMap.keySet().iterator();
    	while(iter.hasNext()){
    		String SwcName = (String) iter.next();
    		
    		//BswRunnable 맵의 Key를 SwcRunnableMap의 Key와 비교하면서 중복이 되어있으면 해당 Key의 Value리스트에 추가하고, 중복되있지 않으면 맵에 그대로 Swc이름과 Runnable을 매핑
    		for(String T : BswRunnableMap.keySet()){
    			if(T.equals(SwcName)){
    				Duplicated.add(T);		//이미 검사한 Key는 Duplicated 리스트에 추가
    				ArrayList Box = new ArrayList<>();	//맵의 Value 값에 추가하기 위한 임시 리스트
    				ArrayList SwcRunnableList = SwcRunnableMap.get(SwcName);
    				Box.addAll(SwcRunnableList);
    				ArrayList BswRunnableList = BswRunnableMap.get(T);
    				Box.addAll(BswRunnableList);
    				ArrayList uniqueBox = new ArrayList(new HashSet<>(Box));
    				SwcBswRunnableMap.put(T, uniqueBox);
    			}
    			
    			//만약 SwcRunnableMap,BswRunnableMap의 Key값(Swc 이름)이 검사하지 않은 Key면 맵에 그대로 매핑 과정을 거침
    			else{
    				if(!Duplicated.contains(T)){
    					ArrayList Box0 = BswRunnableMap.get(T);		//최종 반환될 맵의 Value값에 매핑하기 위한 임시 리스트
    					SwcBswRunnableMap.put(T, Box0);
    				}
    			}
    		}
    	}
    	
    	//Map 프린팅해보기
    	/*for ( String key : SwcBswRunnableMap.keySet() ) {
    	    System.out.println("Swc 이름 : " + key +" || Runnable 이름 : " + SwcBswRunnableMap.get(key));
    	}*/
    	
    	return SwcBswRunnableMap;
    }
    
    /**
     * Bsw와 Runnable의 관계를 나타낸 Map을 반환
     * @param list
     * @return Map<String,ArrayList<ArrayList>>
     */
    public static Map GetBswRunnableMap(ArrayList list){
    	Map<String,ArrayList> BswRunnableMap = new HashMap<String,ArrayList>();		//최종적으로 반환할 맵
    	
    	ArrayList<ArrayList> SwcList = GetSwcList();	//Swc 이름과 경로를 저장한 리스트
    	ArrayList<ArrayList> BswRunnableShortNameList = GetBswRunnableShortNameList(list);		//Bsw Runnable과 SHORT-NAME을 매핑한 리스트
    	
    	Iterator iter = SwcList.iterator();
    	while(iter.hasNext()){
    		String SwcName = (String) ((ArrayList) iter.next()).get(0);
    		String ModifySwcName = "";			//Bsw와 SHORT-NAME을 비교하여 Runnable과 매핑하기 위해 사용되는 String   
    		String AutosarModifySwcName = "";		//EcuM_MainFunction, EcuM_LoopDetection의 SHORT-NAME이 BSW_** 형식을 따르는게 아니라 AUTOSAR_** 형식을 따르고 있음. 이 예외를 처리하기 위해 설정
    		ArrayList Box = new ArrayList<>();		//맵의 Value값으로 매핑하기 위해 사용되는 임시 리스트
    		
    		if(SwcName.contains("SwcInstance_"))	//Bsw는 Swc이름에 SwcInstance_*** 형식이 들어가지 않으므로 제외
    			continue;
    		
    		if(SwcName.contains("BswInstance_")){
    			ModifySwcName = SwcName.replace("BswInstance_","Bsw_");	//Bsw의 SHORT-NAME은 Bsw_***형식을 취하기 때문에 String을 수정해줌
    			AutosarModifySwcName = SwcName.replace("BswInstance_", "AUTOSAR_");	//EcuM_MainFunction, EcuM_LoopDetection의 SHORT-NAME이 BSW_** 형식을 따르는게 아니라 AUTOSAR_** 형식을 따르고 있음. 이 예외를 처리하기 위해 설정
    		}
    		
    		//Bsw의 Swc이름과 Runnable을 매핑하는 과정
    		for(ArrayList T : BswRunnableShortNameList){
    			String LowercaseModifySwcName = ModifySwcName.toLowerCase();
    			String LowercaseAutosarModifySwcName = AutosarModifySwcName.toLowerCase();
    			String LowercaseShortName = ((String) T.get(1)).toLowerCase();
    			
    			//EcuM_MainFunction, EcuM_LoopDetection의 SHORT-NAME이 BSW_** 형식을 따르는게 아니라 AUTOSAR_** 형식을 따르고 있음. 이 예외를 처리하기 위해 설정
    			if(LowercaseShortName.equals(LowercaseModifySwcName) || LowercaseShortName.equals(LowercaseAutosarModifySwcName)){
    				Box.add(T.get(0));
    			}
    		}
    		
    		//만약 Swc이름과 SHORT-NAME을 비교했을 때, 연관성이 잇으면 맵에 추가, 아무런 연관성이 없으면 맵에 추가하지 않고 그냥 넘어감.
    		if(Box.size()!=0)
    			BswRunnableMap.put(SwcName,Box);
    		else 
    			continue;
    	}
    	/*for ( String key : BswRunnableMap.keySet() ) {
    	    System.out.println("Swc 이름 : " + key +" || Runnable 이름 : " + BswRunnableMap.get(key));
    	}*/
    	return BswRunnableMap;
    }
    
    /**
     * Swc와 Runnable의 관계를 나타낸 Map을 반환
     * @param list
     * @return Map<String,ArrayList<ArrayList>>
     */
    public static Map GetSwcRunnableMap(ArrayList list){
    	Map<String,ArrayList> SwcRunnableMap = new HashMap<String,ArrayList>();		//최종적으로 반환할 맵
    	
    	ArrayList<ArrayList> SwcList = GetSwcList();	//Swc 이름과 경로를 저장하고 있는 리스트
    	ArrayList<ArrayList> SwcRunnableShortNameList = GetSwcRunnableShortNameList(list);	//Runnable이름과 그에 해당하는 SHORT-NAME을 저장한 리스트
    	
    	Iterator iter = SwcList.iterator();
    	while(iter.hasNext()){
    		String SwcName = (String) ((ArrayList) iter.next()).get(0);
    		String LowercaseSwcName = SwcName.toLowerCase();		//Swc이름과 SHORT-NAME을 비교하여 Runnable과 매핑하기 위해 Swc이름을 모두 소문자로 고침(대소문자 차이때문에 매칭이 안되는 예외가 있음)
    		ArrayList Box = new ArrayList<>();		//맵의 Value값에 추가하기 위한 임시 리스트
    		
    		//Swc이름과 Runnable을 Swc이름과 SHORT-NAME을 비교하여 매핑하는 과정
    		for(ArrayList T : SwcRunnableShortNameList){
    			String LowercaseShortName = ((String)T.get(1)).toLowerCase();	//Swc이름과 SHORT-NAME을 비교하여 Runnable과 매핑하기 위해 Swc이름을 모두 소문자로 고침(대소문자 차이때문에 매칭이 안되는 예외가 있음)
    			if(LowercaseSwcName.contains(LowercaseShortName)){
    				Box.add(T.get(0));
    			}
    		}
    		
    		//만약 Swc이름과 SHORT-NAME을 비교했을 때, 연관성이 잇으면 맵에 추가, 아무런 연관성이 없으면 맵에 추가하지 않고 그냥 넘어감.
    		if(Box.size()!=0)
    			SwcRunnableMap.put(SwcName,Box);
    		else 
    			continue;
    	}
    	/*for ( String key : SwcRunnableMap.keySet() ) {
    	    System.out.println("Swc 이름 : " + key +" || Runnable 이름 : " + SwcRunnableMap.get(key));
    	}*/
    	return SwcRunnableMap;
    }
   
    /**
     * Bsw Runnable 이름과 그에 따른 SHORT-NAME을 가지고 있는 리스트를 반환
     * @param list
     * @return ArrayList<ArrayList>
     */
    public static ArrayList GetBswRunnableShortNameList(ArrayList list){
    	ArrayList<ArrayList> BswRunnableShortNameList = new ArrayList<ArrayList>();	
    	Iterator iter = list.iterator();
    	
    	//파일을 읽을 때마다 Runnable과 SHORT-NAME을 매핑하는 구조(파일 전체를 읽고 매핑을 하니 잘못 매핑된 경우가 많았음)
    	while(iter.hasNext()){
    		ArrayList<ArrayList> BswRunnableList = new ArrayList<ArrayList>();
        	ArrayList<ArrayList> BswShortNameList = new ArrayList<ArrayList>();

        	ArrayList BswShortNameNodeList = null;
        	ArrayList BswRunnableNodeList = null;
        	
        	//File 배열에 들어있는 경로를 String으로 변환함
        	String StringFilepath = ((File) iter.next()).getPath();
    		File file = new File(StringFilepath);
    		SAXReader reader = new SAXReader();
    		Document doc;
    		
    		try{
    			doc = reader.read(file);
    			removeAllNamespaces(doc);
    			
    			//Runnable 이름에 해당하는 노드리스트를 ArrayList로 뽑아냄(Runnable 노드는 해당 경로에 존재)
    			BswRunnableNodeList = (ArrayList) doc.selectNodes("//ELEMENTS/BSW-MODULE-ENTRY/SHORT-NAME");
    			 for (int i = 0; i < BswRunnableNodeList.size(); i++){
    				 	//임시 리스트
    		        	ArrayList Box = new ArrayList();
    		        	Element el = (Element) BswRunnableNodeList.get(i);
    		            String NameofRunnable = el.getText();
    		            String PathofRunnable = el.getPath();
    		            String UniquePathofRunnable = el.getUniquePath();
    		            
    		            Box.add(NameofRunnable);
    		            Box.add(UniquePathofRunnable);
    		            //System.out.println(Box);
    		            BswRunnableList.add(Box);
    		     }
    			 
    			//Bsw의 SHORT-NAME을 ArrayList롤 뽑아냄(SHORT-NAME 노드는 해당 경로에 존재)
    			 BswShortNameNodeList = (ArrayList) doc.selectNodes("//AUTOSAR/AR-PACKAGES/AR-PACKAGE/SHORT-NAME");
        			 for (int i = 0; i < BswShortNameNodeList.size(); i++){
      		        	ArrayList Box = new ArrayList();
      		        	
      		        	Element el = (Element) BswShortNameNodeList.get(i);
      		            String NameofApplicationShortName = el.getText();
      		            String PathofApplicationShortName = el.getPath();
      		            String UniquePathofApplicationShortName = el.getUniquePath();
      		            
      		            Box.add(NameofApplicationShortName);
      		            Box.add(UniquePathofApplicationShortName);
      		            BswShortNameList.add(Box);
        			 }
        		
    		    	//Runnable 이름을 Bsw의 SHORT-NAME 이름과 연관시키는 과정
    		    	for(ArrayList T : BswShortNameList){
    		    		String Sname = (String) T.get(0);
    		    		String Spath = ((String) T.get(1)).replace("SHORT-NAME","");
    		    		for(ArrayList G : BswRunnableList){
    		    			ArrayList Box = new ArrayList<>();
    		    			String Rname = (String) G.get(0);
    		    			String Rpath = (String) G.get(1);
    		    			
    		    			//EcuM_MainFunction, EcuM_LoopDetection의 SHORT-NAME이 BSW_*** 형식을 따르는게 아니라 AUTOSAR_*** 형식을 따르고 있음. 이 예외를 처리하기 위해 설정
    		    			if((Rpath.contains(Spath) && Sname.contains("Bsw")) || (Rpath.contains(Spath) && Sname.contains("AUTOSAR"))){
    		    				Box.add(Rname);
    		    				Box.add(Sname);
    		    				BswRunnableShortNameList.add(Box);
    		    			}
    		    		}
    		    	}	 
    		}catch(DocumentException e){
    			e.printStackTrace();
    		}catch(ClassCastException e){	//조건에 부합하지 않아 노드가 발견되지 않을경우 무시하고 다음 노드를 탐색
    			continue;
    		}
    		
    		doc = null;
    		
    	}
    	
    	//EcuM_MainFunction, EcuM_LoopDetection의 SHORT-NAME이 중복으로 2개 들어가 있어서 중복이 제거된 ArrayList를 반환
    	ArrayList<ArrayList> FinalBswRunnableShortNameList = new ArrayList<ArrayList>(new HashSet<ArrayList>(BswRunnableShortNameList));
    	
    	/*for(int i=0;i<FinalBswRunnableShortNameList.size();i++){
    		ArrayList test = FinalBswRunnableShortNameList.get(i);
				System.out.println("Runnable 이름 : "+test.get(0)+" || SHORT-NAME 이름 : "+test.get(1));
    	}*/
    	
    	return FinalBswRunnableShortNameList;
    }
    
    
    /**
     * Swc Runnable 이름과 그에 따른 SHORT-NAME을 가지고 있는 리스트를 반환(SERVICE-SW-COMPONENT-TYPE과 APPLICATION-SW-COMPONENT-TYPE의 Swc Runnable과 SHORT-NAME 관계를 합침)
     * @param list
     * @return ArrayList<ArrayList>
     */
    public static ArrayList GetSwcRunnableShortNameList(ArrayList list){
    	ArrayList<ArrayList> SwcRunnableServiceShortNameList = GetSwcRunnableServiceShortNameList(list);
    	ArrayList<ArrayList> SwcRunnableApplicationShortNameList = GetSwcRunnableApplicationShortNameList(list);
    	
    	ArrayList<ArrayList> SwcRunnableShortNameList = new ArrayList<ArrayList>();
    	SwcRunnableShortNameList.addAll(SwcRunnableServiceShortNameList);
    	SwcRunnableShortNameList.addAll(SwcRunnableApplicationShortNameList);

    	return SwcRunnableShortNameList;
    }
    
    /**
     * Swc Runnable 이름과 그에 따른 APPLICATION SHORT-NAME을 가지고 있는 리스트를 반환
     * @param list
     * @return ArrayList<ArrayList> 
     */
    public static ArrayList GetSwcRunnableApplicationShortNameList(ArrayList list){
    	ArrayList<ArrayList> SwcRunnableApplicationShortNameList = new ArrayList<ArrayList>();
    	Iterator iter = list.iterator();
    	
    	//파일을 읽을 때마다 Runnable과 SHORT-NAME을 매핑하는 구조(파일 전체를 읽고 매핑을 하니 잘못 매핑된 경우가 많았음)
    	while(iter.hasNext()){
    		ArrayList<ArrayList> RunnableList = new ArrayList<ArrayList>();
        	ArrayList<ArrayList> ApplicationShortNameList = new ArrayList<ArrayList>();

        	ArrayList ApplicationShortNameNodeList = null;
        	ArrayList RunnableNodeList = null;
        	
        	//File 배열에 들어있는 경로를 String으로 변환함
        	String StringFilepath = ((File) iter.next()).getPath();
    		File file = new File(StringFilepath);
    		SAXReader reader = new SAXReader();
    		Document doc;
    		
    		try{
    			doc = reader.read(file);
    			removeAllNamespaces(doc);
    			
    			//Runnable 이름에 해당하는 노드리스트를 ArrayList로 뽑아냄(Runnable 노드는 해당 경로에 존재)
    			RunnableNodeList = (ArrayList) doc.selectNodes("//RUNNABLES/RUNNABLE-ENTITY/SYMBOL");
    			 for (int i = 0; i < RunnableNodeList.size(); i++){
    		        	ArrayList Box = new ArrayList();		//RunnableList에 넣기 위한 임시적인 ArrayList
    		        	Element el = (Element) RunnableNodeList.get(i);
    		            String NameofRunnable = el.getText();
    		            String PathofRunnable = el.getPath();
    		            String UniquePathofRunnable = el.getUniquePath();
    		            
    		            Box.add(NameofRunnable);
    		            Box.add(UniquePathofRunnable);
    		            RunnableList.add(Box);
    		     }
    			
    			//APPLICATION-SW-COMPONENT-TYPE에 해당하는 Swc의 SHORT-NAME을 ArrayList롤 뽑아냄(SHORT-NAME 노드는 해당 경로에 존재)
    				 ApplicationShortNameNodeList = (ArrayList) doc.selectNodes("//ELEMENTS/APPLICATION-SW-COMPONENT-TYPE/SHORT-NAME");
        			 for (int i = 0; i < ApplicationShortNameNodeList.size(); i++){
      				 	//임시 리스트
      		        	ArrayList Box = new ArrayList();
      		        	
      		        	Element el = (Element) ApplicationShortNameNodeList.get(i);
      		            String NameofApplicationShortName = el.getText();
      		            String PathofApplicationShortName = el.getPath();
      		            String UniquePathofApplicationShortName = el.getUniquePath();
      		            
      		            Box.add(NameofApplicationShortName);
      		            Box.add(UniquePathofApplicationShortName);
      		            ApplicationShortNameList.add(Box);
        			 }
        		
    		    	//Runnable 이름을 Swc APPLICATION-SW-COMPONENT-TYPE의 SHORT-NAME 이름과 연관시키는 과정
    		    	for(ArrayList T : ApplicationShortNameList){
    		    		String Sname = (String) T.get(0);
    		    		String Spath = ((String) T.get(1)).replace("SHORT-NAME","");
    		    		for(ArrayList G : RunnableList){
    		    			ArrayList Box = new ArrayList<>();
    		    			String Rname = (String) G.get(0);
    		    			String Rpath = (String) G.get(1);
    		    			
    		    			//Runnable 경로에 SHORT-NAME 경로가 포함되어 있으므로 포함관계를 확인한다.
    		    			if(Rpath.contains(Spath)){
    		    				Box.add(Rname);
    		    				Box.add(Sname);
    		    				SwcRunnableApplicationShortNameList.add(Box);
    		    			}
    		    		}
    		    	}	 
    		}catch(DocumentException e){
    			e.printStackTrace();
    		}catch(ClassCastException e){	//조건에 부합하지 않아 노드가 발견되지 않을경우 무시하고 다음 노드를 탐색
    			continue;
    		}
    		
    		doc = null;
    		
    	}
    	
    	/*for(int i=0;i<SwcRunnableApplicationShortNameList.size();i++){
    		ArrayList test = SwcRunnableApplicationShortNameList.get(i);
				System.out.println("Runnable 이름 : "+test.get(0)+" || SHORT-NAME 이름 : "+test.get(1));
    	}*/
    	return SwcRunnableApplicationShortNameList;
    }
    
    /**
     * Swc Runnable 이름과 그에 따른 SERVICE-SHORT-NAME을 가지고 있는 리스트를 반환
     * @param list
     * @return ArrayList<ArrayList> 
     */
    public static ArrayList GetSwcRunnableServiceShortNameList(ArrayList list){
    	ArrayList<ArrayList> SwcRunnableServiceShortNameList = new ArrayList<ArrayList>();	//최종적으로 반환할 리스트
    	Iterator iter = list.iterator();
    	
    	//파일을 읽을 때마다 Runnable과 SHORT-NAME을 매핑하는 구조(파일 전체를 읽고 매핑을 하니 잘못 매핑된 경우가 많았음)
    	while(iter.hasNext()){
    		ArrayList<ArrayList> RunnableList = new ArrayList<ArrayList>();
        	ArrayList<ArrayList> ServiceShortNameList = new ArrayList<ArrayList>();

        	ArrayList ServiceShortNameNodeList = null;
        	ArrayList RunnableNodeList = null;
        	
        	//File 배열에 들어있는 경로를 String으로 변환함
        	String StringFilepath = ((File) iter.next()).getPath();
    		File file = new File(StringFilepath);
    		SAXReader reader = new SAXReader();
    		Document doc;
    		
    		try{
    			doc = reader.read(file);
    			removeAllNamespaces(doc);
    			
    			//Runnable 이름에 해당하는 노드리스트를 ArrayList로 뽑아냄(Runnable 노드는 해당 경로에 존재)
    			RunnableNodeList = (ArrayList) doc.selectNodes("//RUNNABLES/RUNNABLE-ENTITY/SYMBOL");	
    			 for (int i = 0; i < RunnableNodeList.size(); i++){
    		        	ArrayList Box = new ArrayList();	//RunnableList에 넣기 위한 임시적인 ArrayList
    		        	Element el = (Element) RunnableNodeList.get(i);
    		            String NameofRunnable = el.getText();
    		            String PathofRunnable = el.getPath();
    		            String UniquePathofRunnable = el.getUniquePath();
    		            
    		            Box.add(NameofRunnable);
    		            Box.add(UniquePathofRunnable);
    		            RunnableList.add(Box);
    		     }
    			
    			 	//SERVICE-SW-COMPONENT-TYPE에 해당하는 Swc의 SHORT-NAME을 ArrayList롤 뽑아냄(SHORT-NAME 노드는 해당 경로에 존재)
    				 ServiceShortNameNodeList = (ArrayList) doc.selectNodes("//ELEMENTS/SERVICE-SW-COMPONENT-TYPE/SHORT-NAME");
        			 for (int i = 0; i < ServiceShortNameNodeList.size(); i++){
     		        	ArrayList Box = new ArrayList();		//ServiceShortNameList에 넣기 위한 임시적인 ArrayList
     		        	Element el = (Element) ServiceShortNameNodeList.get(i);
     		            String NameofServiceShortName = el.getText();
     		            String PathofServiceShortName  = el.getPath();
     		            String UniquePathofServiceShortName  = el.getUniquePath();
     		           
     		            Box.add(NameofServiceShortName);
     		            Box.add(UniquePathofServiceShortName);
     		            ServiceShortNameList.add(Box);
        			 }
    			 
    			//Runnable 이름을  SERVICE-SW-COMPONENT-TYPE의 SHORT-NAME에 해당하는 Swc의 SHORT-NAME과 연관시키는 과정
    		    	for(ArrayList T : ServiceShortNameList){
    		    		String Sname = (String) T.get(0);
    		    		String Spath = ((String) T.get(1)).replace("SHORT-NAME","");
    		    		for(ArrayList G : RunnableList){
    		    			ArrayList Box = new ArrayList<>();	//SwcRunnableServiceShortNameList에 넣기 위한 임시적인 ArrayList
    		    			String Rname = (String) G.get(0);
    		    			String Rpath = (String) G.get(1);
    		    			
    		    			//Runnable 경로에 SHORT-NAME 경로가 포함되어 있으므로 포함관계를 확인한다.
    		    			if(Rpath.contains(Spath)){
    		    				Box.add(Rname);
    		    				Box.add(Sname);
    		    				SwcRunnableServiceShortNameList.add(Box);
    		    			}
    		    		}
    		    	}
    		    		 
    		}catch(DocumentException e){
    			e.printStackTrace();
    		}catch(ClassCastException e){	//조건에 부합하지 않아 노드가 발견되지 않을경우 무시하고 다음 노드를 탐색
    			continue;
    		}
    		doc = null;
    	}
    	
    	/*for(int i=0;i<SwcRunnableServiceShortNameList.size();i++){
    		ArrayList test = SwcRunnableServiceShortNameList.get(i);
				System.out.println("Runnable 이름 : "+test.get(0)+" || SHORT-NAME 이름 : "+test.get(1));
    	}*/
    	
    	return SwcRunnableServiceShortNameList;
    }
    
    
    //filepath 경로 폴더내에 존재하는 모든 파일을 담은 리스트를 반환하는 함수
    public static ArrayList GetEachFilepathList(String filepath){
    	File file = new File(filepath);
    	SAXReader reader = new SAXReader();
    	Document doc;
    	ArrayList Fpath = new ArrayList<>();
    	try {
    		for(File f : file.listFiles()) {
				doc = reader.read(f);
				removeAllNamespaces(doc);
				Fpath.add(f);
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	doc = null;
    	return Fpath;
    }
    
    /**
     * Ecud_Rte.arxml에 존재하는 Swc 이름,해당 경로를 담은 리스트를 반환하는 함수 (삽입할 매개변수가 필요없음) 
     * @return
     */
    public static ArrayList<ArrayList> GetSwcList(){
    	ArrayList<ArrayList> SwcList = new ArrayList<ArrayList>();
    	String filePath = ".\\templates\\Ecud_Rte.arxml";
    	File file = new File(filePath);
		SAXReader reader = new SAXReader();
    	Document doc;
    	try {
    		doc = reader.read(file);
			removeAllNamespaces(doc);
    		ArrayList<ArrayList> listOfSwc = getSwcNodeList(doc,"//CONTAINERS/ECUC-CONTAINER-VALUE/SHORT-NAME");
    		for(ArrayList T : listOfSwc){
    			SwcList.add(T);
    		}
    	} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	/*for(int i=0;i<SwcList.size();i++){
    		ArrayList test = SwcList.get(i);
				System.out.println("Swc 이름 : "+test.get(0)+" || 경로 : "+test.get(1));
		 }*/
    	
    	return SwcList;
    }
    
    /**
     * Task-Swc 관계를 매핑한 맵을 반환하는 함수
     * @return
     */
    public static Map GetTaskSwcRelationMap(){
    	Map<String,ArrayList> FinalTaskSwcRelationmap = new HashMap<String,ArrayList>();	//Task-Swc 관계를 매핑한 맵(함수에서 최종적으로 반환할 맵)
    	String filePath = ".\\templates\\Ecud_Rte.arxml";	//Task와 Swc을 관계를 뽑아낼 수 있는 arxml 파일 경로
    	File file = new File(filePath);
		SAXReader reader = new SAXReader();
    	Document doc;
    	try {
			doc = reader.read(file);
			removeAllNamespaces(doc);		//doc의 Namespace를 제거해주는 함수(Namespace가 존재할 경우 파싱이 재대로 되지 않는 오류가 존재함. 따라서 doc을 불러오고 반드시 Namespace를 제거해주고 파싱작업을 시작해야함)
    	
		Map<String,ArrayList> TaskSwcRelationmap = new HashMap<String,ArrayList>();	//Task이름과  Swc 이름을 매핑한 맵(중복된 key 제거는 안되어잇음)
    	Map<String,ArrayList> TaskPathRelationmap = getTaskNodeMap(doc,"//ECUC-REFERENCE-VALUE/VALUE-REF");	//Task 이름과 그에 해당하는 경로를 매핑한 맵(Task 이름을 포함한 데이터는 해당 상대경로에 존재함) 
    	ArrayList<ArrayList> listOfSwc = getSwcNodeList(doc,"//CONTAINERS/ECUC-CONTAINER-VALUE/SHORT-NAME"); //Swc 이름과 그에 해당하는 경로를 가지고 있는 ArrayList<ArrayList>(Swc 이름을 포함한 데이터는 해당 상대경로에 존재함)
		
    	//Task 경로가 Swc 경로에 포함되어 있으므로 경로를 서로 비교하여 조건에 맞으면 TaskSwcRelationmap에 매핑하는 과정
    	for(String key : TaskPathRelationmap.keySet()){
    		ArrayList pathlistOfTask = new ArrayList();
    		pathlistOfTask = TaskPathRelationmap.get(key);
    		
    		//Task-Swc를 매핑하기 위한 임시 Swc 리스트
    		ArrayList listOfSwcBox = new ArrayList();
    		for(int i =0; i<pathlistOfTask.size();i++){
    			for(int j =0; j<listOfSwc.size();j++){
    				String AllpathOfSwc = (String) listOfSwc.get(j).get(1);
    				String FixpathOfSwc = AllpathOfSwc.replace("SHORT-NAME","");	//Swc이름은 최종 경로가 SHORT-NAME이므로 Task 경로와 포함여부를 확인하기 위해 SHORT-NAME을 경로에서 삭제
    				String SwcName = (String) listOfSwc.get(j).get(0);
    				String pathOfTask = (String) pathlistOfTask.get(i);
    				if(pathOfTask.contains(FixpathOfSwc))
    					listOfSwcBox.add(SwcName);
    				
    			}
    			
    			//한 Task에 대한 Swc매핑이 끝났으면 그동안 Swc이름을 저장한 listOfSwcBox와 Task이름을 매핑
    			if(i == pathlistOfTask.size()-1)
    				TaskSwcRelationmap.put(key,listOfSwcBox);
    		}
    			
    	}
    	
    	//TaskSwcRelationmap의 value값(리스트)에 중복된 Swc이름을 제거하는 작업
    	for(String key : TaskSwcRelationmap.keySet()){
    		ArrayList list = TaskSwcRelationmap.get(key);
    		ArrayList Newlist = new ArrayList(new HashSet(list));
    		FinalTaskSwcRelationmap.put(key, Newlist);
    	}
    	
    	
    	//Map 프린팅해보기
    	/*for ( String key : FinalTaskSwcRelationmap.keySet() ) {
    	    System.out.println("Taskname : " + key +" || Swcname : " + FinalTaskSwcRelationmap.get(key));
    	}*/
    	
    		
    	} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	doc = null;			
    	return FinalTaskSwcRelationmap;
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
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Element에서 Xpath로 결과값  추출
     * @param Element
     * @param Xpath
     * @return
     */
    public static String strGetNodeText(Element el, String strXpath)
    {
        try {
            Node node = el.selectSingleNode(strXpath);
            return node.getText();
        } catch (NullPointerException ne) {
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    
    //Swc 이름과 해당 경로를 저장하고 있는 리스트를 반환
    public static ArrayList getSwcNodeList(Document doc, String strXpath)
    {
        ArrayList list = (ArrayList) doc.selectNodes(strXpath);
        ArrayList<ArrayList> SwcPathRelationlist = new ArrayList<ArrayList>();
        for (int i = 0; i < list.size(); i++)
        {
        	//임시 리스트
        	ArrayList Box = new ArrayList();
            Element el = (Element) list.get(i);
            String NameofSwc = el.getText();
            String PathofSwc = el.getUniquePath();
            if(NameofSwc.contains("Instance_")){
            	Box.add(NameofSwc);
            	Box.add(PathofSwc);
            	SwcPathRelationlist.add(Box);
            }
        }
        /*for(int i=0;i<SwcPathRelationlist.size();i++){
    		ArrayList test = SwcPathRelationlist.get(i);
				System.out.println("Swc 이름 : "+test.get(0)+" || 경로 : "+test.get(1));
		 }*/
        return SwcPathRelationlist;
    }
    
    /**
     * Task 이름과 해당 경로들을 매핑한 맵을 반환(Key : Task 이름, Value : 경로들)
     * getTaskNodeList()보다 더 직관적임.
     * @param doc
     * @param strXpath
     * @return
     */
    public static Map getTaskNodeMap(Document doc, String strXpath){
    	Map<String,ArrayList> map = new HashMap<String,ArrayList>();	//Task와 해당 경로들을 매핑한 맵(함수에서 최종적으로 반환할 맵)
    	ArrayList list = (ArrayList) doc.selectNodes(strXpath);	//경로 strXpath에 해당하는 노드를 list에 저장 
    	ArrayList<ArrayList> TaskPathRelationlist = getTaskNodeList(doc,strXpath);
    	
        
        //TaskPathRelationlist를 맵으로 변환하는 과정(TaskPathRelationlist의 첫번째 인덱스 : Task이름, 나머지 인덱스 : 다수의 경로들)
        for(int i=0;i<TaskPathRelationlist.size();i++){
        	ArrayList PathListBox = new ArrayList<>();		//Task의 경로들을 담는 ArrayList
        	String Taskname = (String) TaskPathRelationlist.get(i).get(0);
        	
        	//Task의 경로는 리스트의 인덱스 1번부터 이므로 j=1부터 시작하여 경로들을 모두 PathListBox에 담는다.
        	for(int j=1;j<TaskPathRelationlist.get(i).size();j++){
        		PathListBox.add(TaskPathRelationlist.get(i).get(j));
        		
        		//만약 PathListBox에 Task의 경로들을 모두 담았으면 map에 Task이름과 매핑시킨다.
        		if(PathListBox.size() == TaskPathRelationlist.get(i).size()-1)
        			map.put(Taskname, PathListBox);
        	}
        }
        
        //Task와 그에 해당하는 경로들을 매핑한 Map을 반환
    	return map;
    }
    
    /**
     * Document에서 경로 strXpath에 해당되는 모든 노드를 리턴
     * Task 이름과 해당 경로를 저장하고 있는 리스트를 반환(TaskPathRelationlist안에 존재하는 리스트들의 첫번째 인덱스 : Task 이름, 나머지 인덱스 : 다수의 경로들)
     * @param doc
     * @param strXpath
     * @return
     */
    public static ArrayList getTaskNodeList(Document doc, String strXpath)
    {
    	ArrayList list = (ArrayList) doc.selectNodes(strXpath);		//경로 strXpath에 해당하는 노드를 list에 저장 
    	ArrayList<ArrayList> TaskPathRelationlist = new ArrayList<ArrayList>();	//Task 이름과 해당 경로를 저장하고 있는 리스트(TaskPathRelationlist안에 존재하는 리스트들의 첫번째 인덱스 : Task 이름, 나머지 인덱스 : 다수의 경로들)
    	
    	//저장한 list안의 노드에서 Task이름과 경로를 뽑아내는 과정
        for (int i = 0; i < list.size(); i++){
        	ArrayList Box = new ArrayList();	//TaskPathRelationlist 안에 리스트를 넣기 위한 임시 ArrayList 
        	Element el = (Element) list.get(i);	//list안에 있는 노드를 element형태로 변환하여 뽑아냄
        	
        	/*getText()함수를 이용하여 element들의 데이터를 String의 형태로 뽑아냄. Task 이름을 가지고 있는 데이터의 형태는 '/AUTOSAR/Os/OsTask_BSW_FG1_10ms_Sub1'로
        	 *  되어있기 때문에 Task이름만 뽑아내기 위해 substring() 함수를 사용
        	 */
        	String NameofTask = el.getText().substring(12);	
            String PathofTask = el.getUniquePath();	//뽑아낸 데이터의 경로를 뽑아내기 위해 getUniquePath() 함수 사용. getPath()함수는 경로의 배열값까지 상세히 나오지 않음. 따라서 getUniquePath()함수 사용.
            
            //뽑아낸 데이터의 문자열에 OsTask가 포함되어있으면 임시리스트 Box에 추가하고 TaskPathRelationlist에 추가.
            if(NameofTask.contains("OsTask")){
            	Box.add(NameofTask);
            	Box.add(PathofTask);
            	TaskPathRelationlist.add(Box);
            }
        }
        
      /*
       * Task가 여러 Swc를 호출하기 때문에 경로도 여러가지이다. 위에서는 여러가지 경로를 고려하지 않고 바로바로 add 했기 때문에  Task 이름이 각각 다른 경로로 중복되어서 리스트에 따로 존재한다. 
       * ex)[[Task1,경로1],[Task1,경로2]....] 
       * 따라서  여러가지 경로를 하나의 Task 이름에 매핑해줘야 한다. ex)[[Task1,경로1,경로2,...],[Task2,경로4,경로5,...]] 
       */
        for(int i=0; i<TaskPathRelationlist.size();i++){
			ArrayList DuplicatedCheck = new ArrayList<>();	
			DuplicatedCheck = TaskPathRelationlist.get(i);
			for(int j=0; j<TaskPathRelationlist.size();j++){
				if(DuplicatedCheck.get(0).equals(TaskPathRelationlist.get(j).get(0))&& i!=j){
					if(DuplicatedCheck.get(1) != TaskPathRelationlist.get(j).get(1)){
						TaskPathRelationlist.get(i).add(TaskPathRelationlist.get(j).get(1));
						TaskPathRelationlist.remove(j);
						j-=1;
						continue;
					}
				}
			}
		}
        return TaskPathRelationlist;
    }

    /**
     * document에서 xpath에 해당되는 모든 element를 리턴
     * @param doc
     * @param strXpath
     * @return
     */
    public static ArrayList getNodeList(Document doc, String strXpath)
    {
        return (ArrayList) doc.selectNodes(strXpath);
    }
    
    /**
     * element에서 xpath에 해당되는 모든 element를 리턴
     * @param element
     * @param strXpath
     * @return
     */
    public static ArrayList getNodeList(Element element, String strXpath)
    {
        return (ArrayList) element.selectNodes(strXpath);	
    }
    
    /**
     * doc의 Namespace를 제거해주는 함수(Namespace가 존재할 경우 파싱이 재대로 되지 않는 오류가 존재함. 따라서 doc을 불러오고 반드시 Namespace를 제거해주고 파싱작업을 시작해야함)
     * @param doc
     */
    public static void removeAllNamespaces(Document doc) {
        Element root = doc.getRootElement();
        if (root.getNamespace() !=
                Namespace.NO_NAMESPACE) {            
                removeNamespaces(root.content());
        }
    }

    public static void unfixNamespaces(Document doc, Namespace original) {
        Element root = doc.getRootElement();
        if (original != null) {
            setNamespaces(root.content(), original);
        }
    }

    public static void setNamespace(Element elem, Namespace ns) {

        elem.setQName(QName.get(elem.getName(), ns,
                elem.getQualifiedName()));
    }

    /**
     *Recursively removes the namespace of the element and all its
    children: sets to Namespace.NO_NAMESPACE
     */
    public static void removeNamespaces(Element elem) {
        setNamespaces(elem, Namespace.NO_NAMESPACE);
    }

    /**
     *Recursively removes the namespace of the list and all its
    children: sets to Namespace.NO_NAMESPACE
     */
    public static void removeNamespaces(List l) {
        setNamespaces(l, Namespace.NO_NAMESPACE);
    }

    /**
     *Recursively sets the namespace of the element and all its children.
     */
    public static void setNamespaces(Element elem, Namespace ns) {
        setNamespace(elem, ns);
        setNamespaces(elem.content(), ns);
    }

    /**
     *Recursively sets the namespace of the List and all children if the
    current namespace is match
     */
    public static void setNamespaces(List l, Namespace ns) {
        Node n = null;
        for (int i = 0; i < l.size(); i++) {
            n = (Node) l.get(i);

            if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                ((Attribute) n).setNamespace(ns);
            }
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                setNamespaces((Element) n, ns);
            }            
        }
    }
}
