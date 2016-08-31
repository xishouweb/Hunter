package net.navagraha.hunter.action;

import java.util.List;

import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

public class PowerAction {

	// Fields
	private int useId;

	private static ObjectDao objectDao = new ObjectDaoImpl();

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	// 反馈到前台
	public JSONObject json = new JSONObject();

	// 获取能力值
	public String givePower() {
		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		json = new JSONObject();

		List<?> list = giveDao().getObjectListBycond("Power",
				"where powUser=" + userId);
		Power power = list.size() > 0 ? (Power) list.get(0) : null;

		if (power == null) {
			power = new Power();
			power.setPowCredit(50);
			power.setPowUser(user);
			power.setPowFast(0);
			giveDao().save(power);
		}
		json.put("PowerList", power);
		int credit = Integer.parseInt(((JSONObject) json.get("PowerList"))
				.get("powCredit")
				+ "");
		((JSONObject) json.get("PowerList")).put("powLevel", getLevel(credit));
		((JSONObject) json.get("PowerList")).remove("powUser");
		return "success";
	}

	// 通过用户Id获取能力值
	public String givePowerByUseId() {

		List<?> list = giveDao().getObjectListBycond("Power",
				"where powUser=" + useId);
		Power power = list.size() > 0 ? (Power) list.get(0) : null;

		if (power != null) {
			json.put("PowerList", power);
			int credit = Integer.parseInt(((JSONObject) json.get("PowerList"))
					.get("powCredit")
					+ "");
			((JSONObject) json.get("PowerList")).put("powLevel",
					getLevel(credit));
			((JSONObject) json.get("PowerList")).remove("powUser");
		}
		return "success";
	}

	// 通过用户Id获取能力值(仅后台调用)
	public static String givePowerByUseId(int useId) {

		List<?> list = objectDao.getObjectListBycond("Power", "where powUser="
				+ useId);
		Power power = list.size() > 0 ? (Power) list.get(0) : null;

		if (power != null) {
			int credit = power.getPowCredit();
			return getLevel(credit);
		} else
			return "";
	}

	/** 等级算法 */
	private final static String levelStr[] = new String[] { "青铜猎手", "白银猎手",
			"黄金猎手", "铂金猎手", "钻石猎手", "超凡猎手" };

	/**
	 * 功能：根据信誉值计算段位
	 * 
	 * @param credit
	 *            信誉值
	 * @return 段位
	 */
	private static String getLevel(int credit) {
		int begin = 0, count = 1, index = 0, diff = 3;

		for (; diff < 3 + 5 * ((0 + 2) / 2 + (1 + 2) / 2 + (2 + 2) / 2
				+ (3 + 2) / 2 + (4 + 2) / 2 + (5 + 2) / 2); diff += (index + 2) / 2) {
			if (count % 6 == 0) {
				index++;
				count = 1;
			}
			if (credit >= begin && credit <= begin + diff)
				return levelStr[index] + (6 - count % 6);

			if (credit < 0)
				return "青铜猎手5";

			begin += diff + 1;
			count++;
		}
		return "王者猎手";
	}

	// Property accessors
	public void setUseId(int useId) {
		this.useId = useId;
	}

	public JSONObject getJson() {

		return json;
	}
}
