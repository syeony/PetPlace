package com.ssafy.cms.util;

import com.ssafy.cms.constant.DataMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * <PRE>
 * 1. ClassName : RequestUtil
 * 2. FileName  : RequestUtil.java
 * 3. Package  : framework.common.util
 * 4. Comment  : request 객체 관련 클래스
 * </PRE>
 */ 
public class RequestUtil {
	
	private static Log log = LogFactory.getLog(RequestUtil.class);
	
	/**
	 * <PRE>
	 * 1. MethodName : getDataMap
	 * 2. ClassName  : RequestUtil
	 * 3. Comment   : request 객체의 데이터를 dataMap 에 담기
	 * </PRE>
	 *   @return DataMap
	 *   @param request
	 *   @return
	 */
	public static DataMap getDataMap(HttpServletRequest request){
		
		DataMap dMap = new DataMap();		
		Enumeration<?> e = request.getParameterNames();
		
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String[] values = request.getParameterValues(key);
			if (values.length > 1) {
				List<Object> list = new ArrayList<Object>(values.length);
				for (int i = 0; i < values.length; i++) {					
					list.add(htmlTagFilter(values[i]));					
				}
				dMap.put(key, list);				
					
			} else {
				dMap.put(key, htmlTagFilter(request.getParameter(key)));				
			}			
		}
		
		return dMap;
	}
	
	public static String htmlTagFilter(String src) {

		if (src != null) {
			src = src.replaceAll("&", "&amp;");
			src = src.replaceAll("<", "&lt;");
			src = src.replaceAll(">", "&gt;");
			src = src.replaceAll("\"", "&quot;");	//쌍따옴표
			src = src.replaceAll("\'", "&#039;");	//작은따옴표
		}
		return src;
	}
	
	/**
	 * <PRE>
	 * 1. MethodName : getRemoteAddr
	 * 2. ClassName  : RequestUtil
	 * 3. Comment   : 프록시 서버를 걸쳐온 clientIP도 가져온다.
	 * </PRE>
	 *   @return String
	 *   @param request
	 *   @return
	 */
	public static String getRemoteAddr(HttpServletRequest request){
		String clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
		
		if(null == clientIp || clientIp.length() == 0 || clientIp.toLowerCase().equals("unknown")){
			clientIp = request.getHeader("REMOTE_ADDR");
		}
		
		if(null == clientIp || clientIp.length() == 0 || clientIp.toLowerCase().equals("unknown")){
			clientIp = request.getRemoteAddr();
		}
		
		return clientIp;
	}
}
