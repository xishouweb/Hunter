package net.navagraha.hunter.lib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 本类实现一个对 图像文件进行缩放处理的方法 (实例化该类将获取更多功能)<br>
 * 即给定一个图像文件，可以生成一个该 图像 文件的缩影图像文件 <br>
 */
public class Big2Small4PicUtil {
	// 对象是否己经初始化
	private boolean isInitFlag = false;

	// 定义生目标图片的宽度和高度，给齐一个就可以了
	private int targetPicWidth = 0;
	private int targetPicHeight = 0;

	// 定义目标图片的相比原图片的比例
	private double picScale = 0;

	/**
	 * 实例化该类将获取transform(srcFileName, targetFileName)方法
	 * 
	 * 实现步骤：<br>
	 * 
	 * JPGTransformer jpg = new JPGTransformer(); <br>
	 * 
	 * * 提供三种生成缩影图像的方法：<br>
	 * 1 、设置缩影文件的宽度，根据设置的宽度和源图像文件的大小来确定新缩影文件的长度来生成缩影图像 <br>
	 * 2 、设置缩影文件的长度，根据设置的长度和源图像文件的大小来确定新缩影文件的宽度来生成缩影图像<br>
	 * 3 、设置缩影文件相对于源图像文件的比例大小，根据源图像文件的大小及设置的比例来确定新缩影文<br>
	 * 件的大小来生成缩影图像，新生成的缩影图像可以比原图像大，这时即是放大源图像。<br>
	 * 即以下三种方式均可实现图片的缩放 <br>
	 * // 将原图片缩小一半 <br>
	 * jpg.setPicScale(0.5); <br>
	 * //设置目标图片宽度 <br>
	 * jpg.setSmallWidth(20); <br>
	 * //设置目标图片高度 <br>
	 * jpg.setSmallHeigth(10); <br>
	 * 
	 * String srcFileName =
	 * "C:/Users/Administrator/Pictures/TestPic/test3.jpeg"; <br>
	 * <br>
	 * String targetFileName = StringUtil.getClassPath(JPGTransformer.class) <br>
	 * + "File/" + DateHandleUtil.getNowForFileName(-1, ".jpeg"); <br>
	 * <br>
	 * jpg.transform(srcFileName, targetFileName); <br>
	 */
	public Big2Small4PicUtil() {
		this.isInitFlag = false;
	}

	/**
	 * 重置JPG图片缩放器
	 */
	private void resetJPGTransformer() {
		this.picScale = 0;
		this.targetPicWidth = 0;
		this.targetPicHeight = 0;
		this.isInitFlag = false;
	}

	/**
	 * 设置目标图片相对于源图片的缩放比例<br>
	 * <br>
	 * 
	 * @param scale
	 *            缩放比例 <br>
	 * @throws JPGException
	 * <br>
	 */
	public void setPicScale(double scale) throws JPGException {
		if (scale <= 0) {
			throw new JPGException(" 缩放比例不能为0和负数！ ");
		}

		this.resetJPGTransformer();
		this.picScale = scale;
		this.isInitFlag = true;
	}

	/**
	 * 设置目标图片的宽度
	 * 
	 * @param width
	 * @throws JPGException
	 */
	public void SetSmallWidth(int width) throws JPGException {
		if (width <= 0) {
			throw new JPGException(" 缩影图片的宽度不能为 0 和负数！ ");
		}

		this.resetJPGTransformer();
		this.targetPicWidth = width;
		this.isInitFlag = true;
	}

	/**
	 * 设置目标图片的高度
	 * 
	 * @param height
	 * @throws JPGException
	 */
	public void SetSmallHeight(int height) throws JPGException {
		if (height <= 0) {
			throw new JPGException(" 缩影图片的高度不能为 0 和负数！ ");
		}

		this.resetJPGTransformer();
		this.targetPicHeight = height;
		this.isInitFlag = true;
	}

