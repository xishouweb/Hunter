package net.navagraha.hunter.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Census;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.tool.MyHunterException;
import net.navagraha.hunter.tool.PhoneCodeTool;

import org.apache.struts2.ServletActionContext;

/**
 * 功能描述：用户Action
 * 
 * @author 冉椿林
 *
 */
public class UserAction extends AbstractObjAction {

	// 与pojo属性对应，故命名保持一致
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
	private String useSign;
	private Integer useAge;
	private String useAlipay;

	// 前台传入
	private String newUsepassword;
	private String phoneCode;
	private String useCond;
	private String mode;
	private int tasId;
	private double payNum;

	/**
	 * 功能：用户激活
	 * 
	 * @return
	 */
	public String activate() {

		// // 查询已激活学生人数,内测限定100(投入时取消)
		// int size = giveDao().getObjectSizeBycond(
		// "select count(*) from Users where useIscompany==0");
		// if (size > 100) {
		// setsCode("20");
		// return "success";
		// }

		List<?> list = giveDaoInstance().getObjectListByfieldInActivate(
				"Users", "useSno", useSno);
		Users user = list.size() > 0 && list.get(0) instanceof Users ? (Users) list
				.get(0) : null;

		if (user != null && user.getUseId() > 0 && user.getUseIscompany() != 3
				&& user.getUseIscompany() != 5) {// 验证用户是否为非激活状态

			Object object = ServletActionContext.getRequest().getSession()
					.getAttribute("phone_yzm");// 从session中取出生成的验证码
			String sysPhoneCode = object != null ? (String) object : null;
			if (sysPhoneCode.equals(phoneCode)) {// 比对用户输入验证码与生成的验证码是否一致
				user.setUseIscompany(0);// 设置为激活状态
				user.setUsePhone(usePhone);
				user.setUseEmei(useEmei);
				giveDaoInstance().update(user);
				setsCode("1");// 激活成功
			} else
				setsCode("7");// 手机验证码验证不成功
		} else
			setsCode("4");// 学号不存在

		return "success";
	}

	/**
	 * 功能：用户登录
	 * 
	 * @return
	 * @throws MyHunterException
	 */
	public String login() throws MyHunterException {

		List<?> list1 = giveDaoInstance().getObjectListByfield("Users",
				"usePhone", usePhone);

		if (list1.size() > 0) {// 先查是否存在该用户名

			List<?> list = giveDaoInstance().check4List("Users", usePhone,
					usePwd);
			if (list.size() > 0 && list.get(0) instanceof Users) {// 再查是否存在该用户名+密码

				Users user = (Users) list.get(0);

				if (user.getUseIsprotect() == 1) {// 是否开启了账号保护
					if (user.getUseEmei().equals(useEmei)) {// 验证设备码是否正确
						do4User(user);// 记录用户
						setsCode("1");// 登录成功
					} else
						setsCode("8");// 非本机登陆，登录失败
				} else {
					do4User(user);// 记录用户
					this.setsCode("1");// 登录成功
				}
			} else
				setsCode("0");// 密码不正确，登录失败
		} else
			setsCode("17");// 手机号未激活
		return "success";
	}

