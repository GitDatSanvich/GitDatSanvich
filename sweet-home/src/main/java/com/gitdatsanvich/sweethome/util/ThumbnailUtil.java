package com.gitdatsanvich.sweethome.util;

import com.gitdatsanvich.common.constants.FileConstants;
import com.gitdatsanvich.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangChen
 * @date 2021/4/28 11:21
 */
@Slf4j
@Component
public class ThumbnailUtil {

    private static final String THUMBNAIL_SUFFIX = "png";
    /*针对每一像素的光感度*/
    private static final int BLACK_RATE = 50;
    /*针对一张图片的黑色像素百分比*/
    private static final int AVERAGE = 10;

    private static final AtomicInteger WORKING_NUM = new AtomicInteger(0);

    private static final int MAX_NUM = 3;

    /**
     * @param file file
     * @param type type
     * @return 缩略图路径
     * @throws BizException BizException
     */
    public String getThumbnail(MultipartFile file, String type) throws BizException {
        long start = System.currentTimeMillis();
        String thumbnailUrl = null;
        try {
            InputStream inputStream = file.getInputStream();
            /*视频缩略图*/
            if (FileConstants.VIDEO.equals(type)) {
                thumbnailUrl = getVideoThumbnail(inputStream);
            }
            long end = System.currentTimeMillis();
            log.info("缩略图生成时间为" + (end - start));
            return thumbnailUrl;
        } catch (IOException e) {
            log.error("缩略图异常", e);
            throw BizException.FILE_EXCEPTION.newInstance("缩略图生成异常", e.getMessage());
        }
    }

    private String getVideoThumbnail(InputStream inputStream) throws FrameGrabber.Exception, BizException {
        /*视频队列最大时间(三秒)*/
        if (WORKING_NUM.get() <= MAX_NUM) {
            WORKING_NUM.incrementAndGet();
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(inputStream);
            /*操作视频数据需要先开启视频 放首位*/
            fFmpegFrameGrabber.start();
            try {
                //获取视频总帧数
                long lengthInTime = fFmpegFrameGrabber.getLengthInTime();
                String videoThumbnail = getVideoThumbnail(fFmpegFrameGrabber, lengthInTime);
                /*关闭视频*/
                log.info("视频关闭");
                closeGrabber(fFmpegFrameGrabber);
                log.info("视频关闭完成");
                return videoThumbnail;
            } catch (Exception e) {
                /*关闭视频*/
                closeGrabber(fFmpegFrameGrabber);
                log.error("视频缩略图异常", e);
                throw BizException.FILE_EXCEPTION.newInstance("视频缩略图生成异常");
            }
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            getVideoThumbnail(inputStream);
        }
        throw BizException.FILE_EXCEPTION.newInstance("视频缩略图解析资源被占用 请稍后再试");
    }

    private void closeGrabber(FFmpegFrameGrabber fFmpegFrameGrabber) throws FrameGrabber.Exception {
        /*视频解析器停止关闭*/
        fFmpegFrameGrabber.stop();
        fFmpegFrameGrabber.close();
        /*同时解析数量维护*/
        WORKING_NUM.decrementAndGet();
    }

    /**
     * 视频缩略图生成
     *
     * @param fFmpegFrameGrabber fFmpegFrameGrabber
     * @param lengthInTime       lengthInTime
     * @return Thumbnail
     * @throws IOException  IOException
     * @throws BizException BizException
     */
    private String getVideoThumbnail(FFmpegFrameGrabber fFmpegFrameGrabber, long lengthInTime) throws IOException, BizException {
        for (int i = 0; i < AVERAGE; i++) {
            /*截取平均值*/
            long l = lengthInTime / AVERAGE * (i + 1);
            fFmpegFrameGrabber.setTimestamp(l);
            Frame frame = fFmpegFrameGrabber.grabImage();
            if (frame != null) {
                //创建BufferedImage对象
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                /*最后一次不判断图片颜色*/
                if (i != (AVERAGE - 2) && isBlack(bufferedImage)) {
                    /*判断为黑色图片*/
                    continue;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                boolean b = ImageIO.write(bufferedImage, THUMBNAIL_SUFFIX, out);
                if (!b) {
                    throw BizException.FILE_EXCEPTION.newInstance("缩略图生成异常");
                }
                ByteArrayInputStream imageInputStream = new ByteArrayInputStream(out.toByteArray());
                /*这里返回的是视频原截图路径*/
                /*缩略图 = 原路径分割"."在“."前面添加“_200x200”*/
            }
        }
        return null;
    }

    /**
     * 判断当前图片是否为黑
     *
     * @param bufferedImage bufferedImage
     * @return boolean
     */
    private static boolean isBlack(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        int totalPixel = height * width;
        int[] rgb = new int[4];
        int o = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = bufferedImage.getRGB(i, j);
                rgb[1] = (pixel & 0xff0000) >> 16;
                rgb[2] = (pixel & 0xff00) >> 8;
                rgb[3] = (pixel & 0xff);
                //如果像素点不相等的数量超过50个 就判断为彩色图片
                if (rgb[1] < BLACK_RATE && rgb[2] < BLACK_RATE && rgb[3] < BLACK_RATE) {
                    o++;
                }
            }
        }
        if (o == 0) {
            return false;
        }
        //100个像素里面20及以上都是黑的返回false
        int i1 = totalPixel / o;
        log.info("总像素" + totalPixel + "黑色像素为" + o + "判断为" + totalPixel / o);
        /*20一下判断黑像素就是好图%*/
        return i1 < 5;
    }
}