	/**
	 * 开始缩放图片
	 * 
	 * @param srcPicFileName
	 *            源图片的文件名
	 * @param targetPicFileName
	 *            生成目标图片的文件名
	 * @throws JPGException
	 */
	@SuppressWarnings("deprecation")
	public void transform(String srcPicFileName, String targetPicFileName)
			throws JPGException {

		if (!this.isInitFlag) {
			throw new JPGException(" 对象参数没有初始化！ ");
		}
		if (srcPicFileName == null || targetPicFileName == null) {
			throw new JPGException(" 包含文件名的路径为空！ ");
		}
		if ((!srcPicFileName.toLowerCase().endsWith("jpg"))
				&& (!srcPicFileName.toLowerCase().endsWith("jpeg"))) {
			throw new JPGException(" 只能处理 JPG/JPEG 文件！ ");
		}
		if ((!targetPicFileName.toLowerCase().endsWith("jpg"))
				&& !targetPicFileName.toLowerCase().endsWith("jpeg")) {
			throw new JPGException(" 只能处理 JPG/JPEG 文件！ ");
		}

		// 新建源图片和生成图片的文件对象
		File fin = new File(srcPicFileName);
		File fout = new File(targetPicFileName);

		// 通过缓冲读入源图片文件
		BufferedImage bSrc = null;
		try {
			// 读取文件生成BufferedImage
			bSrc = ImageIO.read(fin);
		} catch (IOException ex) {
			throw new JPGException(" 读取源图像文件出错！ ");
		}
		// 源图片的宽度和高度
		int srcW = bSrc.getWidth();
		int srcH = bSrc.getHeight();

		// 设置目标图片的实际宽度和高度
		int targetW = 0;
		int targetH = 0;
		if (this.targetPicWidth != 0) {
			// 根据设定的宽度求出长度
			targetW = this.targetPicWidth;
			targetH = (targetW * srcH) / srcW;
		} else if (this.targetPicHeight != 0) {
			// 根据设定的长度求出宽度
			targetH = this.targetPicHeight;
			targetW = (targetH * srcW) / srcH;
		} else if (this.picScale != 0) {
			// 根据设置的缩放比例设置图像的长和宽
			targetW = (int) ((float) srcW * this.picScale);
			targetH = (int) ((float) srcH * this.picScale);
		} else {
			throw new JPGException(" 对象参数初始化不正确！ ");
		}

		System.out.println(" 源图片的分辨率： " + srcW + "×" + srcH);
		System.out.println(" 目标图片的分辨率： " + targetW + "×" + targetH);
		System.out.println("目标位置：" + URLDecoder.decode(targetPicFileName));
		// 目标图像的缓冲对象
		BufferedImage bTarget = new BufferedImage(targetW, targetH,
				BufferedImage.TYPE_3BYTE_BGR);

		// 求得目标图片与源图片宽度、高度的比例。
		double sx = (double) targetW / srcW;
		double sy = (double) targetH / srcH;

		// 构造图像变换对象
		AffineTransform transform = new AffineTransform();
		// 设置图像转换的比例
		transform.setToScale(sx, sy);

		// 构造图像转换操作对象
		AffineTransformOp ato = new AffineTransformOp(transform, null);
		// 实现转换，将bSrc转换成bTarget
		ato.filter(bSrc, bTarget);

		// 输出目标图片
		try {
			// 将目标图片的BufferedImage写到文件中去，jpeg为图片的格式
			ImageIO.write(bTarget, "jpeg", fout);
		} catch (IOException ex1) {
			throw new JPGException(" 写入缩略图像文件出错！ ");
		}
	}

	/**
	 * 
	 * 功能：生成缩略图
	 * 
	 * @param originalFile
	 *            原始图片位置
	 * @param thumbnailFile
	 *            缩略图位置
	 * @param thumbWidth
	 *            缩略图宽
	 * @param thumbHeight
	 *            缩略图高
	 * @throws Exception
	 * @since 1.0
	 */
	public static void transform(String originalFile, String thumbnailFile,
			int thumbWidth, int thumbHeight) throws Exception {
		Image image = javax.imageio.ImageIO.read(new File(originalFile));

		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double imageRatio = (double) imageWidth / (double) imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}

		if (imageWidth < thumbWidth && imageHeight < thumbHeight) {
			thumbWidth = imageWidth;
			thumbHeight = imageHeight;
		} else if (imageWidth < thumbWidth)
			thumbWidth = imageWidth;
		else if (imageHeight < thumbHeight)
			thumbHeight = imageHeight;

		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setBackground(Color.WHITE);
		graphics2D.setPaint(Color.WHITE);
		graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

		javax.imageio.ImageIO.write(thumbImage, "JPG", new File(thumbnailFile));
	}

	/**
	 * 
	 * 功能：生成缩略图
	 * 
	 * @param photoPath
	 *            原图片路径
	 * @param smallPath
	 *            缩略图路径
	 * @param rectangle
	 *            0索引存放缩略图宽，1索引存放缩略图高
	 * @since 1.0
	 */
	public static void CreateSmallPhoto(String photoPath, String smallPath,
			int[] rectangle) {
		File _file = new File(photoPath); // 读入文件
		Image src;
		try {
			src = javax.imageio.ImageIO.read(_file);
			int wideth = rectangle[0]; // 图宽，一般110
			int height = rectangle[1]; // 图长，一般80
			BufferedImage tag = new BufferedImage(wideth, height,
					BufferedImage.TYPE_INT_RGB);
			tag.getGraphics().drawImage(src, 0, 0, wideth, height, null); // 绘制缩小后的图
			FileOutputStream out = new FileOutputStream(smallPath); // 输出到文件流
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(tag); // 近JPEG编码
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * JPG缩放时可能出现的异常
	 */
	private class JPGException extends Exception {
		public JPGException(String msg) {
			super(msg);
		}
	}

}
