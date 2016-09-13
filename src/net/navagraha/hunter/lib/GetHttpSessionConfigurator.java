package net.navagraha.hunter.lib;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/*
 * 获取HttpSession
 * 
 */

public class GetHttpSessionConfigurator extends Configurator {

	@Override
	public void modifyHandshake(ServerEndpointConfig sec,
			HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession != null)// 经测试，浏览器（即管理员）登录，httpsession为空，故不操作其httpsession（ws与httpSession浏览器登录问题①）
			sec.getUserProperties().put(HttpSession.class.getName(),
					httpSession);

	}
}