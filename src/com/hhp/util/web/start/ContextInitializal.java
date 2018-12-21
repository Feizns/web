package com.hhp.util.web.start;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import com.hhp.util.web.annotation.Autowired;
import com.hhp.util.web.annotation.Component;
import com.hhp.util.web.annotation.Controller;
import com.hhp.util.web.annotation.RequestMapping;


@HandlesTypes(Object.class)
public class ContextInitializal implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		
		Set<Class<?>> controllers = new HashSet<>();
		Map<String, Object> components = new HashMap<>();
		Set<RegisterComponent> registerComponent = new HashSet<>();
		c.forEach((item) -> {
			
			//控制器
			Controller con = item.getAnnotation(Controller.class);
			if( con != null ) {
				controllers.add(item);
			}
			
			//依赖组件
			Component com = item.getAnnotation(Component.class);
			if( com != null ) {
				String value = com.value();
				try {
					components.put(value.trim().isEmpty() ? item.getName() : value, item.newInstance());
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(item + ", 不存在无参构造函数.", e);
				}
			}
			
			//自定义组件
			if ( RegisterComponent.class.isAssignableFrom(item) 
					&& item.isInterface() == false 
					&& Modifier.isAbstract(item.getModifiers()) == false ) {
				try {
					registerComponent.add((RegisterComponent) item.newInstance());
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		});
		
		//添加自定义组件
		registerComponent.forEach((item) -> {
			Arrays.stream(item.getComponents()).forEach((item2) -> {
				components.put(item2.getClass().getName(), item2);
			});
		});
		
		//处理自定义注册组件
		registerComponent.forEach((item) -> {
			item.register(components);
		});
		
		//日志
		System.err.println("Component 组件");
		components.forEach((key, val) -> System.err.println("register: Component => name:" + key + " class:" + val.getClass()));
		System.err.println("Controller 组件");
		controllers.forEach((val) -> System.err.println("register: Contoller => " + val));
		System.err.println("Servlet 组件");
		
		Map<String, HttpServletImpl> mapping = new HashMap<>();
		//注册控制器
		controllers.forEach((item) -> {
			try {
				Object controller = autowired(components, item.newInstance());
				RequestMapping annotation = controller.getClass().getAnnotation(RequestMapping.class);
				String url = annotation != null ? prefix(annotation.value()) : "";
				for (Method method : item.getMethods()) {
					RequestMapping methodRM = method.getAnnotation(RequestMapping.class);
					if( methodRM != null ) {
						String realUrl = url + prefix(methodRM.value());
						HttpServletImpl impl = mapping.get(realUrl);
						if( impl != null ) {
							impl.addRequestMethod(methodRM.method(), method);
						} else {
							impl = new HttpServletImpl(controller, realUrl, method);
							mapping.put(realUrl, impl);
							ctx.addServlet(method.toString(), impl).addMapping(realUrl);
						}
						System.err.println("register: Servlet => " + realUrl + " METHOD:" + Arrays.toString(methodRM.method()));
					}
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		});
		
	}
	
	private static Object autowired(Map<String, Object> map, Object obj) throws ReflectiveOperationException {
		if( obj == null ) { return null; }
		Object newInstance = obj;
		for (Field field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			Autowired autowired = field.getAnnotation(Autowired.class);
			if( autowired != null && field.get(newInstance) == null ) {
				if( autowired.value().trim().isEmpty() ) {
					List<Object> list = map.values().stream().filter((item) -> field.getType().isAssignableFrom(item.getClass()))
					.collect(Collectors.toList());
					if( list.size() > 1 ) {
						throw new RuntimeException(field.getName() + " 知道创建类型不明确，因为找到了" + list.size()
							+ "个 " + field.getType().getName() + " 的类 " + " 分别为 : " 
							+ list.stream().map((item) -> item.getClass().getName()).collect(Collectors.joining("", "[", "]")));
					} else if ( list.size() == 0 ) {
						throw new RuntimeException("找不到" + field.getType() + "的类型");
					} else {
						field.set(newInstance, autowired(map, list.get(0)));
					}
				} else {
					Object autoClz = map.get(autowired.value());
					field.set(newInstance, autowired(map, autoClz));
				}
			}
		}
		return newInstance;
	}
	
	private static String prefix(String urlPattern) {
		if((urlPattern.trim().startsWith("/") || urlPattern.trim().startsWith("\\")) == false) {
			return "/" + urlPattern;
		}
		return urlPattern;
	}
	
}
