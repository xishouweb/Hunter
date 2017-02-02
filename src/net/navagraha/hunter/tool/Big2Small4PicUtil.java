package net.navagraha.hunter.tool;

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

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 功能描述：实现一个对图像文件进行缩放处理 (实例化该类将获取更多功能)<br>
 * 即给定一个图像文件，可以生成一个该图像文件的缩影图像 <br>
 * 
 * @author 冉椿林
 * 
 * @since 1.0
 */
public class Big2Small4PicUtil {
	// 对象是否己经初始化
	private boolean bIsInitFlag = false;

	// 定义生目标图片的宽度和高度，给齐一个就可以了
	private int iTargetPicWidth = 0;
	private int iTargetPicHeight = 0;

	// 定义目标图片的相比原图片的比例
	private double dPicScale = 0;

	/**
	 * 实例化该类将获得transform(srcFileName, targetFileName)方法
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
		this.bIsInitFlag = false;
	}

	/**
	 * 重置JPG图片缩放器
	 */
	private void resetJPGTransformer() {
		this.dPicScale = 0;
		this.iTargetPicWidth = 0;
		this.iTargetPicHeight = 0;
		this.bIsInitFlag = false;
	}

	/**
	 * 设置目标图片相对于源图片的缩放比例<br>
	 * <br>
	 * 
	 * @param _scale
	 *            缩放比例 <br>
	 * @throws JPGException
	 * <br>
	 */
	public void setPicScale(double _scale) throws JPGException {
		if (_scale <= 0) {
			throw new JPGException(" 缩放比例不能为0和负数！ ");
		}

		this.resetJPGTransformer();
		this.dPicScale = _scale;
		this.bIsInitFlag = true;
	}

	/**
	 * 设置目标图片的宽度
	 * 
	 * @param _width
	 * @throws JPGException
	 */
	public void SetSmallWidth(int _width) throws JPGException {
		if (_width <= 0) {
			throw new JPGException(" 缩影图片的宽度不能为 0 和负数！ ");
		}

		this.resetJPGTransformer();
		this.iTargetPicWidth = _width;
		this.bIsInitFlag = true;
	}

	/**
	 * 设置目标图片的高度
	 * 
	 * @param _height
	 * @throws JPGException
	 */
	public void SetSmallHeight(int _height) throws JPGException {
		if (_height <= 0) {
			throw new JPGException(" 缩影图片的高度不能为 0 和负数！ ");
		}

		this.resetJPGTransformer();
		this.iTargetPicHeight = _height;
		this.bIsInitFlag = true;
	}

