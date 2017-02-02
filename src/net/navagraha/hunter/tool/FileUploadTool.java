package net.navagraha.hunter.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.navagraha.hunter.dao.ObjectDao;
import net.navagraha.hunter.dao.ObjectDaoImpl;
import net.navagraha.hunter.pojo.Users;

import org.apache.struts2.ServletActionContext;

/**
 * 功能描述：实现上传图片功能
 * 
 * @author 冉椿林
 *
 * @since 1.0
 */
public class FileUploadTool {

	private File file;
	private String sFileFileName;
	private int iIsUser;

	private String sCode;

	// 上传图片
	public String uploadImg() throws IOException {
		// 得到工程保存图片的路径
		@SuppressWarnings("deprecation")
		String root = ServletActionContext.getRequest().getRealPath("/upload");

		InputStream is;
		OutputStream os;
		// 获取时间戳
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String sStr = sdf.format(date);
		try {
			// 用时间戳命名图片
			sFileFileName = sStr
					+ sFileFileName.substring(sFileFileName.lastIndexOf("."));
			is = new FileInputStream(file);
			if (iIsUser == 1) {// 头像
				// 保存头像
				ObjectDao objectDao = new ObjectDaoImpl();
				Object object = ServletActionContext.getRequest().getSession()
						.getAttribute("Users");// 将登陆用户取出
				Users user = object != null ? (Users) object : null;
				sFileFileName = user.getUseSno() + sFileFileName;
				if (user != null) {

					user.setUseImg("upload/" + sFileFileName);
					objectDao.update(user);

					// 得到图片保存的位置(根据root来得到图片保存的路径在Tomcat下的该工程里)
					File destFile = new File(root, sFileFileName);

					// 把图片写入到上面设置的路径里
					os = new FileOutputStream(destFile);
					byte[] buffer = new byte[400];
					int iLength = 0;
					while ((iLength = is.read(buffer)) > 0) {
						os.write(buffer, 0, iLength);
					}
					os.close();
					setsCode("1");
				} else
					setsCode("0");
				is.close();
			} else {// 不是头像
				ServletActionContext.getRequest().getSession()
						.setAttribute("ImgPath", "upload/" + sFileFileName);

				// 得到图片保存的位置(根据root来得到图片保存的路径在Tomcat下的该工程里)
				File destFile = new File(root, sFileFileName);

				// 把图片写入到上面设置的路径里
				os = new FileOutputStream(destFile);
				byte[] buffer = new byte[400];
				int iLength = 0;
				while ((iLength = is.read(buffer)) > 0) {
					os.write(buffer, 0, iLength);
				}
				os.close();
				is.close();

				// 生成缩略图
				Big2Small4PicUtil.transform(destFile.getPath(), root
						+ "/small/" + sFileFileName, 40, 40);
				setsCode("1");
			}
		} catch (Exception e) {
			setsCode("0");
		}
		return "success";
	}

	// ////////////////////////////////////////////////////////////////////////////////
	/* setter、getter方法 */
	public void setFile(File file) {
		this.file = file;
	}

	public void setsFileFileName(String sFileFileName) {
		this.sFileFileName = sFileFileName;
	}

	public void setsCode(String sCode) {
		this.sCode = sCode;
	}

	public String getsCode() {
		return sCode;
	}

	public void setiIsUser(int iIsUser) {
		this.iIsUser = iIsUser;
	}

}
