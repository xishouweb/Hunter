package net.navagraha.hunter.action;

import java.util.List;

import net.navagraha.hunter.pojo.About;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.sf.json.JSONObject;

public class AboutAction {

	private static ObjectDao objectDao = new ObjectDaoImpl();
	public static JSONObject json = new JSONObject();

	// 获取关于
	public String giveAbout() {
		List<?> list = objectDao.getObjectListBycond("About", "");
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
