package net.navagraha.hunter.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.navagraha.hunter.lib.PropertyUtil;
import net.navagraha.hunter.pojo.Apply;
import net.navagraha.hunter.pojo.Money;
import net.navagraha.hunter.pojo.Pay;
import net.navagraha.hunter.pojo.Power;
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
		// TODO 投入使用时取消注释
		// runable = new MyRunable();
		// Thread thread = new Thread(runable);
		// thread.start();
	}

}

class MyRunable implements Runnable {

	private static ObjectDao objectDao = new ObjectDaoImpl();
	private static PropertyUtil propertyUtil = new PropertyUtil(
			"Cons.properties");
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
			try {
				Thread.sleep(CHECK_SECOND);// 一分钟检查一次
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 获取当前时间的N天前
	private static String getDateFromNow(int afterDay) {
		GregorianCalendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - afterDay);
		date = calendar.getTime();

		return df.format(date);
	}

	// 超过时间失效
	private void doDB4Set() {
		String sysTime = new Date().toLocaleString();
		String hql = "where tasTimeout<='" + sysTime
				+ "' and tasState in (0,1,2)";
		List<?> list = objectDao.getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			System.err.println("超过时间失效：本次设置" + list.size() + "条任务失失败");
			for (Object object : list) {
				Task task = (Task) object;
				if (task.getTasState() < 2 || task.getTasType().equals("加急个人")) {// 到规定时间都没有人接受任务或者任务为加急任务
					task.setTasState(5);// 任务失败
					objectDao.update(task);
					Users user = task.getTasUser();
					// 返钱
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
					objectDao.save(money);

					/** 能力 **/
					Set<Apply> set = task.getTasApplies();
					for (Iterator<Apply> iterator = set.iterator(); iterator
							.hasNext();) {
						Apply apply = (Apply) iterator.next();
						Users beUser = apply.getAppBeUser();
						// 获取任务接收者的power
						List<?> list3 = objectDao.getObjectListByfield("Power",
								"powUser", beUser.getUseId());
						Power power;

						if (list3.size() > 0) {
							power = (Power) list3.get(0);
							power.setPowCredit(power.getPowCredit() - 4);// 任务过期失败，接受者默认减少2点信誉值
						} else {
							power = new Power();
							power.setPowCredit(50 - 4);
							power.setPowUser(beUser);
						}
						objectDao.saveOrUpdate(power);
					}
					/** 能力结束 **/

					/** 支付日志 **/
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
					Pay payOut;

					// 获取任务发布者的pay
					List<?> list1 = objectDao.getObjectListByfield("Pay",
							new String[] { "payTime", "payUser" },
							new Object[] { sdf.format(new Date()),
									user.getUseId() });

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
					objectDao.saveOrUpdate(payOut);
					/** 支付日志结束 **/

					return;
				}
				task.setTasState(6);
				objectDao.update(task);
			}
		}
	}

	// 超过时间打钱
	private void doDB4Money() {

		String oldTime = getDateFromNow(EXPIRE_DAY);
		String hql = "where tasState=3 and tasFinishtime<='" + oldTime + "'";
		List<?> list = objectDao.getObjectListBycond("Task", hql);
		if (list.size() > 0) {
			System.err.println("超过时间打钱：本次给" + list.size() + "个用户自动打钱");
			for (Object object : list) {
				Task task = (Task) object;
				Set<Apply> set = task.getTasApplies();
				for (Iterator<Apply> iterator = set.iterator(); iterator
						.hasNext();) {
					Apply apply = (Apply) iterator.next();
					if (apply.getAppState() == 1) {// 为该任务的实际接受者
						Users user = apply.getAppBeUser();
						// 打钱
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
						objectDao.save(money);

						/** 支付日志 **/
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
						Pay payOut, payIn;

						// 获取任务发布者的pay
						List<?> list1 = objectDao.getObjectListByfield("Pay",
								new String[] { "payTime", "payUser" },
								new Object[] { sdf.format(new Date()),
										task.getTasUser().getUseId() });

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
						objectDao.saveOrUpdate(payOut);

						// 获取任务收人者的pay
						List<?> list2 = objectDao.getObjectListByfield("Pay",
								new String[] { "payTime", "payUser" },
								new Object[] { sdf.format(new Date()),
										user.getUseId() });

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
						objectDao.saveOrUpdate(payIn);
						/** 支付日志结束 **/

					}

				}
				task.setTasState(4);// 任务成功
				objectDao.update(task);
			}
		}
	}
}
