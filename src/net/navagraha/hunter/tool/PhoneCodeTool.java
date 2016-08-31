package net.navagraha.hunter.tool;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.navagraha.hunter.lib.PropertyUtil;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

public class PhoneCodeTool {

	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 加载参数配置文件

	private PhoneCodeTool() {
	}

	// POST方法提交/*验证码格式：您本次操作的验证码是。。。。*/
	public static boolean send(String phone, String code, String mode) {
		String text = "";
		if (mode.equals("yzm"))
			text = propertyUtil.getPropertyValue("Yzm") + code + "】，请勿泄露。";// 发送内容模板
		if (mode.equals("pwd")) {
			text = propertyUtil.getPropertyValue("Pwd") + code + "】，请勿泄露。";// 发送内容模板
		}
		if (mode.equals("task")) {
			text = propertyUtil.getPropertyValue("Task") + code
					+ "】已被完成，请前往审核。";// 发送内容模板
		}
		String userName = propertyUtil.getPropertyValue("UserName");// 短信平台用户名
		String passWord = propertyUtil.getPropertyValue("PassWord");// 短信平台密码

		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod("http://www.jc-chn.cn/smsSend.do?");// post提交
		post.addRequestHeader("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");// 在头文件中设置转码
		try {
			NameValuePair[] data = {
					new NameValuePair("username", userName), // 注册的用户名
					new NameValuePair("password", md5Encode(userName
							+ md5Encode(passWord, ""), "")), // 注册成功后,登录网站使用的密钥
					new NameValuePair("mobile", phone), // 手机号码
					new NameValuePair("content", URLEncoder.encode(text,
							"UTF-8")), new NameValuePair("dstime", "") };// 设置短信内容
			post.setRequestBody(data);

			client.executeMethod(post);

			String result = post.getResponseBodyAsString();
			if (result.length() > 3)
				return true;
		} catch (HttpException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			post.releaseConnection();
		}
		return false;
	}

	// 32位小写md5
	private static String md5Encode(String info, String key) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(info.getBytes("UTF-8"));
			StringBuilder result = new StringBuilder(32);
			byte[] temp;
			temp = md5.digest(key.getBytes("UTF-8"));
			for (int i = 0; i < temp.length; i++) {
				result.append(Integer.toHexString(
						(0x000000ff & temp[i]) | 0xffffff00).substring(6));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
