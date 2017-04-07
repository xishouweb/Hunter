package net.navagraha.hunter.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.ServletActionContext;

import net.navagraha.hunter.dao.ObjectDaoImpl;
import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.tool.JoinPushTool;
import net.navagraha.hunter.tool.MyHunterException;
import net.navagraha.hunter.tool.PhoneCodeTool;
import net.navagraha.hunter.tool.PropertyUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * 功能描述：任务Action
 * 
 * @author 冉椿林
 *
 */
public final class TaskAction extends AbstractObjAction {

	// 与pojo属性对应，故命名保持一致
	private Integer tasId;
	private String tasTitle;// 任务名
	private String tasContact;// 联系方式
	private String tasContent;// 内容
	private Double tasPrice;// 赏金
	private String tasType;// 任务类型
	private String tasState;// 任务状态
	private String tasTimeout;// 超时时间
	private Integer tasRulenum;// 规定人数
	private String tasEvaluate;// 评价
	private Integer tasCredit;// 任务评分

	private static PropertyUtil propertyUtil = new PropertyUtil("cons.properties");// 初始化参数配置文件
	private static int HOME_PerPageRow;// 任务大厅每页显示任务数量
	private static int LOG_PerPageRow;// 任务日志每页显示任务数量
	private static int PERSON_PerPageRow;// 任务发布每页显示任务数量
	private static double SUCCESS_TAX;// 成功服务费
	private static double FALSE_TAX;// 失败服务费
	private static int RuleReceive;// 每人限制任务数
	// 未实现发布接受规定任务数得奖励功能
	// private static int RecMoney;
	// private static int PubMoney;

	static {
		try {
			HOME_PerPageRow = Integer.parseInt(propertyUtil.getPropertyValue("HOME_PerPageRow"));
			LOG_PerPageRow = Integer.parseInt(propertyUtil.getPropertyValue("LOG_PerPageRow"));
			PERSON_PerPageRow = Integer.parseInt(propertyUtil.getPropertyValue("PERSON_PerPageRow"));
			RuleReceive = Integer.parseInt(propertyUtil.getPropertyValue("RuleReceive"));
			SUCCESS_TAX = Double.parseDouble(propertyUtil.getPropertyValue("SUCCESS_TAX"));
			FALSE_TAX = Double.parseDouble(propertyUtil.getPropertyValue("FALSE_TAX"));
		} catch (NumberFormatException e) {
			System.err.println("异常来自项目Hunter:\n" + e.getMessage());
		}
		// RecMoney
		// =Integer.parseInt(propertyUtil.getPropertyValue("RecMoney"));//接受任务数奖励
		// PubMoney
		// =Integer.parseInt(propertyUtil.getPropertyValue("PubMoney"));//发布任务数奖励
	}

	// 前台传入
	private Integer curPage;// 当前页
	private Integer appId;// 申请ID
	private String appReason;// 申请理由

	// 反馈到前台
	public String pushPoint;// 推送节点

	/**
	 * 功能：判断是否存在支付宝账户
	 * 
	 * @return
	 */
	public String hasAlipay() {
		Object object = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;
		if (user != null) {
			if ("".equals(user.getUseAlipay()) || user.getUseAlipay() == null || user.getUseName() == null
					|| "".equals(user.getUseName())) {// 判断支付宝账户是否为空,equals比较时：字符串放前面，变量放后面避免空指针异常
				setsCode("21");
				return "success";
			}
		}
		setsCode("1");

		return "success";
	}

	/**
	 * 功能：发布任务
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String publishTask() {

		Object object = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		Task task = null;

		if (user != null) {// 新建任务并初始化
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间

			task = new Task();
			task.setTasTitle(tasTitle);
			task.setTasPrice(tasPrice);
			task.setTasType(tasType);
			task.setTasState(0);// 任务发布状态
			task.setTasContact(tasContact);
			task.setTasContent(tasContent);
			Date date = new Date();
			task.setTasTime(sdf.format(date));

			Object str = ServletActionContext.getRequest().getSession().getAttribute("ImgPath");// 从session上获取刚刚上传图片的路径
			if (str != null) {// 不为空
				task.setTasImg(str.toString());
				ServletActionContext.getRequest().getSession().setAttribute("ImgPath", null);// 重置图片路径
			}
			if ("加急个人".equals(tasType)) {// 加急个人任务
				tasTimeout = tasTimeout.split(":")[0].length() < 2 ? "0" + tasTimeout : tasTimeout;// 解决某些机型使用时间控件传入的小时为12小时制
				task.setTasTimeout(sdf.format(date).substring(0, 11) + tasTimeout + ":00");// 重组时间并添加秒
			}
			if ("个人".equals(tasType)) {// 个人任务
				date.setHours(date.getHours() + 48);// 默认显示2天后失效
				task.setTasTimeout(sdf.format(date));
			}
			if ("团队".equals(tasType)) {// 团队任务
				date.setHours(date.getHours() + 48);// 默认显示2天后失效
				task.setTasTimeout(sdf.format(date));
				task.setTasRulenum(tasRulenum);// 限定人数
				task.setTasReceivenum(0);
				task.setTasFinishnum(0);
			}
		} else {
			setsCode("0");
			return "success";
		}

		if (task != null) {
			user.setUsePublishnum(user.getUsePublishnum() + 1);// 用户发布任务数+1

			/** 付钱记录 */
			Money money = new Money();
			money.setMonAlipay(user.getUseAlipay());// 设置资金对象支付宝

