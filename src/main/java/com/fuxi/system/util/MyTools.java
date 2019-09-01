package com.fuxi.system.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MyTools {

    // List排序
    public static void sort(List<Map<String, Object>> list) {
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return Integer.valueOf(o1.get("IndexNo").toString()) > Integer.valueOf(o2.get("IndexNo").toString()) ? 1 : 0;
            }
        });
    }

    // 去除空值
    public static String formatObjectOfNumber(Object object) {
        String value = String.valueOf(object);
        if (null == value || "".equals(value) || "null".equalsIgnoreCase(value)) {
            value = "0";
        }
        return value;
    }

    /**
     * 保存内容写入文件
     * 
     * @param filePath
     * @param content
     */
    public static void contentToTxt(String filePath, String content) {
        try {
            File f = new File(filePath);
            if (f.exists()) {
                System.out.print("文件存在");
            } else {
                System.out.print("文件不存在");
                f.createNewFile();// 不存在则创建
            }
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f));
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(content);
            writer.flush();
            write.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 解析XML
     * 
     * @param fileName
     */
    public static Map<String, String> parserXml(String path) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(path);
            NodeList node = document.getElementsByTagName("Type");
            for (int i = 0; i < node.getLength(); i++) {
                Element element = (Element) node.item(i);
                String code = element.getAttribute("code");
                String value = element.getFirstChild().getNodeValue();
                map.put(code, value);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return map;
    }

    /**
     * 生成当前时间编码
     * 
     * @return
     */
    public static String generateCurrentTimeCode() {
        String code = null;
        SimpleDateFormat yyyymmddhhmmss = new SimpleDateFormat("yyMMddHHmmss");
        code = yyyymmddhhmmss.format(new Date());
        return code;
    }
    
    /*
     * 判断 货品图片是否存在
     * 
     * */
    public static String isExists(String fileName){
    	
    	String CodeName=null;
    	String path = ResourceUtil.getConfigByName("imgPath");//存放图片的文件夹
    	if(fileName !=null && !"".equals(fileName) ){
    		CodeName=fileName+".jpg";
    	}
    	File oldfile=new File(path+"/"+CodeName);
    	if(oldfile.exists()){
    	   return CodeName;
    	}
    	CodeName =fileName+".JPEG";
    	oldfile=new File(path+"/"+CodeName);
    	if(oldfile.exists()){
    		 return CodeName;
     	}
    	CodeName =fileName+".png";
    	oldfile=new File(path+"/"+CodeName);
    	if(oldfile.exists()){
    		 return CodeName;
     	}
    	
    	CodeName =fileName+".gif";
    	oldfile=new File(path+"/"+CodeName);
    	if(oldfile.exists()){
    		 return CodeName;
     	}
    	
    	
    	
    	return null;
    }
    

}
