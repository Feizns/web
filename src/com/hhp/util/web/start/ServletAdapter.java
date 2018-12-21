package com.hhp.util.web.start;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hhp.util.Convert;
import com.hhp.util.reflect.Asm5Util;
import com.hhp.util.string.StringUtil;
import com.hhp.util.web.annotation.ResponseBody;

public final class ServletAdapter {
	
	private static final String FORWARD = "forward:";
	
	private static final String REDIRECT = "redirect:";
	
	private Object original;
	
	private Method method;
	
	private String contentType;
	
	private boolean isResponseBody;
	
	private Map<String, Class<?>> argsMapping;

	public ServletAdapter(Object original, Method method, String conType) {
		this.original = original;
		this.method = method;
		this.contentType = conType;
		isResponseBody = method.getAnnotation(ResponseBody.class) != null;
		
		//构建参数映射关系
		initArgsMapping(method);
	}
	
	public Object[] parseParameter(HttpServletRequest req, HttpServletResponse resp) {
		if( argsMapping != null ) {
			Object[] args = new Object[argsMapping.size()];
			int index = 0;
			Iterator<Entry<String, Class<?>>> iterator = argsMapping.entrySet().iterator();
			while( iterator.hasNext() ) {
				Entry<String, Class<?>> item = iterator.next();
				String key = item.getKey();
				Class<?> val = item.getValue();
				if( ServletRequest.class.isAssignableFrom(val) ) {
					args[index] = req;
				} else if( ServletResponse.class.isAssignableFrom(val) ) {
					args[index] = resp;
				} else if( HttpSession.class.isAssignableFrom(val) ) {
					args[index] = req.getSession();
				} else if( ServletContext.class.isAssignableFrom(val) ) {
					args[index] = req.getServletContext();
				} else if( Convert.isSupport(val) ) {
					String[] values = req.getParameterMap().get(key);
					args[index] = valueOf(val, values != null && values.length == 1 ? values[0] : values);
				} else {
					args[index] = Convert.transfor(val, req);
				}
				index++;
			}
			
			return args;
		}
		return null;
	}
	
	private void initArgsMapping(Method method) {
		String[] parameterNames = Asm5Util.getParameterNamesByAsm5(method.getDeclaringClass(), method);
		Class<?>[] parameterTypes = method.getParameterTypes();
		if( parameterNames != null ) {
			argsMapping = new LinkedHashMap<>();
			for (int i = 0; i < parameterTypes.length; i++) {
				argsMapping.put(parameterNames[i], parameterTypes[i]);
			}
		}
	}
	
	public void invoke(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		Object[] args = parseParameter(req, resp);
		Object val = method.invoke(original, args);
		
		if( contentType != null ) {
			resp.setContentType(contentType);
		}
		
		if( val != null ) {
			if( isResponseBody == false && val.getClass() == String.class ) {
				if( StringUtil.startWithAndIgnoreCase(val, FORWARD) ) {
					req.getRequestDispatcher(val.toString().substring(FORWARD.length())).forward(req, resp);
				} else if ( StringUtil.startWithAndIgnoreCase(val, REDIRECT) ) {
					resp.sendRedirect(val.toString().substring(REDIRECT.length()));
				}
			} else if( isResponseBody ) {
				resp.getWriter().write(val.toString());
			}
		}
	}
	
	private static Object valueOf(Class<?> targetType, Object obj) {
		try {
			return Convert.valueOf(targetType, obj);
		} catch (RuntimeException e) {
			return null;
		}
	}
	
}
