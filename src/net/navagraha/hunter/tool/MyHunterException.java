package net.navagraha.hunter.tool;

/**
 * 功能描述：自定义异常
 * 
 * @author 冉椿林
 *
 * @since 1.0
 */
public class MyHunterException extends Exception {
	private static StringBuilder sb;
	static {
		sb = new StringBuilder("异常来自项目Hunter:\n");
	}

	public MyHunterException(String _sMessage) {
		super(sb.append(_sMessage).toString());
	}
}