	/** 记录用户 */
	private void do4User(Users _user) throws MyHunterException {

		_user.setUseIsonline(1);// 设置用户在线状态
		_user.setUseIslogin(1);// 设置该用户今日已登录
		giveDaoInstance().update(_user);

		// 更新今日同时在线人数
		Date date = new Date();
		String month = new SimpleDateFormat("yyyy-MM").format(date);
		int day = Integer.parseInt(new SimpleDateFormat("dd").format(date));
		// 今日此时同时在线人数
		int onlineNum = giveDaoInstance().getObjectSizeBycond(
				"select count(*) from Users where useIsonline=1");

		List<?> li = giveDaoInstance().getObjectListBycond(
				"from Census where cenMonth='" + month + "' and cenDay=" + day);
		Census census;

		if (li.size() < 1 || !(li.get(0) instanceof Census)) {// 今日记录是否存在，不存在则新建并初始化
			census = new Census();
			census.setCenMonth(month);
			census.setCenDay(day);
			census.setCenOnlinenum(0);
			census.setCenLoginnum(0);
			census.setCenActivetotal(0);
			census.setCenActivenum(0);
		} else
			census = (Census) li.get(0);

		if (onlineNum > census.getCenOnlinenum())// 设置更高的在线人数并持久化
			census.setCenOnlinenum(onlineNum);
		giveDaoInstance().saveOrUpdate(census);

		// 获取标签
		List<?> list = giveDaoInstance().getObjectListByfield("Tag", "tagUser",
				_user);
		Tag tag = list.size() > 0 && list.get(0) instanceof Tag ? (Tag) list
				.get(0) : new Tag();

		// 记录登录时刻
		int iA = 0, iB = 0, iC = 0, iD = 0;

		if (list.size() > 0) {// 如果是从数据库中拉取，即先前有值存在
			String[] strs = tag.getTagLogtime().split(",");
			try {
				iA = Integer.parseInt(strs[0]);
				iB = Integer.parseInt(strs[1]);
				iC = Integer.parseInt(strs[2]);
				iD = Integer.parseInt(strs[3]);
			} catch (NumberFormatException e) {
				throw new MyHunterException(e.getMessage());
			}
		} else {// 新建的tag，非数据库拉取
			tag.setTagUser(_user);
			tag.setTagSex("0,0");
			tag.setTagTimeout(0);
			tag.setTagTasktype("0,0,0");
		}
		// 判断：是哪个时区登录，则记录相应时区登录的数字+1
		// 上午族
		String sA = "07:00:00";
		String sA1 = "12:59:59";
		// 下午族
		String sB = "13:00:00";
		String sB1 = "17:59:59";
		// 晚上族
		String sC = "18:00:00";
		String sC1 = "23:59:59";
		// 夜猫族
		String sD = "00:00:00";
		String sD1 = "06:59:59";
		String current = new SimpleDateFormat("HH:mm:ss").format(new Date());

		if (compare(sA, current) && compare(current, sA1))
			iA++;
		if (compare(sB, current) && compare(current, sB1))
			iB++;
		if (compare(sC, current) && compare(current, sC1))
			iC++;
		if (compare(sD, current) && compare(current, sD1))
			iD++;
		tag.setTagLogtime(iA + "," + iB + "," + iC + "," + iD);
		giveDaoInstance().saveOrUpdate(tag);// 记录后保存
		ServletActionContext
				.getRequest()
				.getSession()
				.setAttribute(
						"Logtime",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date()));// 存储登录时间

