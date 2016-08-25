package net.navagraha.hunter.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.navagraha.hunter.tool.PhoneCodeTool;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

public class UserAction {

	// Fields

	private Integer useId;
	private String useSno;
	private String useNickname;
	private String useName;
	private String usePwd;
	private String usePhone;
	private String useDepart;
	private String useCollege;
	private String useMajor;
	private String useEmei;

	// 前台传入

	private String newUsepassword;
	private String phoneCode;
	private String useCond;
	private String useSign;
	private String useAlipay;
	private int tasId;

	// 反馈到前台

	private String code;
	public static JSONObject json = new JSONObject();

	private static ObjectDao objectDao = new ObjectDaoImpl();

	// #用户激活
	public String activate() {

		// 查询已激活学生人数
		int size = objectDao
				.getObjectSizeBycond("select count(*) from Users where useIscompany==0");
		if (size > 100) {
			setCode("20");
			return "success";
		}

		List<?> list = objectDao.getObjectListByfieldInActivate("Users",
				"useSno", useSno);
		Users user = list.size() > 0 ? (Users) list.get(0) : null;

		if (user != null && user.getUseId() > 0) {

			Object object = ServletActionContext.getRequest().getSession()
					.getAttribute("phone_yzm");
			String sysPhoneCode = object != null ? (String) object : null;
			if (phoneCode.equals(sysPhoneCode)) {
				user.setUseIscompany(0);
				user.setUsePhone(usePhone);
				user.setUseEmei(useEmei);
				objectDao.update(user);
				setCode("1");// 激活成功
			} else
				setCode("7");// 手机验证码验证不成功
		} else
			setCode("4");// 考号/学号不存在

		return "success";
	}

	// #用户登录
	public String login() {
		List<?> list1 = objectDao.getObjectListByfield("Users", "usePhone",
				usePhone);
		if (list1.size() > 0) {
			List<?> list = objectDao.check4List("Users", usePhone, usePwd);
			if (list.size() > 0) {
				Users user = (Users) list.get(0);
				if (user.getUseIsprotect() == 1) {
					if (user.getUseEmei().equals(useEmei)) {
						ServletActionContext.getRequest().getSession()
								.setAttribute("Users", user);// 将登陆用户保存到session

						// 统计当前账户余额
						Object object = ServletActionContext
								.getServletContext().getAttribute("Money");
						if (object == null) {
							int userNum = Integer
									.parseInt(objectDao
											.getObjectListBycond(
													"select count(*) from Users where useIscompany!=2")
											.get(0).toString());
							ServletActionContext.getServletContext()
									.setAttribute("Money", userNum * 10 - 10);
						} else
							ServletActionContext.getServletContext()
									.setAttribute("Money",
											(Integer) object - 10);
						do4Tag(user);// 记录标签
						setCode("1");// 登录成功
					} else {
						setCode("8");// 不是本机登陆
					}
				} else {
					ServletActionContext.getRequest().getSession()
							.setAttribute("Users", user);// 将登陆用户保存到session
					do4Tag(user);// 记录标签
					setCode("1");// 登录成功
				}
			} else
				setCode("0");// 登录失败

		} else {
			setCode("17");// 手机号未激活
		}
		return "success";
	}

