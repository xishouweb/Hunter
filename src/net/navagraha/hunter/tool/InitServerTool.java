package net.navagraha.hunter.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Census;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Power;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Task;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;

public class InitServerTool implements ServletContextListener {
	private MyRunable runable;

	public void contextDestroyed(ServletContextEvent sce) {
		if (runable != null)// 关闭服务器，停止线程
			runable.run = false;
	}

	public void contextInitialized(ServletContextEvent sce) {
		runable = new MyRunable();
		Thread thread = new Thread(runable);
		thread.start();
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

	// 每天获取活跃用户前三名进行奖励，并且统计当天激活总人数和登录总人数
	private void do4User() {
		String ruleTime = "23:59";// 在凌晨进行统计，活跃前三进行奖励发送
		String sysTime = new SimpleDateFormat("HH:mm").format(new Date());
		if (ruleTime.equals(sysTime)) {
			/** 获取前三进行处理 */
			List<?> list = objectDao.getSomeObjectListBycond(
					"from Tag order by tagTimeout desc", 3);
			for (Object object : list) {
				Tag tag = (Tag) object;
				Users user = tag.getTagUser();
				// 发放奖励
				Money money = new Money();
				money.setMonAlipay(user.getUseAlipay());
				money.setMonComment("");
				money.setMonName(user.getUseName());
				money.setMonNo(new SimpleDateFormat("yyyyMMddHHmmssSSS")
						.format(new Date())
						+ user.getUseSno().substring(
								user.getUseSno().length() - 4));
				money.setMonPay(50.0);
				money.setMonState(0);// 未打钱
				money.setMonType("特殊");
				money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
						.format(new Date()));
				objectDao.save(money);
			}
			/** 处理结束 */

			/** 设置当天统计 */
			// 统计当天激活人数
			String time[] = getDateBeforeNow(0, "yyyy-MM,dd").split(",");
			String month = time[0];
			int day = Integer.parseInt(time[1]);

			// 获取激活总人数
			Object obj1 = objectDao
					.getObjectSizeBycond("select count(*) from Users where useIscompany in(0,1)");
			int activeTotal = obj1 != null ? (Integer) obj1 : 0;

			// 获取当天登录总人数
			Object obj2 = objectDao
					.getObjectSizeBycond("select count(*) from Users where useIslogin=1");
			int loginNum = obj2 != null ? (Integer) obj2 : 0;
			objectDao
					.executeUpdate("update Users set useIslogin=0 where useIslogin=1");// 初始化当天登录状态

			// 获取昨天激活总人数
			String oldTime[] = getDateBeforeNow(1, "yyyy-MM,dd").split(",");
			int oldactiveTotal = objectDao
					.getObjectSizeBycond("select cenActivetotal from Census where cenMonth='"
							+ oldTime[0] + "' and cenDay=" + oldTime[1]);

			List<?> li = objectDao
					.getObjectListBycond("from Census where cenMonth='" + month
							+ "' and cenDay=" + day);
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
			objectDao.saveOrUpdate(census);
			/** 设置当天统计结束 */

		}
	}

	// 超过时间失效
	private void doDB4Set() {
		String sysTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		String hql = "where tasTimeout<='" + sysTime
				+ "' and tasState in (0,1,2)";
		List<?> list = objectDao.getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			System.out.println("【超过时间失效】：本次设置" + list.size() + "条任务状态为失败");
			for (Object object : list) {
				Task task = (Task) object;
				if (task.getTasState() < 2 || task.getTasType().equals("加急个人")) {// 到规定时间都没有人接受任务或者任务为加急任务
					task.setTasState(5);// 任务失败
					objectDao.update(task);
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
					money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
							.format(new Date()));
					objectDao.save(money);
					/** 返钱结束 */

					/** 能力 **/
					Set<Apply> set = task.getTasApplies();
					for (Apply apply : set) {

						if (apply.getAppState() == 1) {// 任务的真实接受者才进行扣信誉
							apply.setAppState(4);// 任务失败
							objectDao.update(apply);

							Users beUser = apply.getAppBeUser();
							// 获取任务接收者的power
							List<?> list3 = objectDao.getObjectListByfield(
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
							objectDao.saveOrUpdate(power);
						}
					}
					/** 能力结束 **/

					/** 支付日志 **/
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					Pay payOut;

					// 获取任务发布者的pay
					List<?> list1 = objectDao.getObjectListByfield("Pay",
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
					objectDao.saveOrUpdate(payOut);
					/** 支付日志结束 **/

				} else {
					task.setTasState(6);
					objectDao.update(task);
				}
			}
		}
	}

	// 超过时间未通过任务自动打钱
	private void doDB4Money() {

		String oldTime = getDateBeforeNow(EXPIRE_DAY, "yyyy-MM-dd HH:mm:ss");
		String hql = "where tasState=3 and tasFinishtime<='" + oldTime + "'";
		List<?> list = objectDao.getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			System.out.println("【超过时间未通过任务自动打钱】：本次给" + list.size() + "个用户自动打钱");
			for (Object object : list) {
				Task task = (Task) object;
				Set<Apply> set = task.getTasApplies();

				/** 支付日志 **/
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				Pay payOut;

				// 获取任务发布者的pay
				List<?> list1 = objectDao.getObjectListByfield("Pay",
						new String[] { "payTime", "payUser" }, new Object[] {
								sdf.format(new Date()), task.getTasUser() });

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
				objectDao.saveOrUpdate(payOut);
				/** 支付日志结束 **/

				for (Apply apply : set) {

					if (apply.getAppState() == 1) {// 为该任务的实际接受者
						apply.setAppState(3);// 任务成功
						objectDao.update(apply);

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
						money.setMonPay(task.getTasPrice() * (1 - SUCCESS_TAX)
								/ set.size());
						money.setMonState(0);// 未打钱
						if (task.getTasUser().getUseIscompany() == 1) {
							money.setMonType("特殊");
						} else
							money.setMonType(task.getTasType());
						money.setMonTime(new SimpleDateFormat("yyyy-MM-dd")
								.format(new Date()));
						objectDao.save(money);
						/** 打钱结束 */

						/** 支付日志 **/
						// 获取任务收人者的pay
						Pay payIn;
						List<?> list2 = objectDao.getObjectListByfield("Pay",
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
						objectDao.saveOrUpdate(payIn);
						/** 支付日志结束 **/

					}
				}
				task.setTasState(4);// 任务成功
				objectDao.update(task);
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

}
