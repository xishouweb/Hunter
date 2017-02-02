package net.navagraha.hunter.tool;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * 功能描述：手动设置HttpSession
 * 
 * @author 冉椿林
 */
public class GetHttpSessionConfigurator extends Configurator {

	@Override
	public void modifyHandshake(ServerEndpointConfig sec,
			HandshakeRequest request, HandshakeResponse response) {

		HttpSession httpSession = (HttpSession) request.getHttpSession();

		if (httpSession != null)// 经测试在整合ws的情况下，浏览器（即管理员）登录后的httpsession为空，故手动设置httpsession到UserProperties（ws与httpSession浏览器登录问题①）
			sec.getUserProperties().put(HttpSession.class.getName(),
					httpSession);
	}
}