		ServletActionContext.getRequest().getSession()
				.setAttribute("Users", _user);// 将登陆用户保存到session
	}

	/** 计算两个时间的差，单位：分 */
	@Deprecated
	private static long getMinutesBetween(String _s1, String _s2) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			Date dt1 = sdf.parse(_s1);
			Date dt2 = sdf.parse(_s2);
			return (dt1.getTime() - dt2.getTime()) / (60 * 1000);
		} catch (Exception e) {
			return 0;
		}
	}

	/** 比较两个时间 */
	private static boolean compare(String _d1, String _d2) {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			Date dt1 = sdf.parse(_d1);
			Date dt2 = sdf.parse(_d2);
			return dt1.getTime() <= dt2.getTime();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 功能：#用户注销
	 * 
	 * @return
	 */
	@Deprecated
	public String quit() {

		// 计算登录时长
		Object object1 = ServletActionContext.getRequest().getSession()
				.getAttribute("Logtime");
		long diff = 0;

		if (object1 != null) {
			String sLogtime = object1.toString();
			diff = getMinutesBetween(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()),
					sLogtime);
		}

		// 将登录时长加到数据库
		Object object2 = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");
		Users user = object2 != null ? (Users) object2 : null;

		if (user != null) {
			List<?> list = giveDaoInstance().getObjectListByfield("Tag",
					"tagUser", user);
			if (list.size() > 0) {
				Tag tag = (Tag) list.get(0);
				tag.setTagTimeout(tag.getTagTimeout()
						+ Integer.valueOf("" + diff));
				giveDaoInstance().update(tag);
			}
			// 设置用户在线状态
			user.setUseIsonline(0);
			giveDaoInstance().update(user);
		}
		ServletActionContext.getRequest().getSession().invalidate();// 清空session

		return "success";
	}

	/**
	 * 功能：设置收款账户
	 * 
	 * @return
	 */
	public String updateAlipay() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null) {
			Object obj = ServletActionContext.getRequest().getSession()
					.getAttribute("phone_yzm");
			String sysPhoneCode = obj != null ? (String) obj : "";
			if (sysPhoneCode.equals(phoneCode)) {// 校验验证码
				user.setUseAlipay(useAlipay);
				user.setUseName(useName);
				giveDaoInstance().update(user);
				ServletActionContext.getRequest().getSession()
						.setAttribute("Users", user);// 将更新用户存入session
				setsCode("1");// 修改收款账户成功
			} else
				setsCode("7");// 手机验证码验证不成功
		} else
			setsCode("0");// 修改收款账户失败

		return "success";
	}

	/**
	 * 功能：用户验证码发送
	 * 
	 * @return
	 */
	public String sendPhone() {

		if (useSno != null) {// 1.表示激活账户时的验证码
			List<?> list = giveDaoInstance().getObjectListByfieldInActivate(
					"Users", "useSno", useSno);
			Users user = list.size() > 0 && list.get(0) instanceof Users ? (Users) list
					.get(0) : null;

			if (user != null && user.getUseId() > 0) {// 检验user
				if (user.getUsePwd().equals(usePwd)) {// 校验初始密码是否正确

					/* 随机生成六位数的验证码 */
					Random random = new Random();
					String code = "";
					for (int i = 0; i < 6; i++)
						code += random.nextInt(10);

					ServletActionContext.getRequest().getSession()
							.setAttribute("phone_yzm", code);// 保存验证码到session
					if (PhoneCodeTool.send(usePhone, code, "yzm")) {// 调用工具类发送验证码到指定手机，验证码模式-yzm
						setsCode("5");// 发送成功
					} else
						setsCode("6");// 发送失败
				} else
					setsCode("3");// 原密码不正确
			} else {
				setsCode("4");// 考号/学号不存在
			}
			return "success";

		} else {// 表示非激活账户时的其他类型验证码

			Object object = ServletActionContext.getRequest().getSession()
					.getAttribute("Users");// 将登陆用户取出
			Users user = object != null ? (Users) object : null;

			if (mode != null && mode.equals("new")) {// 2.表示重绑手机时的验证码

				if (user != null) {
					List<?> list = giveDaoInstance().getObjectListByfield(
							"Users", "usePhone", usePhone);
					if (list.size() > 0) {
						setsCode("18");// 绑定失败，已绑定该手机号
						return "success";
					}

					Random random = new Random();
					String code = "";
					for (int i = 0; i < 6; i++)
						code += random.nextInt(10);

					ServletActionContext.getRequest().getSession()
							.setAttribute("phone_yzm", code);

					if (PhoneCodeTool.send(usePhone, code, "yzm"))// 可以绑定，发送绑定手机验证码
						setsCode("5");// 发送成功
					else
						setsCode("6");// 发送失败
				}
				return "success";
			}

			if (mode != null && mode.equals("server")) {// 3.服务器验证码发送（无需前台传入phone,直接从session中取得登录用户手机号）

				if (user != null) {
					Random random = new Random();
					String code = "";
					for (int i = 0; i < 6; i++)
						code += random.nextInt(10);

					ServletActionContext.getRequest().getSession()
							.setAttribute("phone_yzm", code);

					if (PhoneCodeTool.send(user.getUsePhone(), code, "yzm"))
						setsCode("5");// 发送成功
					else
						setsCode("6");// 发送失败
				}
				return "success";

			} else {// 4.普通验证码发送

				Random random = new Random();
				String code = "";
				for (int i = 0; i < 6; i++)
					code += random.nextInt(10);

				ServletActionContext.getRequest().getSession()
						.setAttribute("phone_yzm", code);

				if (PhoneCodeTool.send(usePhone, code, "yzm"))
					setsCode("5");// 发送成功
				else
					setsCode("6");// 发送失败
				return "success";
			}
		}
	}

	/**
	 * 功能：用户忘记密码
	 * 
	 * @return
	 */
	public String forgetPass() {

		List<?> list = giveDaoInstance().getObjectListByfield("Users",
				"usePhone", usePhone);
		Users user = list.size() > 0 && list.get(0) instanceof Users ? (Users) list
				.get(0) : null;

		if (user != null) {
			if (PhoneCodeTool.send(user.getUsePhone(), user.getUsePwd(), "pwd"))// 调用工具类发送原密码到指定手机，忘记密码模式-pwd
				setsCode("5");// 发送成功
			else
				setsCode("6");// 发送失败
		} else
			setsCode("17");// 手机号未激活

		return "success";
	}

	/**
	 * 功能：用户修改密码
	 * 
	 * @return
	 */
	public String updatePass() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null && user.getUsePwd().equals(usePwd)) {// 校验操作
			user.setUsePwd(newUsepassword);
			giveDaoInstance().update(user);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", user);// 将更新用户存入session
			setsCode("1");// 修改密码成功
		} else
			setsCode("3");// 原密码不正确

		return "success";
	}

	/**
	 * 功能：用户修改手机号
	 * 
	 * @return
	 */
	public String updatePhone() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		if (user != null) {

			Object obj = ServletActionContext.getRequest().getSession()
					.getAttribute("phone_yzm");
			String sysPhoneCode = obj != null ? (String) obj : "";

			if (sysPhoneCode.equals(phoneCode)) {// 校验验证码
				user.setUsePhone(usePhone);
				user.setUseEmei(useEmei);
				giveDaoInstance().update(user);
				ServletActionContext.getRequest().getSession()
						.setAttribute("Users", user);// 将更新用户存入session
				setsCode("1");// 绑定成功
			} else
				setsCode("7");// 手机验证码验证不成功
		}
		return "success";
	}

	/**
	 * 功能：用户详细信息修改
	 * 
	 * @return
	 */
	public String updateInfo() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		/* 校验是否昵称重名 */
		List<?> list = giveDaoInstance().getObjectListByfield("Users",
				"useNickname", useNickname);

		if (user != null && user.getUseId() > 0) {
			if (list.size() > 0) {
				for (Object object2 : list) {
					if (!((Users) object2).getUseId().toString()
							.equals(user.getUseId().toString())) {
						setsCode("2");// 昵称重名
						return "success";
					}
				}
			}
			/* 校验昵称重名结束 */
			user.setUseAge(useAge);
			user.setUseSign(useSign);
			user.setUseNickname(useNickname);
			user.setUseCollege(useCollege);
			user.setUseDepart(useDepart);
			user.setUseMajor(useMajor);
			try {// 此处易出现异常，故处理
				giveDaoInstance().update(user);
				ServletActionContext.getRequest().getSession()
						.setAttribute("Users", user);// 将更新用户存入session
				setsCode("1");
			} catch (Exception e) {
				setsCode("0");
			}
		} else
			setsCode("0");

		return "success";
	}

	/**
	 * 功能：#获取当前用户
	 * 
	 * @return
	 */
	@Deprecated
	public String giveCurrentUser() {

		json = giveJsonInstance();

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");
		Users user = object != null ? (Users) object : null;
		json.put("User", user);

		return "success";
	}

	/**
	 * 功能：获取当前用户支付宝信息
	 * 
	 * @return
	 */
	public String giveCurrentUserAlipay() {

		json = giveJsonInstance();

		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;

		if (user != null) {
			json.put("Alipay", user.getUseAlipay());
			json.put("Name", user.getUseName());
		}

		return "success";
	}

	/**
	 * 功能：改变账号保护状态
	 * 
	 * @return
	 */
	public String changeProtect() {

		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;

		if (user != null) {
			if (user.getUseIsprotect().intValue() == 0) // 关闭状态
				user.setUseIsprotect(1);
			else
				user.setUseIsprotect(0);
			giveDaoInstance().update(user);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", user);// 将更新用户存入session
			setsCode("1");
		} else
			setsCode("0");

		return "success";
	}

	/**
	 * 功能：改变隐身状态（其他用户是否可见）
	 * 
	 * @return
	 */
	public String changeShowSign() {

		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;

		if (user != null) {
			if (user.getUseShowsign().intValue() == 0) // 关闭状态
				user.setUseShowsign(1);
			else
				user.setUseShowsign(0);
			giveDaoInstance().update(user);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", user);// 将更新用户存入session
			setsCode("1");
		} else
			setsCode("0");

		return "success";
	}

	/**
	 * 功能：#用户点赞
	 * 
	 * @return
	 */
	@Deprecated
	public String dianZan() {

		Object object = giveDaoInstance().getObjectById(Users.class, useId);
		Users user = object != null ? (Users) object : null;

		if (user != null && user.getUseId() > 0 && user.getUseIscompany() != 2) {
			user.setUseMoods(user.getUseMoods() + 1);
			giveDaoInstance().update(user);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", user);// 将更新用户存入session
			setsCode("1");// 点赞成功
		} else
			setsCode("0");// 点赞失败

		return "success";
	}

	/**
	 * 功能：根据用户ID获取用户
	 * 
	 * @return
	 */
	public String giveUserById() {

		json = giveJsonInstance();

		Object object = giveDaoInstance().getObjectById(Users.class, useId);
		Users user = object != null ? (Users) object : null;

		if (user.getUseShowsign() == 1 && user.getUseIscompany() != 2)// 用户没有隐身，且是激活状态
			json.put("User", user);

		return "success";
	}

	/**
	 * 功能：#搜索用户
	 * 
	 * @return
	 */
	@Deprecated
	public String giveUserByCond() {

		json = giveJsonInstance();

		String cond = "where useShowsign==1 and (useNickname=" + useCond
				+ " or useName=" + useCond + " or useSno=" + useCond
				+ " or usePhone=" + useCond + ")";

		List<?> list = giveDaoInstance().getObjectListBycond("Users", cond);

		List<Users> users = new ArrayList<Users>();
		for (Object object : list)
			users.add((Users) object);
		json.put("UserList", users);

		return "success";
	}

	/**
	 * 功能：查看余额
	 * 
	 * @return
	 */
	public String giveUseRemain() {

		json = giveJsonInstance();

		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;

		// 从数据库拉取user从而获取余额信息，而不直接使用session中的user，是因为二级缓存原因导致session中user与数据库user不能实时同步
		Users dbuser = null;
		if (user != null) {
			dbuser = (Users) giveDaoInstance().getObjectById(Users.class,
					user.getUseId());
		}

		if (dbuser != null)
			json.put("UseRemain", dbuser.getUseRemain());

		ServletActionContext.getRequest().getSession()
				.setAttribute("Users", dbuser);// 将数据库最新User放入session

		return "success";
	}

	/**
	 * 功能：提现
	 * 
	 * @return
	 */
	public String giveMoney() {

		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;

		// 同上，操作数据库user，而非session中user
		Users dbuser = null;
		if (user != null)
			dbuser = (Users) giveDaoInstance().getObjectById(Users.class,
					user.getUseId());

		if (dbuser != null && dbuser.getUseRemain() >= payNum) {// 余额充足，提现
			dbuser.setUseRemain(dbuser.getUseRemain() - payNum);
			giveDaoInstance().update(dbuser);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", dbuser);// 将更新用户存入session

			/** 提现 */
			Money money = new Money();
			money.setMonAlipay(dbuser.getUseAlipay());
			money.setMonComment("/");
			money.setMonName(dbuser.getUseName());
			money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date())
					+ dbuser.getUseSno().substring(
							dbuser.getUseSno().length() - 4));
			if (payNum < 10) {// 少于10元，自己出支付宝服务费
				payNum--;
			}
			money.setMonPay(payNum);// 不低于10元，平台帮出服务费
			money.setMonState(0);// 提现
			money.setMonPhone(dbuser.getUsePhone());
			money.setMonType("【用户提现】余额");
			money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
					.format(new Date()));
			giveDaoInstance().save(money);
			/** 提现结束 */

			setsCode("1");
		} else
			setsCode("0");
		return "success";
	}

	/**
	 * 功能：根据任务查找接受者
	 * 
	 * @return
	 */
	public String giveBeUsersByTasId() {

		json = giveJsonInstance();

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null) {
			Set<Apply> set = task.getTasApplies();// 接受者用户们
			json.put("UserSet", set);
		}

		return "success";
	}

	// //////////////////////////////////////////////////////////////////////////

	/* setter、getter方法 */
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

	public void setPayNum(double payNum) {
		this.payNum = payNum;
	}

	public void setUseAge(Integer useAge) {
		this.useAge = useAge;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}
