package net.navagraha.hunter.tool;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.xwork.StringUtils;

/**
 * 功能描述：读取配置文件
 * 
 * @author 冉椿林
 *
 * @since 1.0
 */
public class PropertyUtil {

	private static String sUrl;

	private static Properties properties = new Properties();

	@SuppressWarnings("unused")
	private PropertyUtil() {
	}// 屏蔽无参构造

	public PropertyUtil(String _url) {
		PropertyUtil.sUrl = _url;
		loadProperty();
	}

	/**
	 * 功能：加载配置文件
	 * 
	 * @throws MyHunterException
	 */
	public void loadProperty() {
		try {
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(sUrl));
		} catch (IOException e) {
			try {
				throw new MyHunterException(e.getMessage());
			} catch (MyHunterException e1) {
				return;
			}
		}
	}

	/**
	 * 获取值
	 * 
	 * @param key
	 *            配置文件的键
	 * @return 配置文件的值
	 */
	public String getPropertyValue(String key) {
		String sValue = (String) properties.get(key);
		if (StringUtils.isNotEmpty(sValue)) {
			return sValue;
		} else {
			return "";
		}

	}
}