	// #记录标签
	private void do4Tag(Users user) {
		List<?> list = objectDao.getObjectListByfield("Tag", "tagUser", user);
		Tag tag = list.size() > 0 ? (Tag) list.get(0) : new Tag();

		// 记录登录时刻
		int a = 0, b = 0, c = 0, d = 0;
		if (list.size() > 0) {
			String[] strs = tag.getTagLogtime().split(",");
			a = Integer.parseInt(strs[0]);
			b = Integer.parseInt(strs[1]);
			c = Integer.parseInt(strs[2]);
			d = Integer.parseInt(strs[3]);
		} else {
			tag.setTagUser(user);
			tag.setTagSex("0,0");
			tag.setTagTimeout(0);
			tag.setTagTasktype("0,0,0");
		}
		// 上午族
		String A = "07:00:00";
		String A1 = "12:59:59";
		// 下午族
		String B = "13:00:00";
		String B1 = "17:59:59";
		// 晚上族
		String C = "18:00:00";
		String C1 = "23:59:59";
		// 夜猫族
		String D = "00:00:00";
		String D1 = "06:59:59";
		String current = new SimpleDateFormat("HH:mm:ss").format(new Date());
		if (compare(A, current) && compare(current, A1))
			a++;
		if (compare(B, current) && compare(current, B1))
			b++;
		if (compare(C, current) && compare(current, C1))
			c++;
		if (compare(D, current) && compare(current, D1))
			d++;
		tag.setTagLogtime(a + "," + b + "," + c + "," + d);
		objectDao.saveOrUpdate(tag);
		ServletActionContext.getRequest().getSession().setAttribute("Logtime",
				new Date().toLocaleString());// 存储登录时间
	}

