package net.navagraha.hunter.action;

import java.util.ArrayList;
import java.util.List;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

public class PayAction {

	// Fields

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件
	private static int PAY_PerPageRow;

	static {
		PAY_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("PAY_PerPageRow"));// 任务大厅每页显示任务数量
	}

	// 前台传入
	private Integer curPage;

	// 反馈到前台
	public static JSONObject json = new JSONObject();

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	// 获取支付日志
	public String givePayList() {

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		json = new JSONObject();
		List<?> list = giveDao().pageListWithCond("Pay", curPage,
				PAY_PerPageRow,
				"where payUser=" + userId + " order by payTime desc");
		List<Pay> payList = new ArrayList<Pay>();
		for (Object object : list) {
			payList.add((Pay) object);
		}
		json.put("PayList", payList);

		for (int i = 0; i < payList.size(); i++) {
			((JSONObject) ((JSONArray) json.get("PayList")).get(i))
					.remove("payUser");
		}
		if (curPage == 0) {
			List<?> list1 = giveDao().getObjectListBycond("Pay",
					"where payUser=" + userId);
			json.put("size", list1.size());
		}

		return "success";
	}

	public void setCurPage(Integer curPage) {
		this.curPage = curPage;
	}

	public JSONObject getJson() {
		return json;
	}

}
