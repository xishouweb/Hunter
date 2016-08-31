package net.navagraha.hunter.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	private Integer tasPrice;
	private String tasType;
	private String tasState;
	private String tasTimeout;
	private Integer tasRulenum;
	private String tasEvaluate;
	private Integer tasCredit;

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");// 初始化参数配置文件
	private static int HOME_PerPageRow;
	private static int LOG_PerPageRow;
	private static int PERSON_PerPageRow;
	private static int RecMoney;
	private static int PubMoney;
	private static double SUCCESS_TAX;// 成功服务费
	private static double FALSE_TAX;// 失败服务费
	static {
		HOME_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("HOME_PerPageRow"));// 任务大厅每页显示任务数量
		LOG_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("LOG_PerPageRow"));// 任务日志每页显示任务数量
		PERSON_PerPageRow = Integer.parseInt(propertyUtil
				.getPropertyValue("PERSON_PerPageRow"));// 任务日志每页显示任务数量
		RecMoney = Integer.parseInt(propertyUtil.getPropertyValue("RecMoney"));// 任务日志每页显示任务数量
		PubMoney = Integer.parseInt(propertyUtil.getPropertyValue("PubMoney"));// 任务日志每页显示任务数量
		SUCCESS_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("SUCCESS_TAX"));
		FALSE_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("FALSE_TAX"));
	}

	// 前台传入
	private Integer curPage;
	private Integer appId;

	private String appReason;

	// 反馈到前台

	public String code;

	public static JSONObject json = new JSONObject();

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	// 发布任务
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
				ServletActionContext.getRequest().getSession().setAttribute(
						"ImgPath", null);
			}
			if (tasType.equals("加急个人")) {// 加急个人任务
				task.setTasTimeout(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date).substring(
								0,
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(date).indexOf(" "))
						+ " " + tasTimeout + ":00");
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
			}

		}

		if (task != null) {
			try {
				user.setUsePublishnum(user.getUsePublishnum() + 1);

				/** 打钱 */
				if (user.getUsePublishnum() % PubMoney == 0) {// 用户发布任务每达到规定数，进行奖励
					Money money = new Money();
					money.setMonAlipay(user.getUseAlipay());
					money.setMonComment("用户接受任务数达到规定数，进行奖励");
					money.setMonName(user.getUseName());
					money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
							.format(new Date())
							+ user.getUseSno().substring(
									user.getUseSno().length() - 4));
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("青铜"))
						money.setMonPay(8.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("白银"))
						money.setMonPay(15.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("黄金"))
						money.setMonPay(23.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("铂金"))
						money.setMonPay(30.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("钻石"))
						money.setMonPay(37.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("超凡"))
						money.setMonPay(43.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("王者"))
						money.setMonPay(50.0);

					money.setMonState(0);// 未打钱
					money.setMonType("特殊");// 平台奖励属于特殊
					money.setMonTime(new SimpleDateFormat("yyyyMMdd")
							.format(new Date()));
					giveDao().save(money);
				}
				/** 打钱结束 */

				task.setTasUser(user);
				giveDao().update(user);
				giveDao().save(task);
				setCode("1");
			} catch (Exception e) {
				setCode("0");
			}
		}

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
			for (Apply apply : task.getTasApplies()) {
				if (apply.getAppBeUser().getUseId().intValue() == user
						.getUseId().intValue()) {// 已申请,不必重复申请
					setCode("19");
					return "success";
				}
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
				if (task.getTasType().equals("个人加急"))
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
		if (apply != null && apply.getAppState() == 0) {
			giveDao().delete(apply);
			setCode("1");// 操作成功
			return "success";
		} else {
			setCode("13");// 申请记录不存在
			return "success";
		}

	}

	// 用户审核通过申请(非加急)
	public String checkSuccessTask() {
		Object object = giveDao().getObjectById(Apply.class, appId);
		Apply apply = object != null ? (Apply) object : null;

		if (apply != null) {
			Task task = apply.getAppTask();
			if (task != null && task.getTasState() == 1) {
				// 其他申请自动设置为不通过
				List<?> tasList = giveDao().getObjectListByfield("Apply",
						"appTask", task);
				for (Object object2 : tasList) {
					Apply apply2 = (Apply) object2;
					if (apply2.getAppId() != appId) {
						apply2.setAppState(2);// 不通过
						giveDao().update(apply2);
					}
				}

				// 本申请通过
				Users user = apply.getAppBeUser();
				user.setUseAcceptnum(user.getUseAcceptnum() + 1);

				/** 打钱 */
				if (user.getUseAcceptnum() % RecMoney == 0) {// 用户接受任务每达到规定数，进行奖励
					Money money = new Money();
					money.setMonAlipay(user.getUseAlipay());
					money.setMonComment("用户接受任务数达到规定数，进行奖励");
					money.setMonName(user.getUseName());
					money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
							.format(new Date())
							+ user.getUseSno().substring(
									user.getUseSno().length() - 4));
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("青铜"))
						money.setMonPay(8.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("白银"))
						money.setMonPay(15.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("黄金"))
						money.setMonPay(23.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("铂金"))
						money.setMonPay(30.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("钻石"))
						money.setMonPay(37.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("超凡"))
						money.setMonPay(43.0);
					if (PowerAction.givePowerByUseId(user.getUseId())
							.substring(0, 2).equals("王者"))
						money.setMonPay(50.0);

					money.setMonState(0);// 未打钱
					money.setMonType("特殊");// 平台奖励属于特殊
					money.setMonTime(new SimpleDateFormat("yyyyMMdd")
							.format(new Date()));
					giveDao().save(money);
				}
				/** 打钱结束 */

				giveDao().update(user);

				if (task.getTasType().equals("个人")) {

					apply.setAppState(1);// 通过
					giveDao().update(apply);

					task.setTasState(2);// 审核成功,任务进行中
					Set<Apply> set = new HashSet<Apply>(0);
					set.add(apply);
					task.setTasApplies(set);
					giveDao().update(task);

					setCode("1");// 操作成功
					return "success";
				} else {
					if (task.getTasReceivenum() == task.getTasRulenum() - 1) {// 任务人数已达齐，开始进行
						apply.setAppState(1);// 通过
						giveDao().update(apply);

						task.setTasState(2);// 审核成功,任务进行中
						Set<Apply> set = task.getTasApplies();
						set.add(apply);
						task.setTasApplies(set);
						giveDao().update(task);

						setCode("1");// 操作成功
						return "success";
					} else if (task.getTasReceivenum() < task.getTasRulenum() - 1) {// 任务人数未达齐
						apply.setAppState(1);// 通过
						giveDao().update(apply);

						task.setTasState(1);// 审核成功,任务未进行
						Set<Apply> set = task.getTasApplies();
						set = set.size() < 1 ? new HashSet<Apply>(0) : set;
						set.add(apply);
						task.setTasApplies(set);
						giveDao().update(task);

						setCode("1");// 操作成功
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

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;

		if (task != null) {
			if (!task.getTasType().equals("团队")) {

				if (task.getTasState() == 5) {
					setCode("15");// 任务已失败
					return "success";
				} else {
					if (task.getTasState() == 3) {
						setCode("14");// 已点击了申请完成，不必重新点击申请完成
						return "success";
					}
					task.setTasFinishtime(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(new Date()));
					task.setTasState(3);// 提交审核
					giveDao().update(task);
					PhoneCodeTool.send(task.getTasUser().getUsePhone(), task
							.getTasTitle(), "task");// 短信提示发布者任务已完成
					setCode("1");// 操作成功
					return "success";
				}
			} else {// 团队任务
				if (task.getTasFinishnum() == task.getTasRulenum() - 1) {// 任务人数已达齐，开始申请审核

					task.setTasFinishtime(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(new Date()));
					task.setTasFinishnum(task.getTasRulenum());
					task.setTasState(3);// 提交审核
					giveDao().update(task);
					PhoneCodeTool.send(task.getTasUser().getUsePhone(), task
							.getTasTitle(), "task");// 短信提示发布者任务已完成
					setCode("1");// 操作成功
					return "success";
				}
				if (task.getTasReceivenum() < task.getTasRulenum() - 1) {// 任务人数未达齐

					task.setTasFinishnum(task.getTasFinishnum() + 1);
					task.setTasState(2);// 任务仍是进行中
					giveDao().update(task);
					setCode("1");// 操作成功
					return "success";
				} else {
					setCode("14");// 规定人数都点击了申请完成，不必重新点击申请完成
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

				for (Iterator<Apply> iterator = set.iterator(); iterator
						.hasNext();) {
					Apply apply = (Apply) iterator.next();

					if (apply.getAppState() == 1) {// 任务的真实接受者
						apply.setAppState(3);// 任务成功
						giveDao().update(apply);

						Users user = apply.getAppBeUser();

						/** 打钱 */
						Money money = new Money();
						money.setMonAlipay(user.getUseAlipay());
						money.setMonComment("");
						money.setMonName(user.getUseName());
						money
								.setMonNo(new SimpleDateFormat(
										"yyyyMMddHHmmssSSS").format(new Date())
										+ user.getUseSno().substring(
												user.getUseSno().length() - 4));
						money.setMonPay(task.getTasPrice() * (1 - SUCCESS_TAX));
						money.setMonState(0);// 未打钱
						if (task.getTasUser().getUseIscompany() == 1) {
							money.setMonType("特殊");
						} else
							money.setMonType(task.getTasType());
						money.setMonTime(new SimpleDateFormat("yyyyMMdd")
								.format(new Date()));
						giveDao().save(money);
						/** 打钱结束 */

						/** 能力 **/
						// 获取任务接收者的power
						List<?> list3 = giveDao().getObjectListByfield("Power",
								"powUser", user);
						Power power;
						if (tasCredit != null && list3.size() > 0) {

							power = (Power) list3.get(0);
							power
									.setPowCredit(power.getPowCredit()
											+ tasCredit);
							if (tasCredit > 3)
								power.setPowFast(power.getPowFast() + 1);
							giveDao().update(power);
						}
						/** 能力结束 **/

						/** 支付日志 **/
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
						Pay payOut, payIn;

						// 获取任务发布者的pay
						List<?> list1 = giveDao().getObjectListByfield(
								"Pay",
								new String[] { "payTime", "payUser" },
								new Object[] { sdf.format(new Date()),
										task.getTasUser() });

						if (list1.size() > 0) {
							// 支出者
							payOut = (Pay) list1.get(0);
							payOut.setPayOut(payOut.getPayOut()
									+ task.getTasPrice() + 0.0);
						} else {
							// 支出者
							payOut = new Pay();
							payOut.setPayTime(sdf.format(new Date()));
							payOut.setPayIn(0.0);
							payOut.setPayOut(task.getTasPrice() + 0.0);
							payOut.setPayUser(task.getTasUser());
						}
						giveDao().saveOrUpdate(payOut);

						// 获取任务收入者的pay
						List<?> list2 = giveDao().getObjectListByfield("Pay",
								new String[] { "payTime", "payUser" },
								new Object[] { sdf.format(new Date()), user });

						if (list2.size() > 0) {
							// 收入者
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
							// 收入者
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
				}
				task.setTasState(4);// 任务成功
				task.setTasEvaluate(tasEvaluate);
				task.setTasCredit(tasCredit);
				giveDao().update(task);

				setCode("1");// 操作成功
				return "success";
			} else {
				setCode("16");// 接受者不存在
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
				for (Iterator<Apply> iterator = set.iterator(); iterator
						.hasNext();) {
					Apply apply = (Apply) iterator.next();

					if (apply.getAppState() == 1) {// 任务的真实接受者
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
							giveDao().update(power);
						}
					}
				}
				/** 能力结束 **/

				if (task.getTasState() == 6) {// 任务已失效，即失败
					task.setTasState(5);// 任务失败
					giveDao().update(task);
					Users user = task.getTasUser();

					/** 返钱 */
					Money money = new Money();
					money.setMonAlipay(user.getUseAlipay());
					money.setMonComment("");
					money.setMonName(user.getUseName());
					money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
							.format(new Date())
							+ user.getUseSno().substring(
									user.getUseSno().length() - 4));
					money.setMonPay(task.getTasPrice() * (1 - FALSE_TAX));
					money.setMonState(0);// 未打钱
					if (task.getTasUser().getUseIscompany() == 1) {
						money.setMonType("特殊");
					} else
						money.setMonType(task.getTasType());
					money.setMonTime(new SimpleDateFormat("yyyyMMdd")
							.format(new Date()));
					giveDao().save(money);
					/** 返钱结束 */

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

		List<?> list = giveDao().pageListWithCond(
				"Task",
				curPage,
				HOME_PerPageRow,
				"where tasType='" + tasType
						+ "' and  tasState in (0,1) order by tasTime desc");
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
			int size = giveDao().getObjectSizeBycond(
					"select count(*) from Task where tasType='" + tasType
							+ "' and  tasState in (0,1)");
			json.put("size", size);
		}

		return "success";
	}

	// 获取经过发布时间倒序排序的所有特殊任务（任务大厅分页）
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

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		json = new JSONObject();

		String cond = "where appBeUser=" + userId;
		if (tasState.equals("0"))// 显示失败和申请不通过任务
			cond += " and appState in (2,4)" + " order by appId desc";
		if (tasState.equals("1"))// 显示成功任务
			cond += " and appState=3" + " order by appId desc";
		if (tasState.equals("2"))// 显示进行中任务
			cond += " and appState=1" + " order by appId desc";
		if (tasState.equals("3"))// 显示申请中任务
			cond += " and appState=0" + " order by appId desc";

		List<?> list = giveDao().pageListWithCond("Apply", curPage,
				LOG_PerPageRow, cond);
		List<Task> taskList = new ArrayList<Task>();
		for (Object object : list) {
			taskList.add(((Apply) object).getAppTask());
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

		// 获取本用户ID
		Object obj = ServletActionContext.getRequest().getSession()
				.getAttribute("Users");// 将登陆用户取出
		Users user = obj != null ? (Users) obj : null;
		int userId = user != null ? user.getUseId() : 0;// 如为0，则反馈到前台的json为空，即获取失败

		json = new JSONObject();

		String cond = "where tasUser=" + userId;
		if (tasState.equals("0"))// 显示失败任务
			cond += " and tasState=5" + " order by tasTime desc";
		if (tasState.equals("1"))// 显示成功任务
			cond += " and tasState=4" + " order by tasTime desc";
		if (tasState.equals("2"))// 显示已发布、审核中、进行中和失效任务
			cond += " and tasState in (0,2,3,6)" + " order by tasTime desc";
		if (tasState.equals("3"))// 显示申请中任务
			cond += " and tasState=1" + " order by tasTime desc";

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

			String cond = "where appTask=" + tasId + " and appState in(1,3,4)";
			List<?> list = giveDao().getObjectListBycond("Apply", cond);

			JSONArray jArray = new JSONArray();
			for (Object object1 : list)
				jArray.add(((Apply) object1).getAppBeUser());
			json.put("UserList", jArray);
		}

		return "success";
	}

	// 通过任务Id获取任务
	public String giveTaskById() {

		json = new JSONObject();

		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		if (task != null)
			json.put("Task", task);
		// 去掉json多余参数
		((JSONObject) json.get("TaskList")).remove("tasApplies");

		return "success";
	}

	// 通过任务获取任务发布者
	public String giveTasUserByTasId() {
		Object object = giveDao().getObjectById(Task.class, tasId);
		Task task = object != null ? (Task) object : null;
		json = new JSONObject();
		json.put("User", task.getTasUser());
		return "success";
	}

	// Property accessors

	public void setTasId(Integer tasId) {
		this.tasId = tasId;
	}

	public void setTasContent(String tasContent) {
		this.tasContent = tasContent;
	}

	public void setTasPrice(Integer tasPrice) {
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

}
