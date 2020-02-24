package com.suresofttech.car.provautosar.core.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String,ArrayList> a = new HashMap<String,ArrayList>();
		Map<String,ArrayList> b = new HashMap<String,ArrayList>();
		Map<String,ArrayList> c = new HashMap<String,ArrayList>();
		
		ArrayList<String> list0 = new ArrayList<>();
		list0.add("hello");
		list0.add("world");
		list0.add("good");
		
		
		ArrayList list1 = new ArrayList<>();
		list1.add("hello");
		list1.add("party");
		
		ArrayList list2 = new ArrayList<>();
		list2.addAll(list1);
		list2.add("whar areto");
	
		a.put("first",list0);
		b.put("first",list1);
		
		c.putAll(a);
		c.putAll(b);
		c.put("first", list2);
		
		
		
		for ( String key : c.keySet() ) {
    	    System.out.println("Key : " + key +" || Value : " + c.get(key));
    	}
		
		/*for(String asd : list0){
			System.out.println(asd);
		}*/
	}

}
