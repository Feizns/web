package com.hhp.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hhp.util.web.annotation.Autowired;
import com.hhp.util.web.annotation.Controller;
import com.hhp.util.web.annotation.RequestMapping;
import com.hhp.util.web.annotation.RequestMethod;
import com.hhp.util.web.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class ControllerTest {
	
	@Autowired
	private UserDao userDao;
	
	@ResponseBody
	@RequestMapping("/doCmd")
	public String doCmd() {
		return "Hello World...";
	}
	
	@RequestMapping("/do")
	public String doCmd2(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws IOException {
		System.out.println(session.getId());
		return "forward:/user/doCmd";
	}
	
	@ResponseBody
	@RequestMapping(value="/hello", method={ RequestMethod.GET })
	public String hello(User id, String hello, String idw) throws IOException {
		System.out.println(hello);
		System.out.println(idw);
		System.out.println(id);
		System.out.println(userDao);
		return id + "";
	}
	
	@RequestMapping(value="/hello", method={ RequestMethod.POST })
	public String helloPost(String user) {
		System.out.println(user);
		return null;
	}

}