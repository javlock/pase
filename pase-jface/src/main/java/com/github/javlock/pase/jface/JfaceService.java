package com.github.javlock.pase.jface;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.Optional;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.jface.JFaceAnswer.RectObj;
import com.github.javlock.pase.jface.JFaceAnswer.RectObj.FacePoint;
import com.github.javlock.pase.jface.utils.ImagesUtils;
import com.github.javlock.pase.libs.utils.HashUtils;

import lombok.Getter;
import lombok.Setter;

public class JfaceService {

	private static final Logger LOGGER = LoggerFactory.getLogger("JfaceService");
	private JFace jFace;
	private @Getter @Setter CascadeClassifier csc;
	String cscXmlFile = "/usr/local/share/opencv4/haarcascades/haarcascade_frontalface_alt2.xml";

	Logger logger = LoggerFactory.getLogger(getClass());

	public JfaceService(JFace jFace) {
		this.jFace = jFace;
	}

	public Optional<JFaceAnswer> appendFile(File from) {

		try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(from.getAbsolutePath());
				Java2DFrameConverter jFrameConverter = new Java2DFrameConverter()) {
			frameGrabber.start();
			JFaceAnswer answer = new JFaceAnswer();

			int count = 0;
			while (true) {
				Frame image = frameGrabber.grabImage();

				BufferedImage bufImage = jFrameConverter.convert(image);
				Mat src = ImagesUtils.matify(bufImage);
				if (src == null) {
					LOGGER.info("пустой");
					break;
				}
				// next
				Mat cutSrc = src.clone();
				Mat recSrc = src.clone();

				byte[] recSrcbytes = ImagesUtils.getByteData(ImagesUtils.Mat2BufferedImage(recSrc));
				String recSrcSHA256 = HashUtils.toHexString(HashUtils.getSHA(recSrcbytes));

				RectObj rectObj = new RectObj();
				rectObj.setSha256(recSrcSHA256);
				answer.getRects().putIfAbsent(recSrcSHA256, rectObj);

				MatOfRect objectsTo = new MatOfRect();
				getCsc().detectMultiScale(src, objectsTo);// DETECT
				Rect[] array = objectsTo.toArray();
				if (array.length > 0) {
					LOGGER.info("Detected faces: {}", array.length);
					for (int i = 0; i < array.length; i++) {
						Rect origRect = array[i];
						Rect cutRect = origRect.clone();

						int x = cutRect.x;
						int y = cutRect.y;
						int xw = x + cutRect.width;
						int yh = y + cutRect.height;

						LOGGER.info("{} {} {} {}", x, y, xw, yh);

						Rect roi = new Rect(new Point(x, y), new Point(xw, yh));
						Mat submat = cutSrc.submat(roi);

						FacePoint facePoint = new FacePoint(x, y, xw, yh);
						rectObj.getFaces().add(facePoint);
						count++;
					}
				}

			}

			if (count > 0) {
				answer.setSha256(HashUtils.toHexString(HashUtils.getSHA(Files.readAllBytes(from.toPath()))));
				logger.info("answ:sha256:{}", answer.getSha256());
				for (Entry<String, RectObj> entry : answer.getRects().entrySet()) {
					String key = entry.getKey();
					RectObj val = entry.getValue();
					String valSha256 = val.getSha256();

					logger.info("RectObj:sha256:{} correct:{}", key, (key == valSha256));

					for (FacePoint facePoint : val.getFaces()) {
						LOGGER.info("FacePoint:{}", facePoint);
					}
				}
				return Optional.of(answer);
			}
			//

		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}

	public void appendFile(File from, File toDir) {

		try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(from.getAbsolutePath());
				Java2DFrameConverter jFrameConverter = new Java2DFrameConverter()) {
			frameGrabber.start();
			Optional<JFaceAnswer> resOptional = appendFile(from);
			if (resOptional.isPresent()) {
				JFaceAnswer res = resOptional.get();
				logger.warn("TODO:1");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void init() {
		String faceInit = "faceInit";
		String faceInited = "faceInited";

		logger.info(faceInit);
		initLib();
		initClassifiers();
		logger.info(faceInited);
	}

	public void initClassifiers() {
		setCsc(new CascadeClassifier(cscXmlFile));
	}

	private void initLib() {
		String libInit = "initLib";
		logger.info(libInit);

		long first = System.currentTimeMillis();

		Loader.load(opencv_java.class);

		long last = System.currentTimeMillis();
		String libInited = "initLib on " + ((last - first) / 1000) + " seconds";

		logger.info(libInited);
	}

}
