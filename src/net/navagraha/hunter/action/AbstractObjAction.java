package net.navagraha.hunter.action;

import net.navagraha.hunter.dao.ObjectDao;
import net.navagraha.hunter.dao.ObjectDaoImpl;
import net.sf.json.JSONObject;

/**
 * 功能描述：所有Action的抽象父类
 * 
 * 定义了json、以及公用的giveDao方法
 * 
 * @author 冉椿林
 *
 */
public abstract class AbstractObjAction {

	// 反馈到前台
	public static JSONObject json;// json数据
	public String sCode;// 状态码

	/** setter、getter方法 */
	public final ObjectDao giveDaoInstance() {
		return objectDaoProvider.OBJECT_DAO;
	}

	public void setsCode(String sCode) {
		this.sCode = sCode;
	}

	public String getsCode() {
		return sCode;
	}

	public JSONObject getJson() {
		return json;
	}

	public final JSONObject giveJsonInstance() {
		// 使用前先初始化，避免同时访问Action两个方法时使用到了同一个json对象
		return new JSONObject();
	}

	// ///////////////////////////////////////////////////////////////////////////////////

	/** 静态内部类方式实现懒汉式单例模式 */
	private static class objectDaoProvider {
		private static final ObjectDao OBJECT_DAO = new ObjectDaoImpl();
	}
}
