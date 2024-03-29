package com.gitdatsanvich.sweethome.util;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.gitdatsanvich.common.constants.CommonConstants;
import com.gitdatsanvich.common.constants.FileConstants;
import com.gitdatsanvich.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.context.annotation.Scope;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TangChen
 * @date 2021/5/28 14:28
 */
@Slf4j
@Scope(value = "prototype")
public class StorageUtil {


    private static final String THUMBNAIL_SUFFIX = "png";
    /*针对每一像素的光感度*/
    private static final int BLACK_RATE = 50;
    /*针对一张图片的黑色像素百分比*/
    private static final int AVERAGE = 10;

    private static final int MAX_NUM = 1;

    private static final AtomicInteger WORKING_NUM = new AtomicInteger(0);

    private static final String URL_PREFIX = "data/";

    private static final String THUMBNAIL_SIGN = "_thumbnail";

    /**
     * 缩略图保存
     * @param suffix 后缀名
     * @param fileInputStream 文件流
     * @param type 自定义类型
     * @param uuid  文件唯一UUID
     * @return url
     * @throws BizException BizException
     */
    public static String saveThumbnail(String suffix, InputStream fileInputStream, String type, String uuid) throws BizException {
        long start = System.currentTimeMillis();
        String thumbnailUrl = null;
        try {
            /*视频缩略图*/
            if (FileConstants.VIDEO.equals(type)) {
                thumbnailUrl = getVideoThumbnail(fileInputStream, CommonConstants.ZERO, uuid);
            }
            /*图片缩略图*/
            if (FileConstants.IMAGE.equals(type)) {
                thumbnailUrl = getImageThumbnail(fileInputStream, suffix, uuid);
            }
            long end = System.currentTimeMillis();
            log.info("缩略图生成时间为" + (end - start));
            return thumbnailUrl;
        } catch (IOException e) {
            log.error("缩略图异常", e);
            throw BizException.FILE_EXCEPTION.newInstance("缩略图生成异常", e.getMessage());
        }
    }

    /**
     * 保存
     * @param inputStream inputStream
     * @param suffix 后缀名
     * @param uuid 唯一UUID
     * @return URL
     * @throws IOException  IOException
     */
    public static String save(InputStream inputStream, String suffix, String uuid) throws IOException {
        String fileName = uuid + StringPool.DOT + suffix;
        String destination = URL_PREFIX + fileName;
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = inputStream.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        inputStream.close();
        return destination;
    }

    /**
     * 图片缩略图
     * @param  inputStream inputStream
     * @param suffix 后缀名
     * @param uuid 唯一UUID
     * @return URL
     * @throws  IOException IOException
     */
    private static String getImageThumbnail(InputStream inputStream, String suffix, String uuid) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream).scale(0.5).outputFormat(suffix).outputQuality(0.5).toOutputStream(outputStream);
        ByteArrayInputStream inputStreamWrite = new ByteArrayInputStream(outputStream.toByteArray());
        return save(inputStreamWrite, suffix, uuid + THUMBNAIL_SIGN);
    }


    private static String getVideoThumbnail(InputStream inputStream, int retry, String uuid) throws IOException, BizException {
        /*视频队列最大时间(三秒)*/
        if (WORKING_NUM.get() <= MAX_NUM) {
            WORKING_NUM.incrementAndGet();
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(inputStream);
            /*操作视频数据需要先开启视频 放首位*/
            fFmpegFrameGrabber.start();
            try {
                //获取视频总帧数
                long lengthInTime = fFmpegFrameGrabber.getLengthInTime();
                return getVideoThumbnail(fFmpegFrameGrabber, lengthInTime, uuid);
            } catch (Exception e) {
                /*关闭视频*/
                log.error("视频缩略图异常", e);
                throw BizException.FILE_EXCEPTION.newInstance("视频缩略图生成异常");
            } finally {
                /*关闭视频*/
                log.info("视频关闭");
                closeGrabber(fFmpegFrameGrabber, inputStream);
                log.info("视频关闭完成");
            }
        } else {
            if (retry <= 5) {
                try {
                    /*睡一秒*/
                    Thread.sleep(1000);
                    retry++;
                } catch (InterruptedException ignored) {
                }
                getVideoThumbnail(inputStream, retry, uuid);
            }
            throw BizException.FILE_EXCEPTION.newInstance("视频缩略图解析资源被占用 请稍后再试");
        }
    }

    private static void closeGrabber(FFmpegFrameGrabber fFmpegFrameGrabber, InputStream inputStream) throws IOException {
        /*视频解析器停止关闭*/
        fFmpegFrameGrabber.stop();
        fFmpegFrameGrabber.release();
        fFmpegFrameGrabber.close();
        inputStream.close();
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
    private static String getVideoThumbnail(FFmpegFrameGrabber fFmpegFrameGrabber, long lengthInTime, String uuid) throws IOException, BizException {
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
                ByteArrayInputStream imageInputStreamForThumbnail = new ByteArrayInputStream(out.toByteArray());
                ByteArrayInputStream imageInputStream = new ByteArrayInputStream(out.toByteArray());
                /*这里返回的是视频原截图路径*/
                /*缩略图 = 原路径分割"."在“."前面添加“_thumbnail”*/
                /*原图片保存*/
                String save = save(imageInputStream, THUMBNAIL_SUFFIX, uuid);
                /*原图片缩略图保存*/
                getImageThumbnail(imageInputStreamForThumbnail, THUMBNAIL_SUFFIX, uuid);
                out.close();
                return save;
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
                /*BLACK_RATE = 50 针对RGB三个值都小于50算一个黑色像素*/
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
        /*20%以下判断黑像素就是好图*/
        return i1 <= 5;
    }

    /**
     * 克隆流
     *
     * @param input input
     * @return ByteArrayOutputStream
     */
    public static ByteArrayOutputStream cloneInputStream(InputStream input) throws BizException {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                bas.write(buffer, 0, len);
            }
            bas.flush();
            return bas;
        } catch (IOException e) {
            throw BizException.FILE_EXCEPTION.newInstance("流复制失败");
        }
    }
}
