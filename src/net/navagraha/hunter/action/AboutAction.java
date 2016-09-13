package net.navagraha.hunter.action;

import java.util.List;

import net.navagraha.hunter.pojo.About;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.sf.json.JSONObject;

public class AboutAction {

	private static ObjectDao objectDao = new ObjectDaoImpl();
	public static JSONObject json;

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	// 获取关于
	public String giveAbout() {

		json = new JSONObject();

		List<?> list = giveDao().getObjectListBycond("About", "");
		if (list.size() > 0) {
			About about = (About) list.get(0);
			json.put("About", about);
		}
		return "success";
	}

	public JSONObject getJson() {
		return json;
	}

}
