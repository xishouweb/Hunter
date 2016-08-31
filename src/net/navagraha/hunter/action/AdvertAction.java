package net.navagraha.hunter.action;

import java.util.ArrayList;
import java.util.List;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Advert;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.sf.json.JSONObject;

public class AdvertAction implements java.io.Serializable {

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件
	private static int PerPageRow;
	static {
		PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("ADV_PerPageRow"));// 任务大厅每页显示任务数量
	}

	// 反馈到前台

	public String code;

	public static JSONObject json = new JSONObject();

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	// 获取第一轮播广告列表
	public String giveAdvert1() {

		List<?> list = giveDao().pageListWithCond("Advert", 0, PerPageRow,
				"order by advHotlevel");
		if (list.size() == PerPageRow) {
			List<Advert> advertList = new ArrayList<Advert>();
			for (int i = 0; i < PerPageRow; i++) {
				advertList.add((Advert) list.get(i));
			}
			json.put("AdvertList", advertList);
		}

		return "success";
	}

	// 获取第二轮播广告列表
	public String giveAdvert2() {

		List<?> list = giveDao().pageListWithCond("Advert", PerPageRow + 1,
				PerPageRow * 2, "order by advHotlevel");
		if (list.size() == PerPageRow) {
			List<Advert> advertList = new ArrayList<Advert>();
			for (int i = 0; i < PerPageRow; i++) {
				advertList.add((Advert) list.get(i));
			}
			json.put("AdvertList", advertList);
		}

		return "success";
	}

	// 获取第三轮播广告列表
	public String giveAdvert3() {

		List<?> list = giveDao().pageListWithCond("Advert", PerPageRow * 2 + 1,
				PerPageRow * 3, "order by advHotlevel");
		if (list.size() == PerPageRow) {
			List<Advert> advertList = new ArrayList<Advert>();
			for (int i = 0; i < PerPageRow; i++) {
				advertList.add((Advert) list.get(i));
			}
			json.put("AdvertList", advertList);
		}

		return "success";
	}

	// Property accessors

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public JSONObject getJson() {
		return json;
	}

}