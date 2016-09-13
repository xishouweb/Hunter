package net.navagraha.hunter.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;
import net.navagraha.hunter.tool.JoinPushTool;
import net.navagraha.hunter.tool.PhoneCodeTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts2.ServletActionContext;

public class TaskAction {

	// Fields
	private Integer tasId;
	private String tasTitle;
	private String tasContact;
	private String tasContent;
	private Double tasPrice;
	private String tasType;
	private String tasState;
	private String tasTimeout;
	private Integer tasRulenum;
	private String tasEvaluate;
	private Integer tasCredit;

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件
	private static int HOME_PerPageRow;// 任务大厅每页显示任务数量
	private static int LOG_PerPageRow;// 任务日志每页显示任务数量
	private static int PERSON_PerPageRow;// 任务发布每页显示任务数量
	private static double SUCCESS_TAX;// 成功服务费
	private static double FALSE_TAX;// 失败服务费
	private static int RuleReceive;// 每人限制任务数
	// TODO 未实现发布接受规定任务数得奖励功能
	// private static int RecMoney;
	// private static int PubMoney;

	static {
		HOME_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("HOME_PerPageRow"));
		LOG_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("LOG_PerPageRow"));
		PERSON_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("PERSON_PerPageRow"));
		RuleReceive = Integer.parseInt(propertyUtil
				.getPropertyValue("RuleReceive"));
		SUCCESS_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("SUCCESS_TAX"));
		FALSE_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("FALSE_TAX"));
		// RecMoney
		// =Integer.parseInt(propertyUtil.getPropertyValue("RecMoney"));//接受任务数奖励
		// PubMoney
		// =Integer.parseInt(propertyUtil.getPropertyValue("PubMoney"));//发布任务数奖励
	}

	// 前台传入
	private Integer curPage;
	private Integer appId;
	private String appReason;

	// 反馈到前台
	public String code;
	public static JSONObject json = new JSONObject();
	public String pushPoint;

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	public String hasAlipay() {
		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;
		if (user != null) {
			if (user.getUseAlipay().equals("") || user.getUseAlipay() == null
					|| user.getUseName() == null
					|| user.getUseName().equals("")) {
				setCode("21");
				return "success";
			}
		}
		setCode("1");
		return "success";
	}

	// 发布任务
	@SuppressWarnings("deprecation")
	public String publishTask() {

		Object object = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object != null ? (Users) object : null;

		Task task = null;

		if (user != null) {

			task = new Task();
			task.setTasTitle(tasTitle);
			task.setTasPrice(tasPrice);
			task.setTasType(tasType);
			task.setTasState(0);
			task.setTasContact(tasContact);
			task.setTasContent(tasContent);
			Date date = new Date();
			task.setTasTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(date));

			Object str = ServletActionContext.getRequest().getSession()
					.getAttribute("ImgPath");
			if (str != null) {
				task.setTasImg(str.toString());
				ServletActionContext.getRequest().getSession()
						.setAttribute("ImgPath", null);
			}
			if (tasType.equals("加急个人")) {// 加急个人任务
				tasTimeout = tasTimeout.split(":")[0].length() < 2 ? "0"
						+ tasTimeout : tasTimeout;
				task.setTasTimeout(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date).substring(0, 10)
						+ " "
						+ tasTimeout
						+ ":00");
			}
			if (tasType.equals("个人")) {// 个人任务
				date.setHours(date.getHours() + 48);// 默认显示2天
				task.setTasTimeout(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));
			}
			if (tasType.equals("团队")) {// 团队任务
				date.setHours(date.getHours() + 48);// 默认显示2天
				task.setTasTimeout(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));
				task.setTasRulenum(tasRulenum);// 限定人数
				task.setTasReceivenum(0);
				task.setTasFinishnum(0);
			}

		} else {
			setCode("0");
			return "success";
		}

		if (task != null) {
			try {
				user.setUsePublishnum(user.getUsePublishnum() + 1);

				/** 发钱记录 */
				Money money = new Money();
				money.setMonAlipay(user.getUseAlipay());

				money.setMonName(user.getUseName());
				money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
						.format(new Date())
						+ user.getUseSno().substring(
								user.getUseSno().length() - 4));
				money.setMonPay(tasPrice);
				money.setMonState(4);// 发钱（不显示）
				money.setMonPhone(user.getUsePhone());
				money.setMonComment("/");
				money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
						.format(new Date()));
				// if (payType != null && payType == 0) {// 余额支付
				// user.setUseRemain(user.getUseRemain() - tasPrice);// 设置余额
				// money.setMonType("【任务发布】悬赏金(平台余额支付)");
				// } else
				money.setMonType("【任务发布】悬赏金(支付宝支付)");
				giveDao().save(money);
				/** 发钱记录结束 */

				task.setTasUser(user);
				giveDao().update(user);
				ServletActionContext.getRequest().getSession()
						.setAttribute("Users", user);// 将更新用户存入session
				giveDao().save(task);
				setCode("1");

			} catch (Exception e) {
				e.printStackTrace();
				setCode("0");
			}
		} else
			setCode("0");

		return "success";
	}

	// 用户接受任务
	public String receiveTask() {

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		Object object1 = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		if (task != null && user != null) {

			if (user.getUseAlipay().equals("") || user.getUseAlipay() == null) {// 先完善支付宝账户
				setCode("21");
				return "success";
			}

			for (Apply apply : task.getTasApplies()) {
				if (apply.getAppBeUser().getUseId().intValue() == user
						.getUseId().intValue()) {// 已申请,不必重复申请
					setCode("19");
					return "success";
				}
			}

			// 申请与进行的任务已达到上限
			String cond = "where appBeUser=" + user.getUseId()
					+ " and appState in(0,1) order by appId desc";
			int size = giveDao().getObjectSizeBycond(
					"select count(*) from Apply " + cond);
			if (size > RuleReceive) {
				setCode("24");// 申请+进行任务数已达上限
				return "success";
			}

			// 记录tag
			List<?> list = giveDao().getObjectListByfield("Tag", "tagUser",
					user);
			Tag tag = list.size() > 0 ? (Tag) list.get(0) : null;

			if (tag != null) {
				// 设置接收任务性别
				String strs[] = tag.getTagSex().split(",");
				int a = Integer.parseInt(strs[0]), b = Integer
						.parseInt(strs[1]);
				if (task.getTasUser().getUseSex().equals("男"))
					a++;
				if (task.getTasUser().getUseSex().equals("女"))
					b++;
				tag.setTagSex(a + "," + b);
				// 设置接收任务类型
				strs = tag.getTagTasktype().split(",");
				int c = Integer.parseInt(strs[0]), d = Integer
						.parseInt(strs[1]), e = Integer.parseInt(strs[2]);
				if (task.getTasType().equals("加急个人"))
					c++;
				if (task.getTasType().equals("个人"))
					d++;
				if (task.getTasType().equals("团队"))
					e++;
				tag.setTagTasktype(c + "," + d + "," + e);
				giveDao().update(tag);
			}
			// 记录tag结束

			Apply apply = new Apply();
			apply.setAppBeUser(user);
			apply.setAppTask(task);
			apply.setAppTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date()));

			if (task.getTasType().equals("加急个人")) {
				if (task.getTasState() == 0) {// 任务可接

					apply.setAppReason("加急任务，自动申请通过");
					apply.setAppState(1);
					task.setTasState(2);// 任务进行中
					giveDao().save(apply);

					Set<Apply> set = new HashSet<Apply>(0);
					set.add(apply);
					task.setTasApplies(set);
					giveDao().update(task);
					setCode("1");

					// 消息推送接受者
					String phone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("12" + task.getTasTitle(), phone);

					// 消息推送发布者
					phone = task.getTasUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("01" + task.getTasTitle(), phone);

					return "success";
				} else {
					setCode("10");// 任务不可接
					return "success";
				}

			} else {
				if (task.getTasState() == 0 || task.getTasState() == 1) {
					apply.setAppReason(appReason);
					apply.setAppState(0);
					giveDao().save(apply);

					task.setTasState(1);// 任务申请中
					giveDao().update(task);
					setCode("1");

					// 消息推送接受者
					String phone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("10" + task.getTasTitle(), phone);

					// 消息推送发布者
					phone = task.getTasUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("00" + task.getTasTitle(), phone);

					return "success";
				} else {
					setCode("10");// 任务不可接
					return "success";
				}
			}
		} else {
			setCode("9");// 任务不存在
			return "success";
		}
	}

	// 取消申请任务(仅限申请中任务，即appState=0)
	public String backReceiveTask() {

		Object object = giveDao().getObjectById(Apply.class, appId);
		Apply apply = object != null ? (Apply) object : null;
		System.out.println(appId);
		if (apply != null && apply.getAppState() == 0) {
			giveDao().delete(apply);
			setCode("1");// 操作成功
		} else
			setCode("23");// 任务已进入进行状态，不能取消

		return "success";

	}

	// 取消发布任务（仅限发布的任务，即tasState=0,注：传入的任务应该tasState=0,1,2）
	public String backTask() {
		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null && task.getTasState() == 0) {

			task.setTasState(5);// 任务失败
			task.setTasEvaluate("用户撤销任务，导致任务失败");
			giveDao().update(task);

			Users user = task.getTasUser();
			user.setUseRemain(user.getUseRemain() + task.getTasPrice()
					* (1 - FALSE_TAX));// 存入余额
			giveDao().update(user);
			ServletActionContext.getRequest().getSession()
					.setAttribute("Users", user);// 将更新用户存入session

			/** 返钱记录 */
			Money money = new Money();
			money.setMonAlipay(user.getUseAlipay());
			money.setMonComment("/");
			money.setMonName(user.getUseName());
			money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date())
					+ user.getUseSno().substring(user.getUseSno().length() - 4));
			money.setMonPay(task.getTasPrice() * (1 - FALSE_TAX));
			money.setMonState(3);// 打钱（不显示）
			money.setMonPhone(user.getUsePhone());
			money.setMonType("【撤销任务】返金");
			money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
					.format(new Date()));
			giveDao().save(money);
			/** 返钱记录结束 */

			/** 支付日志 **/
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			Pay payOut;

			// 获取任务发布者的pay
			List<?> list1 = giveDao().getObjectListByfield("Pay",
					new String[] { "payTime", "payUser" },
					new Object[] { sdf.format(new Date()), user });

			if (list1.size() > 0) {
				// 支出
				payOut = (Pay) list1.get(0);
				payOut.setPayOut(payOut.getPayOut() + task.getTasPrice()
						* FALSE_TAX);
			} else {
				// 支出
				payOut = new Pay();
				payOut.setPayTime(sdf.format(new Date()));
				payOut.setPayIn(0.0);
				payOut.setPayOut(task.getTasPrice() * FALSE_TAX);
				payOut.setPayUser(task.getTasUser());
			}
			giveDao().saveOrUpdate(payOut);
			/** 支付日志结束 **/

			// 消息推送发布者
			String phone = task.getTasUser().getUsePhone();
			if (JoinPushTool.connections.containsKey(phone))
				JoinPushTool.broadcast("04" + task.getTasTitle(), phone);

			setCode("1");// 操作成功
		} else
			setCode("22");// 任务已过发布和申请状态，不能撤销

		return "success";
	}

	// 用户审核通过申请(非加急)
	public String checkSuccessTask() {
		if (appId == null) {
			setCode("13");// 申请不存在
			return "success";
		}
		Object object = giveDao().getObjectById(Apply.class, appId);
		Apply apply = object != null ? (Apply) object : null;

		if (apply != null) {
			Task task = apply.getAppTask();
			if (task != null && task.getTasState() == 1) {

				// 本申请通过
				Users user = apply.getAppBeUser();
				user.setUseAcceptnum(user.getUseAcceptnum() + 1);
				giveDao().update(user);
				ServletActionContext.getRequest().getSession()
						.setAttribute("Users", user);// 将更新用户存入session

				if (task.getTasType().equals("个人")) {// 个人任务

					// 其他申请自动设置为不通过
					List<?> appList = giveDao().getObjectListByfield("Apply",
							"appTask", task);
					for (Object object2 : appList) {
						Apply apply2 = (Apply) object2;
						if (apply2.getAppId().intValue() != appId) {// 不通过
							apply2.setAppState(2);
							giveDao().update(apply2);

							// 不通过 消息推送接受者
							String phone = apply2.getAppBeUser().getUsePhone();
							if (JoinPushTool.connections.containsKey(phone))
								JoinPushTool.broadcast(
										"11" + task.getTasTitle(), phone);
						} else {// 通过 消息推送接受者
							if (!JoinPushTool.connections.containsKey(apply2
									.getAppBeUser().getUsePhone())) // 用户不在线，发送短信
								PhoneCodeTool.send(apply2.getAppBeUser()
										.getUsePhone(), task.getTasTitle(),
										"apply");
							else {// 用户在线，消息推送通知
								String phone = apply2.getAppBeUser()
										.getUsePhone();
								JoinPushTool.broadcast(
										"12" + task.getTasTitle(), phone);
							}
						}
					}

					apply.setAppState(1);// 通过
					giveDao().update(apply);

					task.setTasState(2);// 审核成功,任务进行中
					Set<Apply> set = new HashSet<Apply>(0);
					set.add(apply);
					task.setTasApplies(set);
					giveDao().update(task);
					setCode("1");// 操作成功

					// 消息推送发布者
					String phone = task.getTasUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("01" + task.getTasTitle(), phone);

					return "success";
				} else {// 团队任务
					if (task.getTasReceivenum().intValue() == task
							.getTasRulenum().intValue() - 1) {// 任务人数已达齐，开始进行
						apply.setAppState(1);// 通过
						giveDao().update(apply);

						task.setTasState(2);// 审核成功,任务进行中
						Set<Apply> set = task.getTasApplies();
						set.add(apply);
						task.setTasApplies(set);
						task.setTasReceivenum(task.getTasReceivenum() + 1);
						giveDao().update(task);

						// 其他申请自动设置为不通过
						List<?> appList = giveDao().getObjectListByfield(
								"Apply", "appTask", task);
						for (Object object2 : appList) {// 总申请
							Apply apply2 = (Apply) object2;
							boolean in = false;
							for (Apply apply3 : set) {// 已通过的申请
								if (apply2.getAppId().intValue() == apply3
										.getAppId().intValue())
									in = true;

								if (!in) {// 不通过的
									apply2.setAppState(2);
									giveDao().update(apply2);

									// 不通过 消息推送接受者
									String phone = apply2.getAppBeUser()
											.getUsePhone();
									if (JoinPushTool.connections
											.containsKey(phone))
										JoinPushTool.broadcast(
												"11" + task.getTasTitle(),
												phone);
								} else {// 通过的
									if (!JoinPushTool.connections
											.containsKey(apply2.getAppBeUser()
													.getUsePhone())) // 用户不在线，发送短信
										PhoneCodeTool.send(apply2
												.getAppBeUser().getUsePhone(),
												task.getTasTitle(), "apply");
									else {// 用户在线，消息推送
										String phone = apply2.getAppBeUser()
												.getUsePhone();
										JoinPushTool.broadcast(
												"12" + task.getTasTitle(),
												phone);
									}
								}
							}
						}
						setCode("1");// 操作成功

						// 消息推送发布者
						String phone = task.getTasUser().getUsePhone();
						if (JoinPushTool.connections.containsKey(phone))
							JoinPushTool.broadcast("01" + task.getTasTitle(),
									phone);

						return "success";
					} else if (task.getTasReceivenum().intValue() < task
							.getTasRulenum().intValue() - 1) {// 任务人数未达齐
						apply.setAppState(1);// 通过
						giveDao().update(apply);

						task.setTasState(1);// 审核成功,任务未进行
						Set<Apply> set = task.getTasApplies();
						set = set.size() < 1 ? new HashSet<Apply>(0) : set;
						set.add(apply);
						task.setTasReceivenum(task.getTasReceivenum() + 1);
						task.setTasApplies(set);
						giveDao().update(task);
						setCode("1");// 操作成功

						// 消息推送接受者 任务未开始，只通知本申请的接受者
						String phone = apply.getAppBeUser().getUsePhone();
						if (JoinPushTool.connections.containsKey(phone))
							JoinPushTool.broadcast("10" + task.getTasTitle(),
									phone);

						return "success";
					} else {
						setCode("12");// 规定人数已满
						return "success";
					}
				}
			} else {
				setCode("9");// 任务不存在
				return "success";
			}
		} else {
			setCode("13");// 申请不存在
			return "success";
		}
	}

	// 用户审核不通过申请(非加急)
	public String checkFalseTask() {

		Object object = giveDao().getObjectById(Apply.class, appId);
		Apply apply = object != null ? (Apply) object : null;
		Task task = apply.getAppTask();

		if (task != null && task.getTasState() == 1) {
			if (apply != null) {
				apply.setAppState(2);
				giveDao().update(apply);
				setCode("1");

				// 消息推送接受者
				String phone = apply.getAppBeUser().getUsePhone();
				if (JoinPushTool.connections.containsKey(phone))
					JoinPushTool.broadcast("11" + task.getTasTitle(), phone);

				return "success";
			} else {
				setCode("13");// 申请记录不存在
				return "success";
			}
		} else {
			setCode("9");// 任务不存在
			return "success";
		}
	}

	// 用户要完成任务
	public String finishTask() {

		Object object1 = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null) {

			if (task.getTasState() == 5) {
				setCode("15");// 任务已失败
				return "success";
			}
			if (task.getTasState() == 3) {
				setCode("14");// 已点击了申请完成，不必重新点击申请完成
				return "success";
			}

			if (!task.getTasType().equals("团队")) {// 个人任务

				for (Apply apply : task.getTasApplies()) {
					if (apply.getAppBeUser().getUseId().intValue() == user
							.getUseId().intValue()) {
						if (apply.getAppState().intValue() == 5) {
							setCode("14");// 已点击了申请完成，不必重新点击申请完成
							return "success";
						} else {
							apply.setAppState(5);
							objectDao.update(apply);// 设置该用户申请已提交（避免重复提交）
							break;
						}

					}
				}

				task.setTasFinishtime(new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date()));
				task.setTasState(3);// 提交审核
				giveDao().update(task);
				setCode("1");// 操作成功

				// 消息推送接受者
				String phone = user.getUsePhone();
				if (JoinPushTool.connections.containsKey(phone))
					JoinPushTool.broadcast("13" + task.getTasTitle(), phone);

				// 消息推送发布者
				phone = task.getTasUser().getUsePhone();
				if (JoinPushTool.connections.containsKey(phone))
					JoinPushTool.broadcast("02" + task.getTasTitle(), phone);
				else
					PhoneCodeTool.send(phone, task.getTasTitle(), "task");// 短信提示发布者任务已完成

				return "success";

			} else {// 团队任务

				for (Apply apply : task.getTasApplies()) {
					if (apply.getAppBeUser().getUseId().intValue() == user
							.getUseId().intValue()) {
						if (apply.getAppState().intValue() == 5) {
							setCode("14");// 已点击了申请完成，不必重新点击申请完成
							return "success";
						} else {
							apply.setAppState(5);
							objectDao.update(apply);// 设置该用户申请已提交（避免重复提交）
							break;
						}

					}
				}

				if (task.getTasFinishnum().intValue() == task.getTasRulenum()
						.intValue() - 1) {// 任务人数已达齐，开始申请审核

					task.setTasFinishtime(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(new Date()));
					task.setTasFinishnum(task.getTasRulenum());
					task.setTasState(3);// 提交审核
					giveDao().update(task);
					setCode("1");// 操作成功

					// 消息推送发布者
					String phone = task.getTasUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("02" + task.getTasTitle(), phone);
					else
						PhoneCodeTool.send(task.getTasUser().getUsePhone(),
								task.getTasTitle(), "task");// 短信提示发布者任务已完成

					return "success";
				} else {// 任务人数未达齐
					task.setTasFinishnum(task.getTasFinishnum() + 1);
					task.setTasState(2);// 任务仍是进行中
					giveDao().update(task);
					setCode("1");// 操作成功

					// 消息推送接受者 仅仅是该操作完成者
					String phone = user.getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("13" + task.getTasTitle(), phone);

					return "success";
				}
			}
		} else {
			setCode("9");// 任务不存在
			return "success";
		}

	}

	// 用户允许通过任务
	public String successTask() {

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null && task.getTasState() == 3) {
			Set<Apply> set = task.getTasApplies();

			if (set.size() > 0) {

				/** 支付日志 **/
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				// 获取任务发布者的pay
				Pay payOut;
				List<?> list1 = giveDao().getObjectListByfield(
						"Pay",
						new String[] { "payTime", "payUser" },
						new Object[] { sdf.format(new Date()),
								task.getTasUser() });

				if (list1.size() > 0) {
					// 支出
					payOut = (Pay) list1.get(0);
					payOut.setPayOut(payOut.getPayOut() + task.getTasPrice()
							+ 0.0);
				} else {
					// 支出
					payOut = new Pay();
					payOut.setPayTime(sdf.format(new Date()));
					payOut.setPayIn(0.0);
					payOut.setPayOut(task.getTasPrice() + 0.0);
					payOut.setPayUser(task.getTasUser());
				}
				giveDao().saveOrUpdate(payOut);
				/** 支付日志结束 **/

				for (Apply apply : set) {

					if (apply.getAppState() == 1 || apply.getAppState() == 5) {// 任务的真实接受者
						apply.setAppState(3);// 任务成功
						giveDao().update(apply);

						Users user = apply.getAppBeUser();
						user.setUseRemain(user.getUseRemain()
								+ task.getTasPrice() * (1 - SUCCESS_TAX)
								/ set.size());// 存入余额
						giveDao().update(user);
						ServletActionContext.getRequest().getSession()
								.setAttribute("Users", user);// 将更新用户存入session

						/** 打钱记录 */
						Money money = new Money();
						money.setMonAlipay(user.getUseAlipay());
						money.setMonComment("/");
						money.setMonName(user.getUseName());
						money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
								.format(new Date())
								+ user.getUseSno().substring(
										user.getUseSno().length() - 4));
						money.setMonPay(task.getTasPrice() * (1 - SUCCESS_TAX)
								/ set.size());
						money.setMonState(3);// 打钱（不显示）
						money.setMonPhone(user.getUsePhone());
						money.setMonType("【任务完成】赏金");
						money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
								.format(new Date()));
						giveDao().save(money);
						/** 打钱记录结束 */

						/** 能力 **/
						// 获取任务接收者的power

						List<?> list3 = giveDao().getObjectListByfield("Power",
								"powUser", user);
						Power power;
						if (tasCredit != null && list3.size() > 0) {
							power = (Power) list3.get(0);
							if (power.getPowCredit() + tasCredit < 790)
								power.setPowCredit(power.getPowCredit()
										+ tasCredit);
							else
								power.setPowCredit(790);// 满级
						} else {
							power = new Power();
							power.setPowCredit(50 + tasCredit);
							power.setPowUser(user);
							power.setPowFast(0);
						}

						if (tasCredit > 3) {
							if (power.getPowFast() < 299)
								power.setPowFast(power.getPowFast() + 1);
							else
								power.setPowFast(300);// 满级
						}
						objectDao.saveOrUpdate(power);
						/** 能力结束 **/

						/** 支付日志 **/
						Pay payIn;

						// 获取任务收入者的pay
						List<?> list2 = giveDao().getObjectListByfield("Pay",
								new String[] { "payTime", "payUser" },
								new Object[] { sdf.format(new Date()), user });

						if (list2.size() > 0) {
							// 收入
							payIn = (Pay) list2.get(0);
							if (task.getTasType().equals("团队"))
								payIn.setPayIn(payIn.getPayIn()
										+ task.getTasPrice()
										* (1 - SUCCESS_TAX)
										/ task.getTasRulenum());
							else
								payIn.setPayIn(payIn.getPayIn()
										+ task.getTasPrice()
										* (1 - SUCCESS_TAX));
						} else {
							// 收入
							payIn = new Pay();
							payIn.setPayTime(sdf.format(new Date()));
							payIn.setPayOut(0.0);
							if (task.getTasType().equals("团队")) {
								payIn.setPayIn(task.getTasPrice()
										* (1 - SUCCESS_TAX)
										/ task.getTasRulenum());
							} else
								payIn.setPayIn(task.getTasPrice()
										* (1 - SUCCESS_TAX));
							payIn.setPayUser(user);
						}
						giveDao().saveOrUpdate(payIn);
						/** 支付日志结束 **/

					}

					// 消息推送接受者
					String phone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("14" + task.getTasTitle(), phone);
				}
				task.setTasState(4);// 任务成功
				task.setTasEvaluate(tasEvaluate);
				task.setTasCredit(tasCredit);
				giveDao().update(task);
				setCode("1");// 操作成功

				// 消息推送发布者
				String phone = task.getTasUser().getUsePhone();
				if (JoinPushTool.connections.containsKey(phone))
					JoinPushTool.broadcast("03" + task.getTasTitle(), phone);

				return "success";
			} else {
				setCode("16");// 接受者们不存在
				return "success";
			}
		} else {
			setCode("9");// 任务不存在
			return "success";
		}
	}

	// 用户不允许通过任务
	public String falseTask() {

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null && task.getTasState() == 3) {
			Set<Apply> set = task.getTasApplies();

			if (set.size() > 0) {

				/** 能力 **/
				for (Apply apply : set) {

					if (apply.getAppState() == 1 || apply.getAppState() == 5) {// 任务的真实接受者
						apply.setAppState(4);// 任务失败
						giveDao().update(apply);

						Users beUser = apply.getAppBeUser();
						// 获取任务接收者的power
						List<?> list3 = giveDao().getObjectListByfield("Power",
								"powUser", beUser);
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
						objectDao.saveOrUpdate(power);

					}

					// 消息推送接受者
					String phone = apply.getAppBeUser().getUsePhone();
					if (JoinPushTool.connections.containsKey(phone))
						JoinPushTool
								.broadcast("15" + task.getTasTitle(), phone);
				}
				/** 能力结束 **/

				// 消息推送发布者
				String phone = task.getTasUser().getUsePhone();
				if (JoinPushTool.connections.containsKey(phone))
					JoinPushTool.broadcast("04" + task.getTasTitle(), phone);

				if (task.getTasState() == 6) {// 任务已失效，即失败
					task.setTasState(5);// 任务失败
					giveDao().update(task);
					Users user = task.getTasUser();
					user.setUseRemain(user.getUseRemain() + task.getTasPrice()
							* (1 - FALSE_TAX));// 存入余额
					giveDao().update(user);
					ServletActionContext.getRequest().getSession()
							.setAttribute("Users", user);// 将更新用户存入session

					/** 返钱记录 */
					Money money = new Money();
					money.setMonAlipay(user.getUseAlipay());
					money.setMonComment("/");
					money.setMonName(user.getUseName());
					money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
							.format(new Date())
							+ user.getUseSno().substring(
									user.getUseSno().length() - 4));
					money.setMonPay(task.getTasPrice() * (1 - FALSE_TAX));
					money.setMonState(3);// 打钱（不显示）
					money.setMonPhone(user.getUsePhone());
					money.setMonType("【任务无人接受】返金");
					money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
							.format(new Date()));
					giveDao().save(money);
					/** 返钱记录结束 */

					/** 支付日志 **/
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					Pay payOut;

					// 获取任务发布者的pay
					List<?> list1 = giveDao().getObjectListByfield("Pay",
							new String[] { "payTime", "payUser" },
							new Object[] { sdf.format(new Date()), user });

					if (list1.size() > 0) {
						// 支出者
						payOut = (Pay) list1.get(0);
						payOut.setPayOut(payOut.getPayOut()
								+ task.getTasPrice() * FALSE_TAX);
					} else {
						// 支出者
						payOut = new Pay();
						payOut.setPayTime(sdf.format(new Date()));
						payOut.setPayIn(0.0);
						payOut.setPayOut(task.getTasPrice() * FALSE_TAX);
						payOut.setPayUser(task.getTasUser());
					}
					giveDao().saveOrUpdate(payOut);
					/** 支付日志结束 **/

					setCode("1");// 操作成功
					return "success";
				} else {
					task.setTasState(0);// 任务已发布，继续可接
					task.setTasApplies(null);// 接任务人清空
					giveDao().update(task);

					setCode("1");// 操作成功
					return "success";
				}
			} else {
				setCode("16");// 接受者不存在
				return "success";
			}
		} else {
			setCode("9");// 任务不存在
			return "success";
		}
	}

	// 根据发布类型获取经过发布时间倒序排序的所有任务(任务大厅分页)
	public String giveTaskByType() {

		json = new JSONObject();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		List<?> list = giveDao().pageListWithCond(
				"Task",
				curPage,
				HOME_PerPageRow,
				"where tasType='" + tasType
						+ "' and  tasState in (0,1) order by tasTime desc");
		List<Task> taskList = new ArrayList<Task>();
		for (Object object : list) {

			Task task = (Task) object;
			if (task.getTasUser().getUseId().intValue() == userId) // 任务发送者是自己
				task.setState("2");

			for (Apply apply : task.getTasApplies()) {

				if (apply.getAppBeUser().getUseId().intValue() == userId) {// 该任务申请者有本人
					task.setAppId(apply.getAppId());// 可不传
					if (apply.getAppState() == 0)
						task.setState("0");
					if (apply.getAppState() == 1
							&& apply.getAppTask().getTasState() == 1)
						task.setState("1");// 通过申请，但任务需要人数未达齐，仍是申请中（团队任务）
				}
			}

			taskList.add((Task) object);
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
			int size = giveDao().getObjectSizeBycond(
					"select count(*) from Task where tasType='" + tasType
							+ "' and  tasState in (0,1)");
			json.put("size", size);
		}
		return "success";
	}

	// 获取经过发布时间倒序排序的所有特殊任务（任务大厅分页-特殊）
	public String giveSpecialTask() {

		json = new JSONObject();

		List<?> list0 = giveDao().getObjectListBycond("Users",
				"where useIscompany=1");

		List<?> list = giveDao()
				.pageListWithCond(
						"Task",
						curPage,
						HOME_PerPageRow,
						"where tasUser in(:list) and  tasState in (0,1) order by tasTime desc",
						list0);
		List<Task> taskList = new ArrayList<Task>();
		for (Object object : list) {
			taskList.add((Task) object);
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
			int size = giveDao()
					.getObjectSizeBycond(
							"select count(*) from Task where tasUser in(:list) and  tasState in (0,1)",
							list0);
			json.put("size", size);
		}

		return "success";
	}

	// 根据任务状态获取经过申请时间倒序排序的所有任务(任务日志分页-接受的任务)
	public String giveBeTaskByState() {

		json = new JSONObject();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		String cond = "where appBeUser=" + userId + " and appState=" + tasState
				+ " order by appId desc";

		List<?> list = giveDao().pageListWithCond("Apply", curPage,
				LOG_PerPageRow, cond);
		List<Task> taskList = new ArrayList<Task>();
		for (Object object : list) {
			Apply apply = ((Apply) object);
			Task task = apply.getAppTask();
			task.setAppId(apply.getAppId());
			if (apply.getAppState() == 0)
				task.setState("0");
			if (apply.getAppState() == 1
					&& apply.getAppTask().getTasState() == 1)
				task.setState("1");// 通过申请，但任务需要人数未达齐，仍是申请中（团队任务）
			taskList.add(task);

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
			int size = giveDao().getObjectSizeBycond(
					"select count(*) from Apply " + cond);
			json.put("size", size);
		}
		return "success";
	}

	// 根据任务状态获取经过发布时间倒序排序的所有任务(个人任务分页-发布的任务)
	public String giveTaskByState() {

		json = new JSONObject();

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		String cond;
		if (tasState.equals("2")) {
			cond = "where tasUser=" + userId
					+ " and tasState in(2,6) order by tasId desc";
		} else
			cond = "where tasUser=" + userId + " and tasState=" + tasState
					+ " order by tasId desc";

		List<?> list = giveDao().pageListWithCond("Task", curPage,
				PERSON_PerPageRow, cond);
		List<Task> taskList = new ArrayList<Task>();
		for (Object object : list) {
			taskList.add((Task) object);
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
			int size = giveDao().getObjectSizeBycond(
					"select count(*) from Task " + cond);
			json.put("size", size);
		}

		return "success";
	}

	// 根据任务Id获取相应的申请
	public String giveApplyByTasId() {

		json = new JSONObject();

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		if (task != null && task.getTasState() == 1) {// 任务为申请中

			String cond = "where appTask=" + tasId + " and appState=0";
			List<?> list = giveDao().getObjectListBycond("Apply", cond);

			JSONArray jArray = new JSONArray();
			// 去掉json多余参数
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setIgnoreDefaultExcludes(false);
			jsonConfig.setExcludes(new String[] { "appTask" });
			for (Object object1 : list)
				jArray.add((Apply) object1, jsonConfig);
			json.put("ApplyList", jArray);
		} else {// 任务为其他状态

			String cond = "where appTask=" + tasId
					+ " and appState in(1,3,4,5)";// 不能加入未通过的申请，因为，这样的申请是和任务没有半点关系的
			List<?> list = giveDao().getObjectListBycond("Apply", cond);

			JSONArray jArray = new JSONArray();
			for (Object object1 : list)
				jArray.add(((Apply) object1).getAppBeUser());
			json.put("UserList", jArray);
		}

		return "success";
	}

	// 通过任务获取任务发布者
	public String giveTasUserByTasId() {

		json = new JSONObject();
		// 由于前台需求，同时调用giveTasUserByTasId与giveApplyByTasId，因而出现session was
		// closed现象，故，这里重新建立一个ObjectDaoImpl对象来解决并发问题
		Object object = new ObjectDaoImpl().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		if (task != null) {
			json = new JSONObject();
			json.put("User", task.getTasUser());
		}
		return "success";
	}

	// 通过任务Id获取任务
	public String giveTaskById() {

		json = new JSONObject();

		Object object1 = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = object1 != null ? (Users) object1 : null;

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null) {
			json.put("Task", task);
			for (Apply apply : task.getTasApplies()) {
				if (apply.getAppBeUser().getUseId().intValue() == user
						.getUseId().intValue()) {// 自己的申请
					if (apply.getAppState() == 0)
						json.put("State", "0");// 申请中
					if (apply.getAppState() == 1
							&& apply.getAppTask().getTasState() == 1)
						json.put("State", "1");// 通过申请，但任务需要人数未达齐，仍是申请中（团队任务）
					task.setAppId(apply.getAppId());
				}
			}
			// 去掉json多余参数
			((JSONObject) json.get("TaskList")).remove("tasApplies");
		}
		return "success";
	}

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

	public JSONObject getJson() {
		return json;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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
