package com.fuxi.core.vo.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestListAdd {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		List<Map<String,Object>> ls=new ArrayList<Map<String,Object>>();
		for(int i=0;i<3;++i){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("key"+String.valueOf(i), "value"+String.valueOf(i));
		
		ls.add(map);
		}
		Map<String,Object> map1=new HashMap<String,Object>();
		map1.put("key", "1111");
	    ls.add(0,map1);
	    
	    for(int i=0;i<ls.size();++i){
	    	System.out.println(ls.get(i));
	    }
		
		

	}

}
