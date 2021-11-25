package com.github.javlock.pase.jface.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ImagesUtils {
	private static String byteArray2Hex(final byte[] hash) {
		@SuppressWarnings("resource")
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public static String computeFileSHA1(File file) throws IOException {
		String sha1 = null;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			throw new IOException("Impossible to get SHA-1 digester", e1);
		}
		try (InputStream input = new FileInputStream(file);
				DigestInputStream digestStream = new DigestInputStream(input, digest)) {
			while (digestStream.read() != -1) {
				// read file stream without buffer
			}
			MessageDigest msgDigest = digestStream.getMessageDigest();
			sha1 = new HexBinaryAdapter().marshal(msgDigest.digest());
		}
		return sha1;

	}

	/**
	 * @param userSpaceImage
	 * @return byte array of supplied image
	 */
	public static byte[] getByteData(BufferedImage userSpaceImage) {
		WritableRaster raster = userSpaceImage.getRaster();
		DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
		return buffer.getData();
	}

	public static BufferedImage Mat2BufferedImage(Mat m) {
		// Method converts a Mat to a Buffered Image
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	public static Mat matify(BufferedImage sourceImg) {
		if (sourceImg == null) {
			return null;
		}
		long millis = System.currentTimeMillis();
		DataBuffer dataBuffer = sourceImg.getRaster().getDataBuffer();
		byte[] imgPixels = null;
		Mat imgMat = null;

		int width = sourceImg.getWidth();
		int height = sourceImg.getHeight();

		if (dataBuffer instanceof DataBufferByte) {
			imgPixels = ((DataBufferByte) dataBuffer).getData();
		}

		if (dataBuffer instanceof DataBufferInt) {

			int byteSize = width * height;
			imgPixels = new byte[byteSize * 3];

			int[] imgIntegerPixels = ((DataBufferInt) dataBuffer).getData();

			for (int p = 0; p < byteSize; p++) {
				imgPixels[p * 3 + 0] = (byte) ((imgIntegerPixels[p] & 0x00FF0000) >> 16);
				imgPixels[p * 3 + 1] = (byte) ((imgIntegerPixels[p] & 0x0000FF00) >> 8);
				imgPixels[p * 3 + 2] = (byte) (imgIntegerPixels[p] & 0x000000FF);
			}
		}

		if (imgPixels != null) {
			imgMat = new Mat(height, width, CvType.CV_8UC3);
			imgMat.put(0, 0, imgPixels);
		}

		// System.out.println("matify exec millis: " + (System.currentTimeMillis() -
		// millis));
		return imgMat;
	}

	public static BufferedImage resize(BufferedImage img, int height, int width) {
		Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height,
				// BufferedImage.TYPE_INT_ARGB
				BufferedImage.TYPE_3BYTE_BGR
		//
		);
		Graphics2D g2d = resized.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return resized;
	}

	public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return byteArray2Hex(md.digest(convertme));
	}

}
