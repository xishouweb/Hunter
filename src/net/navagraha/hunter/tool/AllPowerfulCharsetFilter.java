package net.navagraha.hunter.tool;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.navagraha.hunter.lib.RequestWrapper;

public class AllPowerfulCharsetFilter implements Filter {

	private String characterEncoding;

	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (((HttpServletRequest) request).getMethod().equalsIgnoreCase("POST")) {// 解决post乱码(常规过滤器)

			request.setCharacterEncoding(characterEncoding);
			response.setCharacterEncoding(characterEncoding);
		} else {// 解决get乱码

			HashMap paramsMap = new HashMap(request.getParameterMap());
			for (Object key : paramsMap.keySet()) {

				// 应使用request.getParameter(key.toString())获取参数，而非paramsMap.get(key.toString()),该方式获取将是LString类型
				paramsMap
						.put(key.toString(),
								new String[] { new String((request
										.getParameter(key.toString()))
										.getBytes("ISO8859-1"), "UTF-8") });// 将参数转换为utf-8，解决get乱码
			}
			request = new RequestWrapper(request, paramsMap);
		}
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		characterEncoding = config.getInitParameter("encoding");
	}

	public void destroy() {
	}

}