	/**
	 * 开始缩放图片
	 * 
	 * @param _srcPicFileName
	 *            源图片的文件名
	 * @param _targetPicFileName
	 *            生成目标图片的文件名
	 * @throws JPGException
	 */
	public void transform(String _srcPicFileName, String _targetPicFileName)
			throws JPGException {

		if (!this.bIsInitFlag) {
			throw new JPGException(" 对象参数没有初始化！ ");
		}
		if (_srcPicFileName == null || _targetPicFileName == null) {
			throw new JPGException(" 包含文件名的路径为空！ ");
		}
		if ((!_srcPicFileName.toLowerCase().endsWith("jpg"))
				&& (!_srcPicFileName.toLowerCase().endsWith("jpeg"))) {
			throw new JPGException(" 只能处理 JPG/JPEG 文件！ ");
		}
		if ((!_targetPicFileName.toLowerCase().endsWith("jpg"))
				&& !_targetPicFileName.toLowerCase().endsWith("jpeg")) {
			throw new JPGException(" 只能处理 JPG/JPEG 文件！ ");
		}

		// 新建源图片和生成图片的文件对象
		File fin = new File(_srcPicFileName);
		File fout = new File(_targetPicFileName);

		// 通过缓冲读入源图片文件
		BufferedImage bSrc = null;
		try {
			// 读取文件生成BufferedImage
			bSrc = ImageIO.read(fin);
		} catch (IOException ex) {
			throw new JPGException(" 读取源图像文件出错！ ");
		}
		// 源图片的宽度和高度
		int iSrcW = bSrc.getWidth();
		int iSrcH = bSrc.getHeight();

		// 设置目标图片的实际宽度和高度
		int iTtargetW = 0;
		int iTtargetH = 0;
		if (this.iTargetPicWidth != 0) {
			// 根据设定的宽度求出长度
			iTtargetW = this.iTargetPicWidth;
			iTtargetH = (iTtargetW * iSrcH) / iSrcW;
		} else if (this.iTargetPicHeight != 0) {
			// 根据设定的长度求出宽度
			iTtargetH = this.iTargetPicHeight;
			iTtargetW = (iTtargetH * iSrcW) / iSrcH;
		} else if (this.dPicScale != 0) {
			// 根据设置的缩放比例设置图像的长和宽
			iTtargetW = (int) ((float) iSrcW * this.dPicScale);
			iTtargetH = (int) ((float) iSrcH * this.dPicScale);
		} else {
			throw new JPGException(" 对象参数初始化不正确！ ");
		}

		// System.out.println(" 源图片的分辨率： " + iSrcW + "×" + iSrcH);
		// System.out.println(" 目标图片的分辨率： " + iTtargetW + "×" + iTtargetH);
		// System.out.println("目标位置：" + URLDecoder.decode(_targetPicFileName));
		// 目标图像的缓冲对象
		BufferedImage bTarget = new BufferedImage(iTtargetW, iTtargetH,
				BufferedImage.TYPE_3BYTE_BGR);

		// 求得目标图片与源图片宽度、高度的比例。
		double dSx = (double) iTtargetW / iSrcW;
		double dSy = (double) iTtargetH / iSrcH;

		// 构造图像变换对象
		AffineTransform transform = new AffineTransform();
		// 设置图像转换的比例
		transform.setToScale(dSx, dSy);

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
	 * @param _originalFile
	 *            原始图片位置
	 * @param _thumbnailFile
	 *            缩略图位置
	 * @param _thumbWidth
	 *            缩略图宽
	 * @param _thumbHeight
	 *            缩略图高
	 * @throws Exception
	 * @since 1.0
	 */
	public static void transform(String _originalFile, String _thumbnailFile,
			int _thumbWidth, int _thumbHeight) throws Exception {
		Image image = javax.imageio.ImageIO.read(new File(_originalFile));

		double dThumbRatio = (double) _thumbWidth / (double) _thumbHeight;
		int iImageWidth = image.getWidth(null);
		int iImageHeight = image.getHeight(null);
		double dImageRatio = (double) iImageWidth / (double) iImageHeight;
		if (dThumbRatio < dImageRatio) {
			_thumbHeight = (int) (_thumbWidth / dImageRatio);
		} else {
			_thumbWidth = (int) (_thumbHeight * dImageRatio);
		}

		if (iImageWidth < _thumbWidth && iImageHeight < _thumbHeight) {
			_thumbWidth = iImageWidth;
			_thumbHeight = iImageHeight;
		} else if (iImageWidth < _thumbWidth)
			_thumbWidth = iImageWidth;
		else if (iImageHeight < _thumbHeight)
			_thumbHeight = iImageHeight;

		BufferedImage thumbImage = new BufferedImage(_thumbWidth, _thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setBackground(Color.WHITE);
		graphics2D.setPaint(Color.WHITE);
		graphics2D.fillRect(0, 0, _thumbWidth, _thumbHeight);
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, _thumbWidth, _thumbHeight, null);

		javax.imageio.ImageIO
				.write(thumbImage, "JPG", new File(_thumbnailFile));
	}

	/**
	 * 
	 * 功能：生成缩略图
	 * 
	 * @param _photoPath
	 *            原图片路径
	 * @param _smallPath
	 *            缩略图路径
	 * @param _rectangle
	 *            0索引存放缩略图宽，1索引存放缩略图高
	 * @since 1.0
	 */
	public static void CreateSmallPhoto(String _photoPath, String _smallPath,
			int[] _rectangle) {
		File _file = new File(_photoPath); // 读入文件
		Image src;
		try {
			src = javax.imageio.ImageIO.read(_file);
			int iWideth = _rectangle[0]; // 图宽，一般110
			int iHeight = _rectangle[1]; // 图长，一般80
			BufferedImage tag = new BufferedImage(iWideth, iHeight,
					BufferedImage.TYPE_INT_RGB);
			tag.getGraphics().drawImage(src, 0, 0, iWideth, iHeight, null); // 绘制缩小后的图
			FileOutputStream out = new FileOutputStream(_smallPath); // 输出到文件流
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
		public JPGException(String _msg) {
			super(_msg);
		}
	}

}
