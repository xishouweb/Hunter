package net.navagraha.hunter.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.navagraha.hunter.lib.Big2Small4PicUtil;
import net.navagraha.hunter.pojo.Users;
import net.navagraha.hunter.server.ObjectDao;
import net.navagraha.hunter.server.impl.ObjectDaoImpl;

import org.apache.struts2.ServletActionContext;

public class FileUploadTool {
	private File file;
	private String fileFileName;
	private int isUser;

	private String code;

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
		String str = sdf.format(date);
		try {
			// 用时间戳命名图片
			fileFileName = str
					+ fileFileName.substring(fileFileName.lastIndexOf("."));
			is = new FileInputStream(file);
			if (isUser == 1) {// 头像
				// 保存头像
				ObjectDao objectDao = new ObjectDaoImpl();
				Object object = ServletActionContext.getRequest().getSession()
						.getAttribute("Users");// 将登陆用户取出
				Users user = object != null ? (Users) object : null;
				fileFileName = user.getUseSno() + fileFileName;
				if (user != null) {

					user.setUseImg("upload/" + fileFileName);
					objectDao.update(user);

					// 得到图片保存的位置(根据root来得到图片保存的路径在Tomcat下的该工程里)
					File destFile = new File(root, fileFileName);

					// 把图片写入到上面设置的路径里
					os = new FileOutputStream(destFile);
					byte[] buffer = new byte[400];
					int length = 0;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					}
					os.close();
					setCode("1");
				} else
					setCode("0");
				is.close();
			} else {// 不是头像
				ServletActionContext.getRequest().getSession()
						.setAttribute("ImgPath", "upload/" + fileFileName);

				// 得到图片保存的位置(根据root来得到图片保存的路径在Tomcat下的该工程里)
				File destFile = new File(root, fileFileName);

				// 把图片写入到上面设置的路径里
				os = new FileOutputStream(destFile);
				byte[] buffer = new byte[400];
				int length = 0;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				os.close();
				is.close();

				// 生成缩略图
				Big2Small4PicUtil.transform(destFile.getPath(), root
						+ "/small/" + fileFileName, 40, 40);
				setCode("1");
			}
		} catch (Exception e) {
			setCode("0");
		}
		return "success";
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setIsUser(int isUser) {
		this.isUser = isUser;
	}

}
