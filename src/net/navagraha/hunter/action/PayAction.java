package net.navagraha.hunter.action;

import java.util.ArrayList;
import java.util.List;

import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.tool.PropertyUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

/**
 * 功能描述：支付Action
 * 
 * @author 冉椿林
 *
 */
public final class PayAction extends AbstractObjAction {

	private static PropertyUtil oPropertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件,含有常用配置参数

	private static int PAY_PerPageRow;// 每页行数

	static {
		String sValue = oPropertyUtil.getPropertyValue("PAY_PerPageRow");
		try {
			PAY_PerPageRow = Integer.parseInt(sValue);// 任务大厅每页显示任务数量
		} catch (NumberFormatException e) {
			System.err.println("异常来自项目Hunter:\n" + "String转换int失败！"
					+ e.getMessage());
		}
	}

	// 前台传入
	private Integer curPage;// 当前页

	/**
	 * 功能：获取支付日志
	 * 
	 * @return
	 */
	public String givePayList() {

		json = giveJsonInstance();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int iUserId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		List<?> list = giveDaoInstance().pageListWithCond("Pay", curPage,
				PAY_PerPageRow,
				"where payUser=" + iUserId + " order by payTime desc");// 拿取数据

		// 放入数据到集合
		List<Pay> payList = new ArrayList<Pay>();
		for (int i = 0, iSize = list.size(); i < iSize; i++) {// 可随机访问使用for，需顺序访问才使用foreach，后者底层iterator实现，效率低些
			Object object = list.get(i);
			if (object instanceof Pay) // 是否支持强转
				payList.add((Pay) object);
		}

		// 放入数据到json
		json.put("PayList", payList);

		// 去掉json冗余数据payUser
		// 先取size，再判断，避免每次循环都要取size来比较，消耗效率
		for (int i = 0, iPayListSize = payList.size(); i < iPayListSize; i++) {
			((JSONObject) ((JSONArray) json.get("PayList")).get(i))
					.remove("payUser");
		}

		// 当前页为0表示取出全部数据
		if (curPage == 0) {
			List<?> list1 = giveDaoInstance().getObjectListBycond("Pay",
					"where payUser=" + iUserId);
			json.put("size", list1.size());// 放入数据到json
		}

		return "success";
	}

	/* setter、getter方法 */
	public void setCurPage(Integer curPage) {
		this.curPage = curPage;
	}
}
