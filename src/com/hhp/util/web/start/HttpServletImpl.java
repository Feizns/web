package com.hhp.util.web.start;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hhp.util.web.annotation.RequestMapping;
import com.hhp.util.web.annotation.RequestMethod;

public class HttpServletImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7250783927783921784L;
	
	private Object original;
	
	private String contentType;
	
	private String urlPattern;
	
	private Map<RequestMethod, ServletAdapter> requestMethods = new HashMap<>();
	
	public void addRequestMethod(RequestMethod[] requestMethod, Method method) {
		ServletAdapter adapter = new ServletAdapter(original, method, contentType);
		if( requestMethod != null ) {
			for (int i = 0; i < requestMethod.length; i++) {
				requestMethods.put(requestMethod[i], adapter);
			}
		}
	}
	
	public HttpServletImpl(Object original, String urlPattern, Method method) {
		this.original = original;
		this.urlPattern = urlPattern;
		
		//类的全局配置
		RequestMapping classRequestMapping = original.getClass().getAnnotation(RequestMapping.class);
		if( classRequestMapping != null ) {
			contentType = classRequestMapping.contentType().trim().isEmpty() == false ? classRequestMapping.contentType() : null;  
		}

		//具体方法配置
		RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
		if( requestMapping != null ) {
			if( requestMapping.contentType().trim().isEmpty() == false ) {
				contentType = requestMapping.contentType();
			}
			RequestMethod[] methods = requestMapping.method();
			addRequestMethod(methods, method);
		}
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();
		if( method != null ) {
			ServletAdapter val = requestMethods.get(Enum.valueOf(RequestMethod.class, method.toUpperCase()));
			if( val != null ) {
				try {
					val.invoke(req, resp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("没有找到:" + req.getServletPath() + " ， " + method + " 映射.");
		}
	}
	
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}
	
	public String getUrlPattern() {
		return urlPattern;
	}
	
}
