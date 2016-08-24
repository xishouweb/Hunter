package net.navagraha.hunter.tool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.navagraha.hunter.lib.ParameterRequestWrapper;

public class ShowParamFilter implements Filter {

	private String characterEncoding;

	public void destroy() {
	}

	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) {

		final HttpServletRequest newRequest = (HttpServletRequest) request;
		if (newRequest.getMethod().equalsIgnoreCase("POST")) {// post提交
			// 将参数设置为utf-8，解决post乱码
			try {
				request.setCharacterEncoding(characterEncoding);
			} catch (UnsupportedEncodingException e) {
				System.err
						.println("不支持的编码/ncause by:line33 in ShowParamFilter.java");
			}
			response.setCharacterEncoding(characterEncoding);
		}
		if (newRequest.getMethod().equalsIgnoreCase("GET")) {// get提交,用if而不用else，避免处理服务器反馈的response
			HashMap paramsMap = new HashMap(request.getParameterMap());
			for (Object key : paramsMap.keySet()) {
				// 注意，此次，通过key来获取value应该使用request.getParameter(key.toString())，
				// 而不是paramsMap.get(key.toString()),因为paramsMap.get获取的将是LString类型而不是String
				try {
					paramsMap.put(key.toString(), new String[] { new String(
							(request.getParameter(key.toString()))
									.getBytes("ISO8859-1"), "UTF-8") });
				} catch (UnsupportedEncodingException e) {
					System.err
							.println("不支持的编码/ncause by:line47 in ShowParamFilter.java");
				}
			}
			request = new ParameterRequestWrapper(newRequest, paramsMap);// 将新合成的Request赋给原来的request，让他继续前往后台
		}

		// 调用参数显示工具开始/*运行时注释掉即可*/
		// if (newRequest.getParameterNames().hasMoreElements()) {
		// final Alin_JAppletDubugTool jAppletDubugUtil = new
		// Alin_JAppletDubugTool();
		// Thread thread = new Thread() {
		// @Override
		// @SuppressWarnings( { "static-access" })
		// public void run() {
		//
		// List<String> list = new ArrayList<String>();
		// int i = 0;
		// Enumeration<String> enu = newRequest.getParameterNames();
		// while (enu.hasMoreElements()) {
		//
		// list.add(enu.nextElement().toString());
		// try {
		// String reqString = newRequest.getParameter(list
		// .get(i));
		//
		// if (java.nio.charset.Charset.forName("ISO8859-1")
		// .newEncoder().canEncode(reqString))//
		// 如果是ISO8859-1类型的参数，将其转为utf-8,，解决get乱码
		// list.add(new String(reqString
		// .getBytes("ISO8859-1"), "UTF-8"));
		// else
		// list.add(reqString);
		//
		// } catch (UnsupportedEncodingException e) {
		// System.err
		// .println("不支持的编码/ncause by:line64 in ShowParamFilter.java");
		// }
		// i += 2;
		// }
		// String[] str = (String[]) list.toArray(new String[list
		// .size()]);
		//
		// String[] newstr = new String[str.length + 1];
		// for (int j = 0; j < str.length; j++) {
		// if (j % 2 == 0)
		// newstr[j] = str[str.length - j - 1 - 1];
		// else
		// newstr[j] = str[str.length - j];
		// }
		// newstr[str.length] = " ";
		//
		// jAppletDubugUtil.main(newstr);
		// };
		// };
		// thread.start();
		// }
		// 调用参数显示工具结束/*运行时注释掉即可*/

		try {
			chain.doFilter(request, response);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

	public void init(FilterConfig config) throws ServletException {
		characterEncoding = config.getInitParameter("encoding");
	}

}
