package com.hhp.util.web.start;

import java.util.Map;

public interface RegisterComponent {
	
	/**
	 * 
	 * @return
	 */
	default void register(Map<String, Object> components) { }
	
	/**
	 * 返回要注册的组件
	 * @return
	 */
	Object[] getComponents();
	
}
