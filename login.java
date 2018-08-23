package com.esp.manager.login.controller;

import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.esp.entity.S_USER_INFO;
import com.esp.entity.User;
import com.esp.manager.login.service.LoginService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Controller
@RequestMapping("loginController")
public class LoginController {

	@Autowired
	private LoginService service;

	@RequestMapping(value = "/getVerifyCode/{verifyCode}", method = RequestMethod.GET)
	public void getVerifyCode(@PathVariable("verifyCode") String verifyCode, HttpServletResponse response,
			HttpServletRequest request) {
		String resultData = "";
		PrintWriter out = null;
		try {
			if ("" == verifyCode) {
				resultData = "N";
			} else {
				String kaptchaValue = (String) request.getSession()
						.getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
				if (kaptchaValue == null || kaptchaValue == "" || !verifyCode.equalsIgnoreCase(kaptchaValue)) {
					resultData = "N";
				} else {
					resultData = "Y";
				}
			}
			out = response.getWriter();
			out.write(resultData);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	@RequestMapping(value = "/login/{account},{password},{rememberme}", method = RequestMethod.GET)
	public @ResponseBody Object login(@PathVariable("account") String account,
			@PathVariable("password") String password, @PathVariable("rememberme") String rememberme,
			HttpServletRequest request, HttpServletResponse response) {
		User user = service.login(account, password);
		if (null != user) {
			request.getSession().setAttribute("user", user);
			String loginInfo = user.getLoginName() + "-" + user.getPassword();
			if ("true".equals(rememberme)) {
				Cookie userCookie = new Cookie("loginUser", loginInfo);
				userCookie.setMaxAge(1 * 24 * 60 * 60); // 单位：秒
				userCookie.setPath("/");
				response.addCookie(userCookie);
			}
			return "suc";
		} else {
			return "err";
		}
	}

	@RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
	public @ResponseBody Object getUserInfo(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute("user");
		String account = user.getLoginName();
		String pwd = user.getPassword();
		S_USER_INFO userInfo = service.getUserInfo(account, pwd);
		request.getSession().setAttribute("s_user_info", userInfo);
		JSONObject jo = new JSONObject();
		JsonConfig jc = new JsonConfig();
		/*jc.setExcludes(new String[] {"company"});*/
		jo.put("userInfo", JSONArray.fromObject(userInfo,jc));
		return jo;
	}
}