			money.setMonName(user.getUseName());// 设置资金对象
			money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
					+ user.getUseSno().substring(user.getUseSno().length() - 4));// 为保证流水号不重复，使用时间戳+学号
			money.setMonPay(tasPrice);// 设置赏金
			money.setMonState(4);// 付钱（不显示）
			money.setMonPhone(user.getUsePhone());// 设置联系方式
			money.setMonComment("/");// 设置备注为“/”
			money.setMonTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// 设置时间

			/* 目前统一使用支付宝支付 */
			// if (payType != null && payType == 0) {// 余额支付
			// user.setUseRemain(user.getUseRemain() - tasPrice);// 设置余额
			// money.setMonType("【任务发布】悬赏金(平台余额支付)");
			// } else
			money.setMonType("【任务发布】悬赏金(支付宝支付)");
			giveDaoInstance().save(money);// 持久化对象
			/** 付钱记录结束 */

			task.setTasUser(user);
			giveDaoInstance().update(user);// 更新用户
			ServletActionContext.getRequest().getSession().setAttribute("Users", user);// 将更新用户存入session
			giveDaoInstance().save(task);// 持久化任务
			setsCode("1");
		}

		return "success";
	}

	/**
	 * 功能：用户接受任务
	 * 
	 * @return
	 * @throws MyHunterException
	 */
	public String receiveTask() throws MyHunterException {

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;// 拿取数据

		Object object1 = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		if (task != null && user != null) {// 非空判断

			if ("".equals(user.getUseAlipay()) || user.getUseAlipay() == null) {// 先完善支付宝账户
				setsCode("21");
				return "success";
			}

			for (Apply apply : task.getTasApplies()) {// set集合使用foreach遍历，set本身取值也是iterator，无法使用for遍历提升效率
				if (apply.getAppBeUser().getUseId().intValue() == user.getUseId().intValue()) {// 已申请,不必重复申请
					setsCode("19");
					return "success";
				}
			}

			// 申请与进行的任务已达到上限
			String sCond = "where appBeUser=" + user.getUseId() + " and appState in(0,1) order by appId desc";
			int iSize = giveDaoInstance().getObjectSizeBycond("select count(*) from Apply " + sCond);// 已接受任务人数
			if (iSize > RuleReceive) {
				setsCode("24");// 申请+进行任务数已达上限
				return "success";
			}

			// 记录tag
			List<?> list = giveDaoInstance().getObjectListByfield("Tag", "tagUser", user);
			Tag tag = list.size() > 0 && list.get(0) instanceof Tag ? (Tag) list.get(0) : null;

			if (tag != null) {
				// 设置接收某性别的任务发布者任务次数
				String strs[] = tag.getTagSex().split(",");
				int iA, iB;
				try {
					iA = Integer.parseInt(strs[0]);
					iB = Integer.parseInt(strs[1]);
				} catch (NumberFormatException e2) {
					throw new MyHunterException(e2.getMessage());
				}
				if ("男".equals(task.getTasUser().getUseSex()))// 任务发布者为男性，接受男性任务次数+1，下面同理
					iA++;
				if ("女".equals(task.getTasUser().getUseSex()))
					iB++;
				tag.setTagSex(iA + "," + iB);
				// 设置接收任务类型
				strs = tag.getTagTasktype().split(",");
				int iC, iD, iE;
				try {
					iC = Integer.parseInt(strs[0]);
					iD = Integer.parseInt(strs[1]);
					iE = Integer.parseInt(strs[2]);
				} catch (NumberFormatException e2) {
					throw new MyHunterException(e2.getMessage());
				}
				if ("加急个人".equals(task.getTasType()))
					iC++;
				if ("个人".equals(task.getTasType()))
					iD++;
				if ("团队".equals(task.getTasType()))
					iE++;
				tag.setTagTasktype(iC + "," + iD + "," + iE);
				giveDaoInstance().update(tag);// 更新标签
			}
			// 记录tag结束

			Apply apply = new Apply();// 新建一个申请
			apply.setAppBeUser(user);
			apply.setAppTask(task);
			apply.setAppTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

			if ("加急个人".equals(task.getTasType())) {// 加急任务
				if (task.getTasState() == 0) {// 任务可接

					apply.setAppReason("加急任务，自动申请通过");
					apply.setAppState(1);
					task.setTasState(2);// 任务进行中
					giveDaoInstance().save(apply);// 保存申请

					// 将申请放入该任务的申请set中
					Set<Apply> set = new HashSet<Apply>(0);
					set.add(apply);
					task.setTasApplies(set);
					giveDaoInstance().update(task);// 更新任务
					setsCode("1");

					// 消息推送到接受者
					String sPhone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))// 如果接受者存在于与服务器的连接中
						JoinPushTool.broadcast("12" + task.getTasTitle(), sPhone);// 进行推送

					// 消息推送到发布者
					sPhone = task.getTasUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))// 如果发布者存在于与服务器的连接中
						JoinPushTool.broadcast("01" + task.getTasTitle(), sPhone);

					return "success";
				} else {
					setsCode("10");// 任务不可接
					return "success";
				}

			} else {// 不是加急任务，需要审核
				if (task.getTasState() == 0 || task.getTasState() == 1) {
					apply.setAppReason(appReason);// 设置申请理由
					apply.setAppState(0);// 设置为申请中状态
					giveDaoInstance().save(apply);// 保存申请

					task.setTasState(1);// 设置为任务申请中
					giveDaoInstance().update(task);
					setsCode("1");

					// 消息推送到接受者
					String sPhone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))// 如果接受者存在于与服务器的连接中
						JoinPushTool.broadcast("10" + task.getTasTitle(), sPhone);

					// 消息推送到发布者
					sPhone = task.getTasUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))// 如果发布者存在于与服务器的连接中
						JoinPushTool.broadcast("00" + task.getTasTitle(), sPhone);

					return "success";
				} else {
					setsCode("10");// 任务不可接
					return "success";
				}
			}
		} else {
			setsCode("9");// 任务不存在
			return "success";
		}
	}

	/**
	 * 功能：取消申请任务(仅限申请中任务，即appState=0)
	 * 
	 * @return
	 */
	public String backReceiveTask() {

		Object object = giveDaoInstance().getObjectById(Apply.class, appId);// 取得申请
		Apply apply = object != null && object instanceof Apply ? (Apply) object : null;
		if (apply != null && apply.getAppState() == 0) {// 是否处于可取消状态
			giveDaoInstance().delete(apply);// 删除申请
			setsCode("1");// 操作成功
		} else
			setsCode("23");// 任务已进入进行状态，不能取消

		return "success";

	}

	/**
	 * 功能：取消发布任务（仅限发布的任务，即tasState=0,注：传入的任务应该tasState=0,1,2）
	 * 
	 * @return
	 */
	public String backTask() {
		Object object = giveDaoInstance().getObjectById(Task.class, tasId);// 得到任务
		Task task = object != null && object instanceof Task ? (Task) object : null;

		if (task != null && task.getTasState() == 0) {// 任务属于发布状态（可取消状态）

			task.setTasState(5);// 任务失败
			task.setTasEvaluate("用户撤销任务，导致任务失败");
			giveDaoInstance().update(task);

			// 将发布任务的赏金扣取服务费后存入余额
			Users user = task.getTasUser();
			user.setUseRemain(user.getUseRemain() + task.getTasPrice() * (1 - FALSE_TAX));// 存入余额
			giveDaoInstance().update(user);
			// 将更新用户存入session
			ServletActionContext.getRequest().getSession().setAttribute("Users", user);

			/** 返钱记录 */
			Money money = new Money();
			money.setMonAlipay(user.getUseAlipay());
			money.setMonComment("/");
			money.setMonName(user.getUseName());
			money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
					+ user.getUseSno().substring(user.getUseSno().length() - 4));
			money.setMonPay(task.getTasPrice() * (1 - FALSE_TAX));// 需打款：扣取服务费的赏金
			money.setMonState(3);// 打钱（不显示）
			money.setMonPhone(user.getUsePhone());
			money.setMonType("【撤销任务】返金");
			money.setMonTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			giveDaoInstance().save(money);
			/** 返钱记录结束 */

			/** 支付日志 **/
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			Pay payOut;

			// 获取任务发布者的pay
			List<?> list1 = giveDaoInstance().getObjectListByfield("Pay", new String[] { "payTime", "payUser" },
					new Object[] { sdf.format(new Date()), user });// 根据条件查询对象，String数组对应数据库字段，Object数组对应检索条件

			if (list1.size() > 0) {// 存在当月记录
				// 支出
				payOut = (Pay) list1.get(0);
				payOut.setPayOut(payOut.getPayOut() + task.getTasPrice() * FALSE_TAX);// 支出+该次任务发布的服务费
			} else {// 不存在当月记录，新建并初始化
				// 支出
				payOut = new Pay();
				payOut.setPayTime(sdf.format(new Date()));
				payOut.setPayIn(0.0);
				payOut.setPayOut(task.getTasPrice() * FALSE_TAX);
				payOut.setPayUser(task.getTasUser());
			}
			giveDaoInstance().saveOrUpdate(payOut);
			/** 支付日志结束 **/

			// 消息推送到发布者
			String sPhone = task.getTasUser().getUsePhone();
			if (JoinPushTool.getConnections().containsKey(sPhone))// 如果发布者存在于与服务器的连接中
				JoinPushTool.broadcast("04" + task.getTasTitle(), sPhone);

			setsCode("1");// 操作成功
		} else
			setsCode("22");// 任务已过发布和申请状态，不能撤销

		return "success";
	}

	/**
	 * 功能：用户审核通过申请(非加急任务)
	 * 
	 * @return
	 */
	public String checkSuccessTask() {
		if (appId == null) {
			setsCode("13");// 申请不存在
			return "success";
		}
		Object object = giveDaoInstance().getObjectById(Apply.class, appId);// 得到申请
		Apply apply = object != null && object instanceof Apply ? (Apply) object : null;

		if (apply != null) {
			Task task = apply.getAppTask();// 得到申请的任务
			if (task != null && task.getTasState() == 1) {// 任务处于被申请状态

				// 让本申请通过
				Users user = apply.getAppBeUser();
				user.setUseAcceptnum(user.getUseAcceptnum() + 1);// 接受者任务接受数+1
				giveDaoInstance().update(user);
				ServletActionContext.getRequest().getSession().setAttribute("Users", user);// 将更新用户存入session

				if ("个人".equals(task.getTasType())) {// 个人任务

					// 其他申请自动设置为不通过
					List<?> appList = giveDaoInstance().getObjectListByfield("Apply", "appTask", task);
					for (int i = 0, iSize = appList.size(); i < iSize; i++) {
						Object object2 = appList.get(i);
						Apply apply2 = (Apply) object2;
						if (apply2.getAppId().intValue() != appId) {// 不是需要通过的appId,不通过,使用intValue方法拆箱后比较，integer比较int只支持-128到127内
							apply2.setAppState(2);
							giveDaoInstance().update(apply2);

							// 不通过消息推送到接受者
							String sPhone = apply2.getAppBeUser().getUsePhone();
							if (JoinPushTool.getConnections().containsKey(sPhone))
								JoinPushTool.broadcast("11" + task.getTasTitle(), sPhone);
						} else {// 是需要通过的appId,通过并消息推送到接受者
							if (!JoinPushTool.getConnections().containsKey(apply2.getAppBeUser().getUsePhone())) // 用户不在线，发送短信
								PhoneCodeTool.send(apply2.getAppBeUser().getUsePhone(), task.getTasTitle(), "apply");
							else {// 用户在线，消息推送通知
								String sPhone = apply2.getAppBeUser().getUsePhone();
								JoinPushTool.broadcast("12" + task.getTasTitle(), sPhone);
							}
						}
					}

					apply.setAppState(1);// 通过
					giveDaoInstance().update(apply);

					task.setTasState(2);// 审核成功,设置任务为进行中状态
					Set<Apply> set = new HashSet<Apply>(0);
					set.add(apply);
					task.setTasApplies(set);
					giveDaoInstance().update(task);
					setsCode("1");// 操作成功

					// 消息推送发布者
					String sPhone = task.getTasUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))
						JoinPushTool.broadcast("01" + task.getTasTitle(), sPhone);

					return "success";
				} else {// 团队任务
					if (task.getTasReceivenum().intValue() == task.getTasRulenum().intValue() - 1) {// 任务人数已达齐，开始进行
						apply.setAppState(1);// 通过
						giveDaoInstance().update(apply);

						task.setTasState(2);// 审核成功,任务进行中
						// 将该申请添加到任务申请set中
						Set<Apply> set = task.getTasApplies();// 已通过的申请
						set.add(apply);
						task.setTasApplies(set);
						task.setTasReceivenum(task.getTasReceivenum() + 1);
						giveDaoInstance().update(task);

						// 其他申请自动设置为不通过
						List<?> appList = giveDaoInstance().getObjectListByfield("Apply", "appTask", task);// 获取所有的申请
						for (int i = 0, iSize = appList.size(); i < iSize; i++) {
							Object object2 = appList.get(i);
							Apply apply2 = (Apply) object2;
							boolean bIn = false;
							for (Apply apply3 : set) {// 遍历已通过的申请
								if (apply2.getAppId().intValue() == apply3.getAppId().intValue())
									bIn = true;// 该申请是否已经存在于里面，标志位为TRUE

								if (!bIn) {// 不通过的申请
									apply2.setAppState(2);
									giveDaoInstance().update(apply2);

									// 不通过 消息推送接受者
									String sPhone = apply2.getAppBeUser().getUsePhone();
									if (JoinPushTool.getConnections().containsKey(sPhone))
										JoinPushTool.broadcast("11" + task.getTasTitle(), sPhone);
								} else {// 通过的的申请
									if (!JoinPushTool.getConnections().containsKey(apply2.getAppBeUser().getUsePhone())) // 用户不在线，发送短信
										PhoneCodeTool.send(apply2.getAppBeUser().getUsePhone(), task.getTasTitle(),
												"apply");
									else {// 用户在线，消息推送
										String sPhone = apply2.getAppBeUser().getUsePhone();
										JoinPushTool.broadcast("12" + task.getTasTitle(), sPhone);
									}
								}
							}
						}
						setsCode("1");// 操作成功

						// 消息推送发布者
						String sPhone = task.getTasUser().getUsePhone();
						if (JoinPushTool.getConnections().containsKey(sPhone))
							JoinPushTool.broadcast("01" + task.getTasTitle(), sPhone);

						return "success";
					} else if (task.getTasReceivenum().intValue() < task.getTasRulenum().intValue() - 1) {// 任务人数未达齐
						apply.setAppState(1);// 通过
						giveDaoInstance().update(apply);

						task.setTasState(1);// 审核成功,任务未进行
						Set<Apply> set = task.getTasApplies();
						set = set == null ? new HashSet<Apply>(0) : set;// set不存在则新建set
						set.add(apply);
						task.setTasReceivenum(task.getTasReceivenum() + 1);
						task.setTasApplies(set);
						giveDaoInstance().update(task);
						setsCode("1");// 操作成功

						// 消息推送接受者 任务未开始，只通知本申请的接受者
						String sPhone = apply.getAppBeUser().getUsePhone();
						if (JoinPushTool.getConnections().containsKey(sPhone))
							JoinPushTool.broadcast("10" + task.getTasTitle(), sPhone);

						return "success";
					} else {
						setsCode("12");// 规定人数已满
						return "success";
					}
				}
			} else {
				setsCode("9");// 任务不存在
				return "success";
			}
		} else {
			setsCode("13");// 申请不存在
			return "success";
		}
	}

	/**
	 * 功能：用户审核不通过申请(非加急任务)
	 * 
	 * @return
	 */
	public String checkFalseTask() {

		Object object = giveDaoInstance().getObjectById(Apply.class, appId);// 获取申请
		Apply apply = object != null && object instanceof Apply ? (Apply) object : null;
		if (apply != null) {

			Task task = apply.getAppTask();// 获取任务

			if (task != null && task.getTasState() == 1) {// 如果任务为被申请状态
				apply.setAppState(2);// 设置申请为不通过状态
				giveDaoInstance().update(apply);
				setsCode("1");

				// 消息推送到接受者
				String sPhone = apply.getAppBeUser().getUsePhone();
				if (JoinPushTool.getConnections().containsKey(sPhone))
					JoinPushTool.broadcast("11" + task.getTasTitle(), sPhone);

				return "success";
			} else {
				setsCode("9");// 任务不存在
				return "success";
			}
		} else {
			setsCode("13");// 申请记录不存在
			return "success";
		}
	}

	/**
	 * 功能：用户要完成任务
	 * 
	 * @return
	 */
	public String finishTask() {

		Object object1 = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;

		if (task != null && user != null) {

			if (task.getTasState() == 5) {// 判断任务是否已处于失败状态
				setsCode("15");// 任务已失败
				return "success";
			}
			if (task.getTasState() == 3) {// 判断任务是否已被申请完成
				setsCode("14");// 已点击了申请完成，不必重新点击申请完成
				return "success";
			}

			if (!"团队".equals(task.getTasType())) {// 个人任务

				for (Apply apply : task.getTasApplies()) {// 申请set中肯定只有一个申请（因为是个人任务）
					if (apply.getAppBeUser().getUseId().intValue() == user.getUseId().intValue()) {// 验证登录用户是否是该任务的申请者
						if (apply.getAppState().intValue() == 5) {// “申请”已被申请完成
							setsCode("14");// 已点击了申请完成，不必重新点击申请完成
							return "success";
						} else {// 设置“申请”被申请完成
							apply.setAppState(5);
							giveDaoInstance().update(apply);// 设置该用户申请已提交（避免重复提交）
							break;
						}

					}
				}

				task.setTasFinishtime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				task.setTasState(3);// 设置任务为提交审核状态
				giveDaoInstance().update(task);
				setsCode("1");// 操作成功

				// 消息推送到接受者
				String sPhone = user.getUsePhone();
				if (JoinPushTool.getConnections().containsKey(sPhone))
					JoinPushTool.broadcast("13" + task.getTasTitle(), sPhone);

				// 消息推送到发布者
				sPhone = task.getTasUser().getUsePhone();
				if (JoinPushTool.getConnections().containsKey(sPhone))
					JoinPushTool.broadcast("02" + task.getTasTitle(), sPhone);
				else
					PhoneCodeTool.send(sPhone, task.getTasTitle(), "task");// 短信提示发布者任务已完成

				return "success";

			} else {// 团队任务

				for (Apply apply : task.getTasApplies()) {// 申请set中可能有多个申请
					if (apply.getAppBeUser().getUseId().intValue() == user.getUseId().intValue()) {// 验证登录用户是否是该任务的申请者之一
						if (apply.getAppState().intValue() == 5) {
							setsCode("14");// 已点击了申请完成，不必重新点击申请完成
							return "success";
						} else {
							apply.setAppState(5);
							giveDaoInstance().update(apply);// 设置该用户申请已提交（避免重复提交）
							break;
						}

					}
				}

				if (task.getTasFinishnum().intValue() == task.getTasRulenum().intValue() - 1) {// 任务人数已达齐，开始申请审核

					task.setTasFinishtime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					task.setTasFinishnum(task.getTasRulenum());
					task.setTasState(3);// 提交审核
					giveDaoInstance().update(task);
					setsCode("1");// 操作成功

					// 消息推送到发布者
					String sPhone = task.getTasUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))
						JoinPushTool.broadcast("02" + task.getTasTitle(), sPhone);
					else
						PhoneCodeTool.send(task.getTasUser().getUsePhone(), task.getTasTitle(), "task");// 短信提示发布者任务已完成

					return "success";
				} else {// 任务人数未达齐
					task.setTasFinishnum(task.getTasFinishnum() + 1);
					task.setTasState(2);// 任务仍是进行中
					giveDaoInstance().update(task);
					setsCode("1");// 操作成功

					// 消息推送到接受者 仅仅是该操作完成者
					String sPhone = user.getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))
						JoinPushTool.broadcast("13" + task.getTasTitle(), sPhone);

					return "success";
				}
			}
		} else {
			setsCode("9");// 任务不存在
			return "success";
		}

	}

	/**
	 * 功能：用户允许通过任务
	 * 
	 * @return
	 */
	public String successTask() {

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;

		if (task != null && task.getTasState() == 3) {
			Set<Apply> set = task.getTasApplies();

			if (set.size() > 0) {// 存在申请

				/** 支付日志 **/
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				// 获取任务发布者的pay
				Pay payOut;
				List<?> list1 = giveDaoInstance().getObjectListByfield("Pay", new String[] { "payTime", "payUser" },
						new Object[] { sdf.format(new Date()), task.getTasUser() });

				if (list1.size() > 0 && list1.get(0) instanceof Pay) {// 存在当月支付日志
					// 支出
					payOut = (Pay) list1.get(0);
					payOut.setPayOut(payOut.getPayOut() + task.getTasPrice() + 0.0);
				} else {// 不存在当月支付日志
					// 支出
					payOut = new Pay();
					payOut.setPayTime(sdf.format(new Date()));
					payOut.setPayIn(0.0);
					payOut.setPayOut(task.getTasPrice() + 0.0);
					payOut.setPayUser(task.getTasUser());
				}
				giveDaoInstance().saveOrUpdate(payOut);
				/** 支付日志结束 **/

				for (Apply apply : set) {

					if (apply.getAppState() == 1 || apply.getAppState() == 5) {// 任务为可完成状态
						apply.setAppState(3);// 任务成功
						giveDaoInstance().update(apply);

						Users user = apply.getAppBeUser();
						user.setUseRemain(user.getUseRemain() + task.getTasPrice() * (1 - SUCCESS_TAX) / set.size());// 存入余额
						giveDaoInstance().update(user);
						ServletActionContext.getRequest().getSession().setAttribute("Users", user);// 将更新用户存入session

						/** 打钱记录 */
						Money money = new Money();
						money.setMonAlipay(user.getUseAlipay());
						money.setMonComment("/");
						money.setMonName(user.getUseName());
						money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
								+ user.getUseSno().substring(user.getUseSno().length() - 4));
						money.setMonPay(task.getTasPrice() * (1 - SUCCESS_TAX) / set.size());
						money.setMonState(3);// 打钱（不显示）
						money.setMonPhone(user.getUsePhone());
						money.setMonType("【任务完成】赏金");
						money.setMonTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
						giveDaoInstance().save(money);
						/** 打钱记录结束 */

						/** 能力 **/
						List<?> list3 = giveDaoInstance().getObjectListByfield("Power", "powUser", user);// 获取任务接收者的power
						Power power;
						if (tasCredit != null && list3.size() > 0) {// 含有power数据
							power = (Power) list3.get(0);
							if (power.getPowCredit() + tasCredit < 790)// 如果信誉值超过790直接设置为满级
								power.setPowCredit(power.getPowCredit() + tasCredit);
							else
								power.setPowCredit(790);// 满级
						} else {// 否则新建并初始化
							power = new Power();
							power.setPowCredit(50 + tasCredit);
							power.setPowUser(user);
							power.setPowFast(0);
						}

						if (tasCredit > 3) {// 如果评分大于三分，表示该次服务为优秀服务，将对完成者增加效率分
							if (power.getPowFast() < 299)
								power.setPowFast(power.getPowFast() + 1);
							else
								power.setPowFast(300);// 满级
						}
						giveDaoInstance().saveOrUpdate(power);
						/** 能力结束 **/

						/** 支付日志 **/
						Pay payIn;
						List<?> list2 = giveDaoInstance().getObjectListByfield("Pay",
								new String[] { "payTime", "payUser" }, new Object[] { sdf.format(new Date()), user });// 获取任务收入者的pay

						if (list2.size() > 0) {// 当月记录不为空
							// 收入
							payIn = (Pay) list2.get(0);
							if ("团队".equals(task.getTasType()))// 团队任务，设置收入时应加上任务赏金除以人数取平均
								payIn.setPayIn(payIn.getPayIn()
										+ task.getTasPrice() * (1 - SUCCESS_TAX) / task.getTasRulenum());
							else
								payIn.setPayIn(payIn.getPayIn()// 否则直接设置加上任务赏金
										+ task.getTasPrice() * (1 - SUCCESS_TAX));
						} else {// 当月记录为空
							// 收入
							payIn = new Pay();
							payIn.setPayTime(sdf.format(new Date()));
							payIn.setPayOut(0.0);
							if (task.getTasType().equals("团队")) {
								payIn.setPayIn(task.getTasPrice() * (1 - SUCCESS_TAX) / task.getTasRulenum());
							} else
								payIn.setPayIn(task.getTasPrice() * (1 - SUCCESS_TAX));
							payIn.setPayUser(user);
						}
						giveDaoInstance().saveOrUpdate(payIn);
						/** 支付日志结束 **/

					}

					// 消息推送到接受者
					String sPhone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))
						JoinPushTool.broadcast("14" + task.getTasTitle(), sPhone);
				}
				task.setTasState(4);// 设置任务为成功状态
				task.setTasEvaluate(tasEvaluate);// 设置任务评价
				task.setTasCredit(tasCredit);// 设置任务评分
				giveDaoInstance().update(task);
				setsCode("1");// 操作成功

				// 消息推送发布者
				String sPhone = task.getTasUser().getUsePhone();
				if (JoinPushTool.getConnections().containsKey(sPhone))
					JoinPushTool.broadcast("03" + task.getTasTitle(), sPhone);

				return "success";
			} else {
				setsCode("16");// 接受者们不存在
				return "success";
			}
		} else {
			setsCode("9");// 任务不存在
			return "success";
		}
	}

	/**
	 * 功能：用户不允许通过任务
	 * 
	 * @return
	 */
	public String falseTask() {

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;

		if (task != null && task.getTasState() == 3) {// 任务为审核状态
			Set<Apply> set = task.getTasApplies();

			if (set.size() > 0) {// 申请不为空

				/** 能力 **/
				for (Apply apply : set) {

					if (apply.getAppState() == 1 || apply.getAppState() == 5) {// 任务为可改变为失败状态
						apply.setAppState(4);// 任务失败
						giveDaoInstance().update(apply);

						Users beUser = apply.getAppBeUser();
						// 获取任务接收者的power
						List<?> list3 = giveDaoInstance().getObjectListByfield("Power", "powUser", beUser);
						Power power;
						if (list3.size() > 0) {
							power = (Power) list3.get(0);
							power.setPowCredit(power.getPowCredit() - 4);// 失败任务减4分
						} else {
							power = new Power();
							power.setPowUser(beUser);
							power.setPowCredit(50 - 4);// 失败任务减4分
							power.setPowFast(0);
						}
						giveDaoInstance().saveOrUpdate(power);

					}

					// 消息推送到接受者
					String sPhone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.getConnections().containsKey(sPhone))
						JoinPushTool.broadcast("15" + task.getTasTitle(), sPhone);
				}
				/** 能力结束 **/

				// 消息推送到发布者
				String sPhone = task.getTasUser().getUsePhone();
				if (JoinPushTool.getConnections().containsKey(sPhone))
					JoinPushTool.broadcast("04" + task.getTasTitle(), sPhone);

				if (task.getTasState() == 6) {// 任务已失效，即失败
					task.setTasState(5);// 任务失败
					giveDaoInstance().update(task);
					Users user = task.getTasUser();
					user.setUseRemain(user.getUseRemain() + task.getTasPrice() * (1 - FALSE_TAX));// 存入余额
					giveDaoInstance().update(user);
					ServletActionContext.getRequest().getSession().setAttribute("Users", user);// 将更新用户存入session

					/** 返钱记录 */
					Money money = new Money();
					money.setMonAlipay(user.getUseAlipay());
					money.setMonComment("/");
					money.setMonName(user.getUseName());
					money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
							+ user.getUseSno().substring(user.getUseSno().length() - 4));
					money.setMonPay(task.getTasPrice() * (1 - FALSE_TAX));
					money.setMonState(3);// 打钱（不显示）
					money.setMonPhone(user.getUsePhone());
					money.setMonType("【任务无人接受】返金");
					money.setMonTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
					giveDaoInstance().save(money);
					/** 返钱记录结束 */

					/** 支付日志 **/
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					Pay payOut;

					// 获取任务发布者的pay
					List<?> list1 = giveDaoInstance().getObjectListByfield("Pay", new String[] { "payTime", "payUser" },
							new Object[] { sdf.format(new Date()), user });

					if (list1.size() > 0) {
						// 支出者
						payOut = (Pay) list1.get(0);
						payOut.setPayOut(payOut.getPayOut() + task.getTasPrice() * FALSE_TAX);
					} else {
						// 支出者
						payOut = new Pay();
						payOut.setPayTime(sdf.format(new Date()));
						payOut.setPayIn(0.0);
						payOut.setPayOut(task.getTasPrice() * FALSE_TAX);
						payOut.setPayUser(task.getTasUser());
					}
					giveDaoInstance().saveOrUpdate(payOut);
					/** 支付日志结束 **/

					setsCode("1");// 操作成功
					return "success";
				} else {// 任务未失效
					task.setTasState(0);// 任务已发布，继续可接
					task.setTasApplies(null);// 接任务人清空
					giveDaoInstance().update(task);

					setsCode("1");// 操作成功
					return "success";
				}
			} else {
				setsCode("16");// 接受者不存在
				return "success";
			}
		} else {
			setsCode("9");// 任务不存在
			return "success";
		}
	}

	/**
	 * 功能：根据发布类型获取经过发布时间倒序排序的所有任务(任务大厅分页)
	 * 
	 * @return
	 */
	public String giveTaskByType() {

		json = giveJsonInstance();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败
		List<?> list;
		list = giveDaoInstance().pageListWithCond("Task", curPage, HOME_PerPageRow,
				"where tasType='" + tasType + "' and  tasState in (0,1) order by tasTime desc");
		List<Task> taskList = new ArrayList<Task>();

		if (list.size() > 0 && list.get(0) instanceof Task) {// 如果集合不为空且为Task类型

			for (int i = 0, iSize = list.size(); i < iSize; i++) {
				Object object = list.get(i);
				Task task = (Task) object;// 任务实体为游离态，更改任务实体不会同步到数据库

				if (task.getTasUser().getUseId().intValue() == userId) // 任务发布者是自己
					task.setState("2");// 设置游离态任务实体为进行中状态,方便前台查阅自己任务是显示进行中即不可点击申请

				for (Apply apply : task.getTasApplies()) {

					if (apply.getAppBeUser().getUseId().intValue() == userId) {// 该任务申请者有本人
						task.setAppId(apply.getAppId());// 可不传
						if (apply.getAppState() == 0)
							task.setState("0");// 设置游离态任务实体为发布中状态
						if (apply.getAppState() == 1 && apply.getAppTask().getTasState() == 1)
							task.setState("1");// 设置游离态任务实体为申请中状态：“申请”已通过，但任务规定人数未达齐，仍是申请中状态（团队任务）
					}
				}
				taskList.add(task);
			}
		}

		// 去掉json多余参数
		JsonConfig jsonConfig = new JsonConfig(); // 建立配置文件
		jsonConfig.setIgnoreDefaultExcludes(false); // 设置默认忽略
		jsonConfig.setExcludes(new String[] { "tasApplies", "tasUser" }); // 只要将所需忽略字段加到数组中即可

		HashMap<String, List<?>> map = new HashMap<String, List<?>>();
		map.put("TaskList", taskList);
		json.putAll(map, jsonConfig);

		// 放入总页数
		if (curPage == 0) {
			int iSize;
			iSize = giveDaoInstance().getObjectSizeBycond(
					"select count(*) from Task where tasType='" + tasType + "' and  tasState in (0,1)");
			iSize = giveDaoInstance().getObjectSizeBycond(
					"select count(*) from Task where tasType='" + tasType + "' and  tasState in (0,1)");
			json.put("size", iSize);
		}
		return "success";
	}

	/**
	 * 功能：获取经过发布时间倒序排序的所有特殊任务（任务大厅分页-特殊）
	 * 
	 * @return
	 */
	public String giveSpecialTask() {

		json = giveJsonInstance();

		List<?> list0 = giveDaoInstance().getObjectListBycond("Users", "where useIscompany=1");

		List<?> list = giveDaoInstance().pageListWithCond("Task", curPage, HOME_PerPageRow,
				"where tasUser in(:list) and  tasState in (0,1) order by tasTime desc", list0);
		List<Task> taskList = new ArrayList<Task>();
		if (list.size() > 0 && list.get(0) instanceof Task) {
			for (Object object : list) {
				taskList.add((Task) object);
			}
		}

		// 去掉json多余参数
		JsonConfig jsonConfig = new JsonConfig(); // 建立配置文件
		jsonConfig.setIgnoreDefaultExcludes(false); // 设置默认忽略
		jsonConfig.setExcludes(new String[] { "tasApplies", "tasUser" }); // 只要将所需忽略字段加到数组中即可

		HashMap<String, List<?>> map = new HashMap<String, List<?>>();
		map.put("TaskList", taskList);
		json.putAll(map, jsonConfig);

		// 放入总页数
		if (curPage == 0) {
			int iSize = giveDaoInstance().getObjectSizeBycond(
					"select count(*) from Task where tasUser in(:list) and  tasState in (0,1)", list0);
			json.put("size", iSize);
		}

		return "success";
	}

	/**
	 * 功能： 根据任务状态获取经过申请时间倒序排序的所有任务(任务日志分页-接受的任务)
	 * 
	 * @return
	 */
	public String giveBeTaskByState() {

		json = giveJsonInstance();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		String sCond = "where appBeUser=" + userId + " and appState=" + tasState + " order by appId desc";

		List<?> list = giveDaoInstance().pageListWithCond("Apply", curPage, LOG_PerPageRow, sCond);// 取得申请

		List<Task> taskList = new ArrayList<Task>();
		if (list.size() > 0 && list.get(0) instanceof Apply) {// 如果集合不为空且为Task类型

			for (int i = 0, iSize = list.size(); i < iSize; i++) {// 遍历申请
				Object object = list.get(i);
				Apply apply = ((Apply) object);
				Task task = apply.getAppTask();
				task.setAppId(apply.getAppId());
				if (apply.getAppState() == 0)
					task.setState("0");// 设置游离态任务实体为发布中状态
				if (apply.getAppState() == 1 && apply.getAppTask().getTasState() == 1)
					task.setState("1");

				taskList.add(task);
			}
		}

		// 去掉json多余参数
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setIgnoreDefaultExcludes(false);
		jsonConfig.setExcludes(new String[] { "tasApplies", "tasUser" });// "tasApplies",
		Map<String, List<?>> map = new HashMap<String, List<?>>();
		map.put("TaskList", taskList);
		json.putAll(map, jsonConfig);

		// 放入总页数
		if (curPage == 0) {
			int iSize = giveDaoInstance().getObjectSizeBycond("select count(*) from Apply " + sCond);
			json.put("size", iSize);
		}
		return "success";
	}

	/**
	 * 功能：根据任务状态获取经过发布时间倒序排序的所有任务(个人任务分页-发布的任务)
	 * 
	 * @return
	 */
	public String giveTaskByState() {

		json = giveJsonInstance();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		String sCond;
		if ("2".equals(tasState)) {// 获取进行中任务，将查询进行中和失效任务（失效不等于失败，会继续进行）
			sCond = "where tasUser=" + userId + " and tasState in(2,6) order by tasId desc";
		} else
			sCond = "where tasUser=" + userId + " and tasState=" + tasState + " order by tasId desc";

		List<?> list = giveDaoInstance().pageListWithCond("Task", curPage, PERSON_PerPageRow, sCond);

		List<Task> taskList = new ArrayList<Task>();
		if (list.size() > 0 && list.get(0) instanceof Task) {// 如果集合不为空且为Task类型
			for (Object object : list) {
				taskList.add((Task) object);
			}
		}

		// 去掉json多余参数
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setIgnoreDefaultExcludes(false);
		jsonConfig.setExcludes(new String[] { "tasApplies", "tasUser" });
		Map<String, List<?>> map = new HashMap<String, List<?>>();
		map.put("TaskList", taskList);
		json.putAll(map, jsonConfig);

		// 放入总页数
		if (curPage == 0) {
			int iSize = giveDaoInstance().getObjectSizeBycond("select count(*) from Task " + sCond);
			json.put("size", iSize);
		}

		return "success";
	}

	/**
	 * 功能：根据任务Id获取相应的申请
	 * 
	 * @return
	 */
	public String giveApplyByTasId() {

		json = giveJsonInstance();

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		if (task != null && task.getTasState() == 1) {// 任务为申请中状态

			String sCond = "where appTask=" + tasId + " and appState=0";
			List<?> list = giveDaoInstance().getObjectListBycond("Apply", sCond);

			JSONArray jArray = new JSONArray();
			// 去掉json多余参数
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setIgnoreDefaultExcludes(false);
			jsonConfig.setExcludes(new String[] { "appTask" });
			if (list.size() > 0 && list.get(0) instanceof Apply) {// 如果集合不为空且为Task类型
				for (Object object1 : list)
					jArray.add((Apply) object1, jsonConfig);
			}
			json.put("ApplyList", jArray);
		} else {// 任务为其他状态

			String sCond = "where appTask=" + tasId + " and appState in(1,3,4,5)";// 不能加入未通过的申请，因为这样的申请和任务没有半点关系
			List<?> list = giveDaoInstance().getObjectListBycond("Apply", sCond);

			JSONArray jArray = new JSONArray();
			if (list.size() > 0 && list.get(0) instanceof Apply) {// 如果集合不为空且为Task类型
				for (Object object1 : list)
					jArray.add(((Apply) object1).getAppBeUser());
			}
			json.put("UserList", jArray);
		}

		return "success";
	}

	/**
	 * 功能：通过任务获取任务发布者
	 * 
	 * @return
	 */
	public String giveTasUserByTasId() {

		json = giveJsonInstance();
		// 由于前台需求，需同时调用giveTasUserByTasId与giveApplyByTasId，因而出现session was
		// closed现象，故这里重新建立一个ObjectDaoImpl对象来解决并发问题
		Object object = new ObjectDaoImpl().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;
		if (task != null) {
			json = giveJsonInstance();
			json.put("User", task.getTasUser());
		}
		return "success";
	}

	/**
	 * 功能：通过任务Id获取任务
	 * 
	 * @return
	 */
	public String giveTaskById() {

		json = giveJsonInstance();

		Object object1 = ServletActionContext.getRequest().getSession().getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		Object object = giveDaoInstance().getObjectById(Task.class, tasId);
		Task task = object != null && object instanceof Task ? (Task) object : null;

		if (task != null) {
			json.put("Task", task);
			for (Apply apply : task.getTasApplies()) {
				if (apply.getAppBeUser().getUseId().intValue() == user.getUseId().intValue()) {// 自己的申请
					if (apply.getAppState() == 0)
						json.put("State", "0");// 申请中
					if (apply.getAppState() == 1 && apply.getAppTask().getTasState() == 1)
						json.put("State", "1");// 设置游离态任务实体为申请中状态：“申请”已通过，但任务仍处于申请中，说明为团队任务：任务规定人数未达齐，仍是申请中状态
					task.setAppId(apply.getAppId());
				}
			}
			// 去掉json多余参数
			((JSONObject) json.get("TaskList")).remove("tasApplies");
		}
		return "success";
	}

	// //////////////////////////////////////////////////////////////////////////

	/* seeter、getter */
	public void setTasId(Integer tasId) {
		this.tasId = tasId;
	}

	public void setTasContent(String tasContent) {
		this.tasContent = tasContent;
	}

	public void setTasPrice(Double tasPrice) {
		this.tasPrice = tasPrice;
	}

	public void setTasType(String tasType) {
		this.tasType = tasType;
	}

	public void setCurPage(Integer curPage) {
		this.curPage = curPage;
	}

	public void setTasState(String tasState) {
		this.tasState = tasState;
	}

	public void setTasTitle(String tasTitle) {
		this.tasTitle = tasTitle;
	}

	public void setTasTimeout(String tasTimeout) {
		this.tasTimeout = tasTimeout;
	}

	public void setTasContact(String tasContact) {
		this.tasContact = tasContact;
	}

	public void setTasRulenum(Integer tasRulenum) {
		this.tasRulenum = tasRulenum;
	}

	public void setAppReason(String appReason) {
		this.appReason = appReason;
	}

	public void setTasEvaluate(String tasEvaluate) {
		this.tasEvaluate = tasEvaluate;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public void setTasCredit(Integer tasCredit) {
		this.tasCredit = tasCredit;
	}

	public void setPushPoint(String pushPoint) {
		this.pushPoint = pushPoint;
	}

}
