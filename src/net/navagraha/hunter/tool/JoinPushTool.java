package net.navagraha.hunter.tool;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import net.navagraha.hunter.lib.GetHttpSessionConfigurator;
import net.navagraha.hunter.pojo.About;
import net.navagraha.hunter.pojo.Tag;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;

/**
 * 功能：建立连接-前后台调用OnOpen,前后台可调用OnClose进行关闭,都会引发对方的OnClose方法
 * 异常退出说明：前台页面关闭，先调用OnError,再调用Close；后台服务器关闭，前台只调用Close
 */
@ServerEndpoint(value = "/websocket/{phone}/{version}", configurator = GetHttpSessionConfigurator.class)
public class JoinPushTool {

	private static Map<String, Session> connections = new ConcurrentHashMap<String, Session>();

	private static ObjectDao objectDao = new ObjectDaoImpl();

	private HttpSession httpSession;

	/** 获取Dao */
	public ObjectDao giveDao() {
		if (objectDao == null)
			objectDao = new ObjectDaoImpl();
		return objectDao;
	}

	/**
	 * @return the connections
	 */
	public static Map<String, Session> getConnections() {
		// 重新检查connections是否有已关闭的连接，如存在就移除掉
		for (String key : connections.keySet()) {
			try {
				connections.get(key).getBasicRemote().sendText("");
			} catch (Exception e) {
				e.printStackTrace();
				connections.remove(key);
			}
		}
		return connections;
	}

	@OnOpen
	public void OnOpen(@PathParam("phone") String phone,
			@PathParam("version") String version, Session session,
			EndpointConfig config) {// 建立连接

		connections.put(phone, session);// 加入集合

		httpSession = (HttpSession) config.getUserProperties().get(
				HttpSession.class.getName());// 浏览器登录httpSession为空的（ws与httpSession浏览器登录问题②）
		List<?> list = giveDao().getObjectListBycond("About",
				"order by aboId desc limit 1");// 取得当前版本
		if (!version.equals("x.x.x")) {// 非管理员
			if (list.size() > 0 && version != null) {
				if (!version.equals(((About) list.get(0)).getAboVersion())) {// 不是最新版本，需要更新
					broadcast("更新", phone);
				}
			}
		} else
			broadcast("当前APP版本为" + ((About) list.get(0)).getAboVersion()
					+ "，请实时关注！", "01010000000");// 推送管理员

		if (phone.equals("01010000000")) { // 管理员登录
			session.setMaxIdleTimeout(20 * 60 * 1000);// 毫秒 空闲20分钟断开 默认0，表示永远不断开
			session.setMaxTextMessageBufferSize(1600);// 大约可发100个汉字 最大字符缓冲区长度
														// （默认8192Bit=1024Byte=1024个字母或数字=512个汉字）
			session.setMaxBinaryMessageBufferSize(3200);// 最大字节缓冲区长度
		} else {
			session.setMaxTextMessageBufferSize(8);// 只能发送一个字节
			session.setMaxBinaryMessageBufferSize(8);
		}

		// 测试httpsession是否是地址引用还是值引用
		// new Thread(new Runnable() {
		//
		// public void run() {
		// while (true) {
		// Object object2 = httpSession.getAttribute("Users");
		// System.out.println("用户姓名:"
		// + ((Users) object2).getUseNickname() + "  用户余额:"
		// + ((Users) object2).getUseRemain());
		// try {
		// Thread.sleep(1000);// 默认1秒检查一次
		// } catch (InterruptedException e) {
		// System.err.println(e.getMessage());
		// }
		// }
		// }
		// }).start();

	}

	@OnClose
	public void OnClose(@PathParam("phone") String phone) {
		System.out.println("close");
		connections.remove(phone);// 移除集合
		if (!phone.equals("01010000000"))// 非浏览器退出才调用quit，否则，浏览器退出，是没有httpsession的，会出现异常（ws与httpSession浏览器登录问题③）
			quit();// 用户注销
	}

	@OnMessage
	public void OnMessage(String message) {// 前台发生消息（请不要信任终端）
		broadcast(message, null);
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		System.out.println("error");
		t.printStackTrace();
	}

	// 用户注销
	public void quit() {

		// 计算登录时长
		Object object1;
		try {
			object1 = httpSession.getAttribute("Logtime");
		} catch (Exception e) {
			return;
		}
		long diff = 0;

		if (object1 != null) {
			String Logtime = object1.toString();
			diff = getMinutesBetween(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()),
					Logtime);
		}

		// 将登录时长加到数据库
		Object object2 = httpSession.getAttribute("Users");
		Users user = object2 != null ? (Users) object2 : null;

		if (user != null) {
			List<?> list = giveDao().getObjectListByfield("Tag", "tagUser",
					user);
			if (list.size() > 0) {
				Tag tag = (Tag) list.get(0);
				tag.setTagTimeout(tag.getTagTimeout()
						+ Integer.valueOf("" + diff));
				giveDao().update(tag);
			}
			// 设置用户在线状态
			user.setUseIsonline(0);
			giveDao().update(user);
		}
		httpSession.invalidate();// 清空session
	}

	/**
	 * 功能：推送
	 * 
	 * @param msg
	 * @param phone
	 *            为空时进行全局推送
	 */
	public static void broadcast(String msg, String phone) {// 进行消息推送
		if (phone != null) {
			Session session = null;
			try {
				session = connections.get(phone);
				synchronized (session) {
					if (session.isOpen())
						session.getBasicRemote().sendText(msg);
					else
						connections.remove(phone);
				}
			} catch (IOException e) {
				System.err
						.println("WebSocket Send Failed in Send message to client");
				connections.remove(phone);
				try {
					session.close();
				} catch (IOException e1) {
					// 忽略
				}
			}
		} else {
			for (String key : connections.keySet()) {
				Session session = null;
				try {
					session = connections.get(key);
					synchronized (session) {
						if (session.isOpen())
							session.getBasicRemote().sendText(msg);
						else
							connections.remove(key);
					}
				} catch (IOException e) {
					System.err
							.println("WebSocket Send Failed in Send message to client");
					connections.remove(key);
					try {
						session.close();
					} catch (IOException e1) {
						// 忽略
					}
				}
			}
		}
	}

	/**
	 * 功能： 计算两个时间的差，单位：分
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
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

}
