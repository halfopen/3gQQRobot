package Robot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Scanner;

public class Robot {
	public static String Answer(String ask){
		String result="";
		String url = "http://api.mrtimo.com/Simsimi.ashx?parm=";
		try {
			url = url + java.net.URLEncoder.encode(ask,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			return("你说了什么我听不懂啊");
		}
		//System.out.println(url);
		try{
	        URL u = new URL( url);
	        InputStream in = u.openStream();
	        BufferedReader bin = new BufferedReader(new InputStreamReader(in, "utf-8"));
	        String s = null;
	        while((s=bin.readLine())!=null){
	            result += s;
	        }
	        //System.out.println(result);
	        bin.close();
	        }catch(Exception e){
	            e.printStackTrace();
	        }
		return result;
	}
	
	public static void main(String[] args){
	   Scanner scn = new Scanner(System.in);
	   System.out.println( Answer(scn.nextLine()) );
	}
}
