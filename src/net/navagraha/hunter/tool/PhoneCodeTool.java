package net.navagraha.hunter.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * 功能描述：发送信息到手机
 * 
 * @author 冉椿林
 * 
 * @since 1.0
 */
public class PhoneCodeTool {

	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 加载参数配置文件

	private PhoneCodeTool() {
	}

	/**
	 * 功能：发送信息
	 * 
	 * POST方法提交 验证码格式：您本次操作的验证码是。。。。
	 * 
	 * @param _phone
	 * @param _code
	 * @param _mode
	 * @return
	 */
	public static boolean send(String _phone, String _code, String _mode) {
		String sText = "";
		if (_mode.equals("yzm"))
			sText = propertyUtil.getPropertyValue("Yzm") + _code + "】，请勿泄露。";// 发送内容模板
		if (_mode.equals("pwd"))
			sText = propertyUtil.getPropertyValue("Pwd") + _code + "】，请勿泄露。";// 发送内容模板
		if (_mode.equals("task"))
			sText = propertyUtil.getPropertyValue("Task") + _code
					+ "】已被完成，请前往审核。";// 发送内容模板
		if (_mode.equals("apply")) {
			sText = propertyUtil.getPropertyValue("Apply") + _code
					+ "】已被通过，请尽快完成。";// 发送内容模板
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
					new NameValuePair("mobile", _phone), // 手机号码
					new NameValuePair("content", URLEncoder.encode(sText,
							"UTF-8")), new NameValuePair("dstime", "") };// 设置短信内容
			post.setRequestBody(data);

			client.executeMethod(post);

			// 读取post返回信息
			InputStream is = post.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String sStr;
			while ((sStr = br.readLine()) != null)
				sb.append(sStr);

			String sResult = sb.toString();// / post.getResponseBodyAsString()
			if (sResult.length() > 3)
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

	/** 32位小写md5 */
	private static String md5Encode(String _info, String _key) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(_info.getBytes("UTF-8"));
			StringBuilder result = new StringBuilder(32);
			byte[] temp;
			temp = md5.digest(_key.getBytes("UTF-8"));
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
