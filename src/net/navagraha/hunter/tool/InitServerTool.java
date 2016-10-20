package net.navagraha.hunter.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Census;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;

public class InitServerTool implements ServletContextListener {
	private MyRunable runable;
	private ScheduledFuture<?> timer;

	public void contextDestroyed(ServletContextEvent sce) {
		if (runable != null)// 关闭服务器，停止线程
			runable.run = false;
		if (timer != null)
			timer.cancel(true);// 取消webSocket心跳测试
	}

	public void contextInitialized(ServletContextEvent sce) {
		runable = new MyRunable();
		Thread thread = new Thread(runable);
		thread.start();// 开启24小时监测线程
		timer = JoinPushTool.startHeartBeat();// 开启webSocket心跳测试
	}

}

class MyRunable implements Runnable {

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"cons.properties");
	private static int EXPIRE_DAY;// 过期时间
	private static int CHECK_SECOND;// 每隔多少秒检查一次
	private static double SUCCESS_TAX;// 成功服务费
	private static double FALSE_TAX;// 失败服务费
	static {
		EXPIRE_DAY = Integer.parseInt(propertyUtil
				.getPropertyValue("EXPIRE_DAY"));
		CHECK_SECOND = Integer.parseInt(propertyUtil
				.getPropertyValue("CHECK_SECOND"));
		SUCCESS_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("SUCCESS_TAX"));
		FALSE_TAX = Double.parseDouble(propertyUtil
				.getPropertyValue("FALSE_TAX"));
	}

	public boolean run = true;

	public void run() {
		while (run) {
			doDB4Set();
			doDB4Money();
			do4User();
			try {
				Thread.sleep(CHECK_SECOND);// 默认60秒检查一次
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	// 每天统计当天激活总人数和登录总人数
	private void do4User() {
		String ruleTime = "23:58";// 在凌晨进行统计
		String sysTime = new SimpleDateFormat("HH:mm").format(new Date());
		if (ruleTime.equals(sysTime)) {
			// /** 获取活跃用户前三名进行奖励 */
			// List<?> list = giveDao().getSomeObjectListBycond(
			// "from Tag order by tagTimeout desc", 3);
			// for (Object object : list) {
			// Tag tag = (Tag) object;
			// Users user = tag.getTagUser();
			// // 发放奖励
			// Money money = new Money();
			// money.setMonAlipay(user.getUseAlipay());
			// money.setMonComment("/");
			// money.setMonName(user.getUseName());
			// money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
			// .format(new Date())
			// + user.getUseSno().substring(
			// user.getUseSno().length() - 4));
			// money.setMonPay(50.0);
			// money.setMonPhone(user.getUsePhone());
			// money.setMonState(0);// 提现
			// money.setMonType("【内测活动】在线时长前三名");
			// money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
			// .format(new Date()));
			// giveDao().save(money);
			// }
			// /** 奖励结束 */

			/** 设置当天统计 */
			// 统计当天激活人数
			String time[] = getDateBeforeNow(0, "yyyy-MM,dd").split(",");
			String month = time[0];
			int day = Integer.parseInt(time[1]);

			// 获取激活总人数
			Object obj1 = giveDao().getObjectSizeBycond(
					"select count(*) from Users where useIscompany in(0,1)");
			int activeTotal = obj1 != null ? (Integer) obj1 : 0;

			// 获取当天登录总人数
			Object obj2 = giveDao().getObjectSizeBycond(
					"select count(*) from Users where useIslogin=1");
			int loginNum = obj2 != null ? (Integer) obj2 : 0;
			giveDao().executeUpdate(
					"update Users set useIslogin=0 where useIslogin=1");// 初始化当天登录标识

			// giveDao().executeUpdate(
			// "update Users set useIsonline=0 where useIsonline=1");//
			// 初始化当天登录状态

			// 获取昨天激活总人数
			String oldTime[] = getDateBeforeNow(1, "yyyy-MM,dd").split(",");
			int oldactiveTotal = giveDao().getObjectSizeBycond(
					"select cenActivetotal from Census where cenMonth='"
							+ oldTime[0] + "' and cenDay=" + oldTime[1]);

			List<?> li = giveDao().getObjectListBycond(
					"from Census where cenMonth='" + month + "' and cenDay="
							+ day);
			Census census;
			if (li.size() < 1) {
				census = new Census();
				census.setCenMonth(month);
				census.setCenDay(day);
				census.setCenOnlinenum(0);
				census.setCenLoginnum(0);
			} else
				census = (Census) li.get(0);
			census.setCenLoginnum(loginNum);
			census.setCenActivetotal(activeTotal);
			census.setCenActivenum(activeTotal - oldactiveTotal);
			giveDao().saveOrUpdate(census);
			// System.err.println(census.getcenloginn);
			/** 设置当天统计结束 */

		}
	}

	// 超过时间失败
	private void doDB4Set() {
		String sysTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		String hql = "where tasTimeout<='" + sysTime
				+ "' and tasState in (0,1,2)";
		List<?> list = giveDao().getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			// System.out.println("【超过时间失效】：本次设置" + list.size() + "条任务状态为失败");
			for (Object object : list) {
				Task task = (Task) object;
				if (task.getTasState() < 2 || task.getTasType().equals("加急个人")) {// 到规定时间都没有人接受任务或者任务为加急任务
					task.setTasState(5);// 任务失败
					if (task.getTasType().equals("加急个人"))
						task.setTasEvaluate("加急任务已过时限，导致任务失败");
					else
						task.setTasEvaluate("任务已过平台显示时间且无人接受，导致任务失败");
					giveDao().update(task);

					Users user = task.getTasUser();
					user.setUseRemain(user.getUseRemain() + task.getTasPrice()
							* (1 - FALSE_TAX));// 存入余额
					giveDao().update(user);// 操作了存入httpsession的user对象的useRemain属性，故拉取user的useRemain时应从数据库拉取
					// System.out.println("余额：" + user.getUseRemain());

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
					money.setMonType("【超时任务】返金");
					money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
							.format(new Date()));
					giveDao().save(money);
					/** 返钱记录结束 */

					/** 能力 **/
					Set<Apply> set = task.getTasApplies();
					for (Apply apply : set) {

						if (apply.getAppState() == 1) {// 任务的真实接受者才进行扣信誉
							apply.setAppState(4);// 任务失败
							giveDao().update(apply);

							Users beUser = apply.getAppBeUser();
							// 获取任务接收者的power
							List<?> list3 = giveDao().getObjectListByfield(
									"Power", "powUser", beUser);
							Power power;

							if (list3.size() > 0) {
								power = (Power) list3.get(0);
								power.setPowCredit(power.getPowCredit() - 4);// 任务过期失败，接受者默认减少4点信誉值
							} else {
								power = new Power();
								power.setPowCredit(50 - 4);
								power.setPowUser(beUser);
								power.setPowFast(0);
							}
							giveDao().saveOrUpdate(power);

							// 消息推送接受者
							String phone = apply.getAppBeUser().getUsePhone();
							if (JoinPushTool.getConnections()
									.containsKey(phone))
								JoinPushTool.broadcast(
										"15" + task.getTasTitle(), phone);
						}

					}
					/** 能力结束 **/

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
						payOut.setPayOut(payOut.getPayOut()
								+ task.getTasPrice() * FALSE_TAX);
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
					if (JoinPushTool.getConnections().containsKey(phone))
						JoinPushTool
								.broadcast("04" + task.getTasTitle(), phone);

				} else {
					task.setTasState(6);
					giveDao().update(task);
				}
			}
		}
	}

	// 超过时间未通过任务自动打钱
	private void doDB4Money() {

		String oldTime = getDateBeforeNow(EXPIRE_DAY, "yyyy-MM-dd HH:mm:ss");
		String hql = "where tasState=3 and tasFinishtime<='" + oldTime + "'";
		List<?> list = giveDao().getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			System.out.println("【超过时间未通过任务自动打钱】：本次给" + list.size() + "个用户自动打钱");
			for (Object object : list) {
				Task task = (Task) object;
				Set<Apply> set = task.getTasApplies();

				/** 支付日志 **/
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				Pay payOut;

				// 获取任务发布者的pay
				List<?> list1 = giveDao().getObjectListByfield(
						"Pay",
						new String[] { "payTime", "payUser" },
						new Object[] { sdf.format(new Date()),
								task.getTasUser() });

				if (list1.size() > 0) {
					// 支出
					payOut = (Pay) list1.get(0);
					payOut.setPayOut(payOut.getPayOut() + task.getTasPrice()
							* 1.0);
				} else {
					// 支出
					payOut = new Pay();
					payOut.setPayTime(sdf.format(new Date()));
					payOut.setPayIn(0.0);
					payOut.setPayOut(task.getTasPrice() * 1.0);
					payOut.setPayUser(task.getTasUser());
				}
				giveDao().saveOrUpdate(payOut);
				/** 支付日志结束 **/

				for (Apply apply : set) {

					if (apply.getAppState() == 1) {// 为该任务的实际接受者
						apply.setAppState(3);// 任务成功
						giveDao().update(apply);

						Users user = apply.getAppBeUser();
						user.setUseRemain(user.getUseRemain()
								+ task.getTasPrice() * (1 - SUCCESS_TAX)
								/ set.size());// 存入余额
						giveDao().update(user); // 操作了存入httpsession的user对象的useRemain属性，故拉取user的useRemain时应从数据库拉取

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
						money.setMonType("【超时未通过任务】赏金");
						money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
								.format(new Date()));
						giveDao().save(money);
						/** 打钱记录结束 */

						/** 支付日志 **/
						// 获取任务收人者的pay
						Pay payIn;
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

						// 消息推送接受者
						String phone = apply.getAppBeUser().getUsePhone();
						if (JoinPushTool.getConnections().containsKey(phone))
							JoinPushTool.broadcast("14" + task.getTasTitle(),
									phone);
					}
				}
				task.setTasState(4);// 任务成功
				task.setTasEvaluate("任务进入审核阶段两天，任务自动完成");
				giveDao().update(task);

				// 消息推送发布者
				String phone = task.getTasUser().getUsePhone();
				if (JoinPushTool.getConnections().containsKey(phone))
					JoinPushTool.broadcast("03" + task.getTasTitle(), phone);
			}
		}
	}

	/** 获取当前时间的N天前 */
	private static String getDateBeforeNow(int beforeDay, String sfd) {
		GregorianCalendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();

		SimpleDateFormat df = new SimpleDateFormat(sfd);

		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - beforeDay);
		date = calendar.getTime();

		return df.format(date);
	}

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

}
