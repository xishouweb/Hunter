package net.navagraha.hunter.action;

import java.util.List;

import net.navagraha.hunter.pojo.About;

/**
 * 功能描述：关于Action
 * 
 * @author 冉椿林
 *
 */
public final class AboutAction extends AbstractObjAction {

	/**
	 * 功能：获取关于信息
	 * 
	 * @return
	 */
	public String giveAbout() {

		json = giveJsonInstance();

		List<?> list = giveDaoInstance().getObjectListBycond("About", "");
		if (list.size() > 0) {// 如果有数据
			if (list.get(0) instanceof About) {// 转换前进行类型判断，避免使用try，catch来捕获，提高效率
				About about = (About) list.get(0);
				json.put("About", about);// 放入数据到json
			}
		}
		return "success";
	}
}
