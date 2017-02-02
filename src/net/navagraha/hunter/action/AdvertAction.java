package net.navagraha.hunter.action;

import java.util.ArrayList;
import java.util.List;

import net.navagraha.hunter.pojo.Advert;
import net.navagraha.hunter.tool.PropertyUtil;

/**
 * 功能描述：广告Action
 * 
 * @author 冉椿林
 *
 */
public final class AdvertAction extends AbstractObjAction {

	private static PropertyUtil oPropertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件,含有常用配置参数

	private static int ADV_PerPageRow;// 每页行数

	static {
		String sValue = oPropertyUtil.getPropertyValue("ADV_PerPageRow");
		try {
			ADV_PerPageRow = Integer.parseInt(sValue);// 任务大厅每页显示任务数量
		} catch (NumberFormatException e) {
			System.err.println("异常来自项目Hunter:\n" + e.getMessage());
		}
	}

	private int iBegin, iEnd;

	/**
	 * 功能：获取第一轮播广告列表
	 * 
	 * @return
	 */
	public String giveAdvert1() {

		iBegin = 0;
		iEnd = ADV_PerPageRow;
		giveAdvert();// 调用获取广告方法

		return "success";
	}

	/**
	 * 功能：获取第二轮播广告列表
	 * 
	 * @return
	 */
	public String giveAdvert2() {

		iBegin = ADV_PerPageRow + 1;
		iEnd = ADV_PerPageRow * 2;
		giveAdvert();// 调用获取广告方法

		return "success";
	}

	/**
	 * 功能：获取第三轮播广告列表
	 * 
	 * @return
	 */
	public String giveAdvert3() {

		iBegin = ADV_PerPageRow * 2 + 1;
		iEnd = ADV_PerPageRow * 3;
		giveAdvert();// 调用获取广告方法

		return "success";
	}

	/**
	 * 功能：获取广告
	 *
	 */
	public void giveAdvert() {
		json = giveJsonInstance();

		List<?> list = giveDaoInstance().pageListWithCond("Advert", iBegin,
				iEnd, "order by advHotlevel");// 从持久层获取数据
		if (list.size() == ADV_PerPageRow && list.get(0) instanceof Advert) {// 符合查询条件
			List<Advert> advertList = new ArrayList<Advert>();
			for (int i = 0; i < ADV_PerPageRow; i++) {
				advertList.add((Advert) list.get(i));
			}
			json.put("AdvertList", advertList);// 放入数据到json
		}
	}

}