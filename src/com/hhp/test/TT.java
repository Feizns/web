package com.hhp.test;

import com.hhp.util.auto.mapper.Dao;
import com.hhp.util.web.start.RegisterComponent;

public class TT implements RegisterComponent {

	@Override
	public Object[] getComponents() {
		return new Object[]{ Dao.getMapper(UserDao.class) };
	}
	
}
