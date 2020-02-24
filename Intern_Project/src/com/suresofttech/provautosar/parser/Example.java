package com.suresofttech.provautosar.parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class Example {
	private static Map <String,List<String>> map = new HashMap<String,List<String>>();
	private static List<String> list1 = new ArrayList<String>();
	private static List<String> list2 = new ArrayList<String>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		list1.add("hello");
		list1.add("world");
		list2.add("what?");
		map.put("swc1",list1);
		map.put("swc2", list2);
		
		Iterator<String> itr = list1.iterator();
		while(itr.hasNext()){
			String word = itr.next();
			System.out.println(word);
		}
	}

}
