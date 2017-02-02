package net.navagraha.hunter.tool;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * 功能：过滤器
 * 
 * @author 冉椿林
 *
 * @since 1.0
 */
public class AllPowerfulCharsetFilter implements Filter {

	private String sCharacterEncoding;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (((HttpServletRequest) request).getMethod().equalsIgnoreCase("POST")) {// 解决post乱码(常规过滤器)

			request.setCharacterEncoding(sCharacterEncoding);
			response.setCharacterEncoding(sCharacterEncoding);
		}
		// else {// 解决get乱码
		// System.out.println("test0: " + request.getParameter("name"));
		// System.out.println("test: "
		// + new String(request.getParameter("name").getBytes(
		// "ISO8859-1"), "UTF-8"));
		//
		// HashMap paramsMap = new HashMap(request.getParameterMap());
		// for (Object key : paramsMap.keySet()) {
		//
		// //
		// 应使用request.getParameter(key.toString())获取参数，而非paramsMap.get(key.toString()),该方式获取将是LString类型
		// paramsMap
		// .put(key.toString(),
		// new String[] { new String((request
		// .getParameter(key.toString()))
		// .getBytes("ISO8859-1"), "UTF-8") });// 将参数转换为utf-8，解决get乱码
		// System.out.println(key);
		// System.out.println(new String((request
		// .getParameter((String) key)).getBytes("ISO8859-1"),
		// "UTF-8"));
		// }
		// request = new RequestWrapper((HttpServletRequest) request,
		// paramsMap);
		// }
		chain.doFilter(request, (HttpServletResponse) response);
	}

	public void init(FilterConfig config) throws ServletException {
		sCharacterEncoding = config.getInitParameter("encoding");
	}

	public void destroy() {
	}

}

/**
 * 功能：ServletRequest的封装类,封装ServletRequest只需要两个参数，一个是要封装的ServletRequest对象，
 * 一个是需要包含新的ServletRequest的key、value的Map
 * 
 * @param request
 * 
 * @param newParams
 * 
 * @author 冉椿林
 * 
 * @since 1.0
 */
@SuppressWarnings("unchecked")
class RequestWrapper extends HttpServletRequestWrapper {

	private Map params;

	public RequestWrapper(HttpServletRequest request, Map newParams) {
		super(request);
		this.params = newParams;
	}

	@Override
	public Map getParameterMap() {
		return params;
	}

	@Override
	public Enumeration getParameterNames() {
		Vector l = new Vector(params.keySet());
		return l.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		Object v = params.get(name);
		if (v == null) {
			return null;
		} else if (v instanceof String[]) {
			return (String[]) v;
		} else if (v instanceof String) {
			return new String[] { (String) v };
		} else {
			return new String[] { v.toString() };
		}
	}

}