	/** 比较两个时间 */
	private static boolean compare(String d1, String d2) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			Date dt1 = sdf.parse(d1);
			Date dt2 = sdf.parse(d2);
			return dt1.getTime() <= dt2.getTime();
		} catch (Exception e) {
			return false;
		}
	}

	// 获取当前账户余额
	public String giveMoney() {
		Object object = ServletActionContext.getServletContext().getAttribute(
				"Money");
		if (object == null) {
			json.put("Money", (String) object);
		}
		return "success";
	}

	// 用户注销
	public String quit() {

		// 将账户余额自动增加8
		Object object = ServletActionContext.getServletContext().getAttribute(
				"Money");
		if (object != null) {
			ServletActionContext.getServletContext().setAttribute("Money",
					(Integer) object + 8);
		}

		// 计算登录时长
		Object object1 = ServletActionContext.getServletContext().getAttribute(
				"Logtime");
		long diff = 0;
		if (object1 != null) {
			String Logtime = object1.toString();
			diff = getMinutesBetween(new Date().toLocaleString(), Logtime);

		}

		// 将登录时长加到数据库
		Object object2 = ServletActionContext.getServletContext().getAttribute(
				"Users");
		Users user = object2 != null ? (Users) object2 : null;
		if (user != null) {
			List<?> list = objectDao.getObjectListByfield("Tag", "tagUser",
					user);
			if (list.size() > 0) {
				Tag tag = (Tag) list.get(0);
				tag.setTagTimeout(tag.getTagTimeout()
						+ Integer.valueOf("" + diff));
				objectDao.update(tag);
			}

		}

		ServletActionContext.getRequest().getSession().invalidate();// 清空session

		return "success";
	}

	/** 计算两个时间的差，单位：分 */
	private static long getMinutesBetween(String s1, String s2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date dt1 = sdf.parse(s1);
			Date dt2 = sdf.parse(s2);
			return (dt1.getTime() - dt2.getTime()) / (60 * 1000);
		} catch (Exception e) {
			return 0;
		}

	}

	// 设置收款账户
	public String updateAlipay() {
		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null) {
			System.out.println(useAlipay);
			user.setUseAlipay(useAlipay);
			user.setUseName(useName);
			objectDao.update(user);
			setCode("1");// 修改收款账户成功
		} else
			setCode("0");// 修改收款账户失败

		return "success";
	}

	// 用户验证短信发送
	public String sendPhone() {
		if (useSno != null) {// 表示验证码模式-yzm
			List<?> list = objectDao.getObjectListByfieldInActivate("Users",
					"useSno", useSno);
			Users user = list.size() > 0 ? (Users) list.get(0) : null;
			if (user != null && user.getUseId() > 0) {
				if (user.getUsePwd().equals(usePwd)) {
					Random random = new Random();
					String code = "";
					for (int i = 0; i < 6; i++) {
						code += random.nextInt(10);
					}
					ServletActionContext.getRequest().getSession()
							.setAttribute("phone_yzm", code);
					// if (PhoneCodeTool.send(usePhone, code, "yzm")) {
					setCode("5");// 发送成功
					System.out.println(code);
					// } else
					// setCode("6");// 发送失败
				} else
					setCode("3");// 原密码不正确
			} else {
				setCode("4");// 考号/学号不存在
			}
		} else {// 忘记密码模式-pwd
			List<?> list = objectDao.getObjectListByfield("Users", "usePhone",
					usePhone);
			Users user = list.size() > 0 ? (Users) list.get(0) : null;
			if (user != null) {
				if (PhoneCodeTool.send(user.getUsePhone(), user.getUsePwd(),
						"pwd"))
					setCode("5");// 发送成功
				else
					setCode("6");// 发送失败
			} else
				setCode("17");// 手机号未激活
		}

		return "success";
	}

	/** 用户验证短信发送 */
	private String sendPhoneCode() {

		if (useSno != null) {// 表示验证码模式-yzm
			List<?> list = objectDao.getObjectListByfieldInActivate("Users",
					"useSno", useSno);
			Users user = list.size() > 0 ? (Users) list.get(0) : null;
			if (user != null && user.getUseId() > 0) {
				Random random = new Random();
				String code = "";
				for (int i = 0; i < 6; i++) {
					code += random.nextInt(10);
				}
				ServletActionContext.getRequest().getSession().setAttribute(
						"phone_yzm", code);
				if (PhoneCodeTool.send(usePhone, code, "yzm"))
					setCode("5");// 发送成功
				else
					setCode("6");// 发送失败
			} else {
				setCode("4");// 考号/学号不存在
			}
		} else {// 忘记密码模式-pwd
			List<?> list = objectDao.getObjectListByfield("Users", "usePhone",
					usePhone);
			Users user = list.size() > 0 ? (Users) list.get(0) : null;
			if (user != null) {
				if (PhoneCodeTool.send(user.getUsePhone(), user.getUsePwd(),
						"pwd"))
					setCode("5");// 发送成功
				else
					setCode("6");// 发送失败
			} else
				setCode("17");// 手机号未激活
		}

		return "success";
	}

	// #用户忘记密码
	public String forgetPass() {
		sendPhoneCode();
		return "success";
	}

	// 用户修改密码
	public String updatePass() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null && user.getUsePwd().equals(usePwd)) {
			user.setUsePwd(newUsepassword);
			objectDao.update(user);
			setCode("1");// 修改密码成功
		} else
			setCode("3");// 原密码不正确

		return "success";
	}

	// 用户修改手机号
	public String updatePhone() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null) {
			setUseSno(user.getUseSno());
			List<?> list = objectDao.getObjectListByfield("Users", "usePhone",
					usePhone);
			if (list.size() > 0) {
				setCode("18");// 手机号已存在
				return "success";
			}
			sendPhoneCode();
		}

		return "success";
	}

	// #用户信息修改
	public String updateInfo() {
		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		List<?> list = objectDao.getObjectListByfield("Users", "useNickname",
				useNickname);

		if (user != null && user.getUseId() > 0) {
			if (list.size() > 0) {
				for (Object object2 : list) {
					if (!((Users) object2).getUseId().toString().equals(
							user.getUseId().toString())) {
						setCode("2");// 昵称重名
						return "success";
					}
				}
			}
			user.setUseSign(useSign);
			user.setUseNickname(useNickname);
			user.setUseCollege(useCollege);
			user.setUseDepart(useDepart);
			user.setUseMajor(useMajor);
			try {
				objectDao.update(user);
				setCode("1");
			} catch (Exception e) {
				setCode("0");
			}
		} else
			setCode("0");

		return "success";
	}

	// 验证验证码，绑定手机号
	public String checkPhone() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null) {
			setUseSno(user.getUseSno());
			activate();
		}

		return "success";
	}

	// 获取当前用户
	public String giveCurrentUser() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");
		Users user = object != null ? (Users) object : null;
		json.put("User", user);

		return "success";
	}

	// 改变账号保护状态
	public String changeProtect() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");
		Users user = object != null ? (Users) object : null;
		if (user != null) {
			if (user.getUseIsprotect().intValue() == 0) // 关闭状态
				user.setUseIsprotect(1);
			else
				user.setUseIsprotect(0);
			objectDao.update(user);
			setCode("1");
		} else
			setCode("0");

		return "success";
	}

	// 改变隐身状态
	public String changeShowSign() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");
		Users user = object != null ? (Users) object : null;
		if (user != null) {
			if (user.getUseShowsign().intValue() == 0) // 关闭状态
				user.setUseShowsign(1);
			else
				user.setUseShowsign(0);
			objectDao.update(user);
			setCode("1");
		} else
			setCode("0");

		return "success";
	}

	// 用户点赞
	public String dianZan() {

		Object object = objectDao.getObjectById(Users.class, useId);
		Users user = object != null ? (Users) object : null;
		if (user != null && user.getUseId() > 0 && user.getUseIscompany() != 2) {
			user.setUseMoods(user.getUseMoods() + 1);
			objectDao.update(user);
			setCode("1");// 点赞成功
		} else
			setCode("0");// 点赞失败

		return "success";
	}

	// 根据用户ID获取用户
	public String giveUserById() {

		Object object = objectDao.getObjectById(Users.class, useId);
		Users user = object != null ? (Users) object : null;
		if (user.getUseShowsign() == 1 && user.getUseIscompany() != 2)// 用户没有隐身
			json.put("User", user);

		return "success";
	}

	// 搜索用户
	public String giveUserByCond() {

		String cond = "where useShowsign==1 and (useNickname=" + useCond
				+ " or useName=" + useCond + " or useSno=" + useCond
				+ " or usePhone=" + useCond + ")";

		List<?> list = objectDao.getObjectListBycond("Users", cond);
		List<Users> users = new ArrayList<Users>();
		for (Object object : list) {
			users.add((Users) object);
		}
		json.put("UserList", users);

		return "success";
	}

	// 根据任务查找接受者用户
	public String giveBeUsersByTasId() {
		Object object = objectDao.getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		if (task != null) {
			Set<Apply> set = task.getTasApplies();// 接受者用户们
			json.put("UserSet", set);
		}

		return "success";
	}

	public void setUseId(Integer useId) {
		this.useId = useId;
	}

	public void setUseSno(String useSno) {
		this.useSno = useSno;
	}

	public void setUseNickname(String useNickname) {
		this.useNickname = useNickname;
	}

	public void setUsePwd(String usePwd) {
		this.usePwd = usePwd;
	}

	public void setUsePhone(String usePhone) {
		this.usePhone = usePhone;
	}

	public void setUseDepart(String useDepart) {
		this.useDepart = useDepart;
	}

	public void setUseCollege(String useCollege) {
		this.useCollege = useCollege;
	}

	public void setUseMajor(String useMajor) {
		this.useMajor = useMajor;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setUseEmei(String useEmei) {
		this.useEmei = useEmei;
	}

	public void setNewUsepassword(String newUsepassword) {
		this.newUsepassword = newUsepassword;
	}

	public void setPhoneCode(String phoneCode) {
		this.phoneCode = phoneCode;
	}

	public void setTasId(int tasId) {
		this.tasId = tasId;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setCond(String useCond) {
		this.useCond = useCond;
	}

	public void setUseCond(String useCond) {
		this.useCond = useCond;
	}

	public void setUseSign(String useSign) {
		this.useSign = useSign;
	}

	public void setUseAlipay(String useAlipay) {
		this.useAlipay = useAlipay;
	}

	public void setUseName(String useName) {
		this.useName = useName;
	}

}
