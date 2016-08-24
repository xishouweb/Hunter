package net.navagraha.hunter.lib;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;

/*
 * 调试小工具-Applet(显示来自Html提交的参数和参数值)
 */

public class Alin_JAppletDubugUtil extends JApplet {
	private static final long serialVersionUID = 1L;

	public String[] string = null;

	@Override
	public void init() {

		JLabel j = new JLabel("获取接收的参数：");
		j.setBounds(5, 5, 200, 30);
		add(j);
		String[] str = new String[string.length + 1];
		for (int k = 0; k < string.length; k++) {
			str[k] = string[k];
		}
		str[string.length] = " ";
		int i = 0, x = 30, y = 40;

		for (String string : str) {

			if (i % 2 == 0 && i < str.length - 1) {
				JLabel jLabel;
				if (i == str.length - 2)
					jLabel = new JLabel(string);
				else
					jLabel = new JLabel(string + "   :");

				jLabel.setBounds(x, y, 70, 30);
				add(jLabel);
				x += 50;
			}

			if (i % 2 != 0 && i < str.length - 1) {
				JLabel jLabel = new JLabel("          " + string);
				jLabel.setBounds(x, y, 100, 30);
				add(jLabel);
				y += 50;
				x -= 50;
			}

			i++;
		}

	}

	// 用main方法运行JApplet
	public static void main(String[] args) {

		final JFrame frame = new JFrame("Web调试小工具byAlin(显示接收参数)：");
		final Alin_JAppletDubugUtil jAppletDubugUtil = new Alin_JAppletDubugUtil();
		// main方法里创建一个框架来放置applet，applet单独运行时，
		// 要完成操作必须手动调用init和start方法
		frame.add(jAppletDubugUtil, BorderLayout.CENTER);
		jAppletDubugUtil.string = args;
		jAppletDubugUtil.init();

		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 400);
		frame.setLocation(750, 200);
		frame.setVisible(true);

		jAppletDubugUtil.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				System.out.println("自动关闭Web调试小工具");
				frame.dispose();
			}
		});

	}

	// 调用main方法,一般可用在过滤器里面
	/*
	 * if (request.getParameterNames().hasMoreElements()) { final MyJApplet
	 * myJApplet = new MyJApplet(); Thread thread = new Thread() {
	 * 
	 * @SuppressWarnings( { "unchecked", "static-access" }) public void run() {
	 * 
	 * List<String> list = new ArrayList<String>(); int i = 0;
	 * Enumeration<String> enu = request.getParameterNames(); while
	 * (enu.hasMoreElements()) { list.add(enu.nextElement().toString()); try {
	 * list .add(new String(request.getParameter(
	 * list.get(i)).getBytes("iso8859-1"), "utf-8")); } catch
	 * (UnsupportedEncodingException e) { e.printStackTrace(); } i++; } String[]
	 * str = (String[]) list.toArray(new String[list .size()]);
	 * 
	 * myJApplet.main(str); }; }; thread.start(); }
	 */
}
