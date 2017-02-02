package net.navagraha.hunter.action;

import java.util.List;

import net.navagraha.hunter.dao.ObjectDaoImpl;
import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.tool.MyHunterException;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

/**
 * 功能描述：个人能力Action
 * 
 * @author 冉椿林
 *
 */
public final class PowerAction extends AbstractObjAction {

	private int useId;// 与pojo属性对应，故命名保持一致

	/**
	 * 功能：获取能力值
	 * 
	 * @return
	 * @throws MyHunterException
	 */
	public String givePower() throws MyHunterException {

		json = giveJsonInstance();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int iUserId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		List<?> list = giveDaoInstance().getObjectListBycond("Power",
				"where powUser=" + iUserId);
		Power power = list.size() > 0 && list.get(0) instanceof Power ? (Power) list
				.get(0) : null;// 取出对象

		if (power == null) {// 为空则新建对象并初始化
			power = new Power();// 瞬态对象
			power.setPowCredit(50);
			power.setPowUser(user);
			power.setPowFast(0);
			giveDaoInstance().save(power);// 持久化对象
		}
		json.put("PowerList", power);// 放入数据到json

		setLevel2Json();// 设置level到json

		// 去掉json冗余数据powUser
		((JSONObject) json.get("PowerList")).remove("powUser");

		return "success";
	}

	/**
	 * 功能：通过用户Id获取能力值
	 * 
	 * @return
	 * @throws MyHunterException
	 */
	public String givePowerByUseId() throws MyHunterException {

		json = giveJsonInstance();

		List<?> list = giveDaoInstance().getObjectListBycond("Power",
				"where powUser=" + useId);
		Power power = list.size() > 0 && list.get(0) instanceof Power ? (Power) list
				.get(0) : null;

		if (power != null) {
			json.put("PowerList", power);// 放入数据到json

			setLevel2Json();// 设置level到json

			// 去掉json冗余数据powUser
			((JSONObject) json.get("PowerList")).remove("powUser");
		}

		return "success";
	}

	/**
	 * 功能：通过用户Id获取能力值(仅后台调用)
	 * 
	 * @param useId
	 * @return
	 */
	public static String givePowerByUseId(int useId) {

		List<?> list = new ObjectDaoImpl().getObjectListBycond("Power",
				"where powUser=" + useId);
		Power power = list.size() > 0 && list.get(0) instanceof Power ? (Power) list
				.get(0) : null;// 得到持久化对象

		if (power != null) {
			int credit = power.getPowCredit();
			return getLevel(credit);// 将信誉值根据算法转换为信誉等级并返回
		}

		return "";
	}

	/**
	 * 功能：取出json的powCredit转换为等级并设置到json
	 * 
	 * @throws MyHunterException
	 *
	 */
	public void setLevel2Json() throws MyHunterException {

		int iCredit = 50;
		try {
			iCredit = Integer.parseInt(((JSONObject) json.get("PowerList"))
					.get("powCredit").toString());// 取出持久化的信誉值
		} catch (NumberFormatException e) {
			throw new MyHunterException(e.getMessage());
		}
		// 将信誉值根据算法转换为信誉等级并放入json
		((JSONObject) json.get("PowerList")).put("powLevel", getLevel(iCredit));
	}

	/* 等级 */
	private final static String levelStr[] = new String[] { "F级兼职者", "E级兼职者",
			"D级兼职者", "C级兼职者", "B级兼职者", "A级兼职者" };

	/**
	 * 功能：根据信誉值计算段位
	 * 
	 * @param _credit
	 *            信誉值
	 * @return 段位
	 */
	public static String getLevel(int _credit) {
		int iBegin = 0, iCount = 1, iIndex = 0, iDiff = 3;

		for (; iDiff < 3 + 5 * ((0 + 2) / 2 + (1 + 2) / 2 + (2 + 2) / 2
				+ (3 + 2) / 2 + (4 + 2) / 2 + (5 + 2) / 2); iDiff += (iIndex + 2) / 2) {
			if (iCount % 6 == 0) {
				iIndex++;
				iCount = 1;
			}
			if (_credit >= iBegin && _credit <= iBegin + iDiff)
				return levelStr[iIndex].toString() + (6 - iCount % 6) + "段";

			if (_credit < 0)
				return "F级兼职者 5段";

			iBegin += iDiff + 1;
			iCount++;
		}

		return "S级兼职者";
	}

	/* setter、getter方法 */
	public void setUseId(int useId) {
		this.useId = useId;
	}
}
