package com.gitdatsanvich.sweethome.util;


import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.gitdatsanvich.common.constants.FileConstants;
import com.gitdatsanvich.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件头校验
 *
 * @author TangChen
 */
@Slf4j
public class FileHeaderCheckUtil {

    private final static Map<String, String> FILE_TYPE_MAP = new HashMap<>(16);
    private final static Map<String, String> DEFINED_TYPE_MAP = new HashMap<>(16);
    private final static Map<String, String> ALLOWED_TYPE_MAP = new HashMap<>(16);

    static {
        //初始化文件类型信息
        getAllFileType();
        getDefinedType();
        getAllowedType();
    }

    private static void getAllowedType() {
        /*支持的音频文件*/
        ALLOWED_TYPE_MAP.put("m4a", FileConstants.AUDIO);
        ALLOWED_TYPE_MAP.put("wav", FileConstants.AUDIO);
        ALLOWED_TYPE_MAP.put("mp3", FileConstants.AUDIO);
        ALLOWED_TYPE_MAP.put("aac", FileConstants.AUDIO);
        /*音频文件异常*/
        ALLOWED_TYPE_MAP.put(FileConstants.AUDIO, "音频文件暂不支持%s格式");
        /*支持的视频文件*/
        ALLOWED_TYPE_MAP.put("mp4", FileConstants.VIDEO);
        /*视频文件异常*/
        ALLOWED_TYPE_MAP.put(FileConstants.VIDEO, "视频文件暂不支持%s格式");
        /*支持的图片文件*/
        ALLOWED_TYPE_MAP.put("jpg", FileConstants.IMAGE);
        ALLOWED_TYPE_MAP.put("jpeg", FileConstants.IMAGE);
        ALLOWED_TYPE_MAP.put("png", FileConstants.IMAGE);
        ALLOWED_TYPE_MAP.put("gif", FileConstants.IMAGE);
        ALLOWED_TYPE_MAP.put("bmp", FileConstants.IMAGE);
        /*图片文件异常*/
        ALLOWED_TYPE_MAP.put(FileConstants.IMAGE, "图片文件暂不支持%s格式");
        /*其他文件*/
        ALLOWED_TYPE_MAP.put("pdf", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("ppt", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("doc", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("xls", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("docx", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("xlsx", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("pptx", FileConstants.OTHER);
        ALLOWED_TYPE_MAP.put("txt", FileConstants.OTHER);
        /*其他文件格式异常*/
        ALLOWED_TYPE_MAP.put(FileConstants.OTHER, "文件暂不支持%s格式");
        /*全部文件的格式异常*/
        ALLOWED_TYPE_MAP.put(FileConstants.ALL_TYPE, "全部文件暂不支持%s格式");
    }

    private static void getDefinedType() {
        DEFINED_TYPE_MAP.put("jpg", FileConstants.IMAGE);
        DEFINED_TYPE_MAP.put("jpeg", FileConstants.IMAGE);
        DEFINED_TYPE_MAP.put("png", FileConstants.IMAGE);
        DEFINED_TYPE_MAP.put("gif", FileConstants.IMAGE);
        DEFINED_TYPE_MAP.put("bmp", FileConstants.IMAGE);
        DEFINED_TYPE_MAP.put("mp3", FileConstants.AUDIO);
        DEFINED_TYPE_MAP.put("m4a", FileConstants.AUDIO);
        DEFINED_TYPE_MAP.put("wav", FileConstants.AUDIO);
        DEFINED_TYPE_MAP.put("aac", FileConstants.AUDIO);
        DEFINED_TYPE_MAP.put("mp4", FileConstants.VIDEO);
        DEFINED_TYPE_MAP.put(FileConstants.OTHER, FileConstants.OTHER);
    }

    /**
     * 文件头存储规则
     * 文件头全部存储大写
     * 一般：key = 文件头 value = 文件后缀
     * 当同一文件头有多个 后缀时
     * 存储 一个key = value = 文件头
     * 之后存储 key = 文件后缀 value = 文件头
     * 若同一文件头有多个后缀同时 后缀又同时拥有多个文件头
     * 使用 大小写区分 key 值
     */
    private static void getAllFileType() {
        FILE_TYPE_MAP.put("D0CF11E0", "D0CF11E0");
        FILE_TYPE_MAP.put("ppt", "D0CF11E0");
        FILE_TYPE_MAP.put("doc", "D0CF11E0");
        FILE_TYPE_MAP.put("xls", "D0CF11E0");
        FILE_TYPE_MAP.put("vst", "D0CF11E0");
        FILE_TYPE_MAP.put("vsd", "D0CF11E0");
        FILE_TYPE_MAP.put("vss", "D0CF11E0");
        FILE_TYPE_MAP.put("mpp", "D0CF11E0");
        FILE_TYPE_MAP.put("504B0304", "504B0304");
        FILE_TYPE_MAP.put("pptx", "504B0304");
        FILE_TYPE_MAP.put("docx", "504B0304");
        FILE_TYPE_MAP.put("xlsx", "504B0304");
        FILE_TYPE_MAP.put("vsdx", "504B0304");
        FILE_TYPE_MAP.put("zip", "504B0304");
        FILE_TYPE_MAP.put("FFD8FFE0", "FFD8FFE0");
        FILE_TYPE_MAP.put("jpeg", "FFD8FFE0");
        FILE_TYPE_MAP.put("89504E47", "89504E47");
        FILE_TYPE_MAP.put("png", "89504E47");
        FILE_TYPE_MAP.put("68746D6C3E", "html");
        FILE_TYPE_MAP.put("25504446", "pdf");
        FILE_TYPE_MAP.put("47494638", "gif");
        FILE_TYPE_MAP.put("4D534346", "zip");
        /*bmp文件头很乱 重点关注*/
        FILE_TYPE_MAP.put("424D", "bmp");
        FILE_TYPE_MAP.put("424D7E1A", "bmp");
        FILE_TYPE_MAP.put("424D16C9", "bmp");
        FILE_TYPE_MAP.put("424D3664", "bmp");
        FILE_TYPE_MAP.put("424DD64E", "bmp");
        FILE_TYPE_MAP.put("2F2F2073", "js");
        FILE_TYPE_MAP.put("2E617661", "css");
        FILE_TYPE_MAP.put("49545346", "chm");
        FILE_TYPE_MAP.put("52617221", "rar");
        /*MP4文件头很乱 重点关注*/
        FILE_TYPE_MAP.put("0000", "mp4");
        FILE_TYPE_MAP.put("00000018", "mp4");
        FILE_TYPE_MAP.put("00000020", "mp4");
        FILE_TYPE_MAP.put("0000001C", "mp4");
        /*MP3文件头很乱 重点关注*/
        FILE_TYPE_MAP.put("664C6143", "flac");
        FILE_TYPE_MAP.put("464C5601", "flv");
        FILE_TYPE_MAP.put("FFF15080", "aac");
        FILE_TYPE_MAP.put("52494646", "wav");
        /*不校验的文件头*/
        FILE_TYPE_MAP.put("txt", FileConstants.NO_NEED);
        FILE_TYPE_MAP.put("jpg", FileConstants.NO_NEED);
        FILE_TYPE_MAP.put("mp3", FileConstants.NO_NEED);
    }

    /**
     * 得到上传文件的文件头
     *
     * @param src src
     * @return String
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null == src || src.length <= 0) {
            return StringPool.EMPTY;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 检查文件头并返回文件类型
     *
     * @param file file
     * @return type
     * @throws BizException BizException
     */
    public static String checkFileHeader(MultipartFile file, String uploadType) throws BizException {
        try {
            /*文件名校验,文件后缀获取*/
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw BizException.FILE_EXCEPTION.newInstance("文件名称获取为空");
            }
            int num = originalFilename.lastIndexOf(StringPool.DOT);
            if (-1 == num) {
                throw BizException.FILE_EXCEPTION.newInstance("文件后缀获取失败");
            }
            /*后缀获转换两种 大写和小写*/
            String suffix = file.getOriginalFilename().substring(num + 1);
            /*文件预期类型校验*/
            allowedCheck(suffix, uploadType);
            /*获取文件头*/
            InputStream inputStream = file.getInputStream();
            byte[] b = new byte[4];
            int read = inputStream.read(b, 0, b.length);
            String header = bytesToHexString(b);
            check(suffix, header, originalFilename);
            return definedType(suffix);
        } catch (IOException e) {
            log.error("文件头校验异常", e);
            throw BizException.FILE_EXCEPTION.newInstance("文件头校验异常");
        }
    }

    private static void allowedCheck(String suffix, String uploadType) throws BizException {
        /*上传定义校验*/
        if (uploadType == null) {
            uploadType = FileConstants.ALL_TYPE;
        }
        if (!FileConstants.ALL_TYPE.equals(uploadType)) {
            if (!ALLOWED_TYPE_MAP.containsKey(uploadType)) {
                throw BizException.FILE_EXCEPTION.newInstance("未被定义的上传方式");
            }
            String lowerCaseSuffix = suffix.toLowerCase();
            /*后缀被定义*/
            if (ALLOWED_TYPE_MAP.containsKey(lowerCaseSuffix)) {
                String allowedType = ALLOWED_TYPE_MAP.get(lowerCaseSuffix);
                /*上传文件 定义文件不一致*/
                if (!allowedType.equals(uploadType)) {
                    throw BizException.FILE_EXCEPTION.newInstance(String.format(ALLOWED_TYPE_MAP.get(uploadType), suffix));
                }
                /*后缀未被定义*/
            } else {
                throw BizException.FILE_EXCEPTION.newInstance(String.format(ALLOWED_TYPE_MAP.get(uploadType), suffix));
            }
        } else {
            if (!ALLOWED_TYPE_MAP.containsKey(suffix.toLowerCase())) {
                throw BizException.FILE_EXCEPTION.newInstance(String.format(ALLOWED_TYPE_MAP.get(uploadType), suffix));
            }
        }
    }

    private static String definedType(String suffix) {
        return DEFINED_TYPE_MAP.getOrDefault(suffix, FileConstants.OTHER);
    }

    public static void check(String suffix, String header, String originalFilename) throws BizException {
        log.info("校验文件，文件头为：{}，文件后缀为：{}", header, suffix);
        /*转换大小写*/
        String suffixLowerCase = suffix.toLowerCase();
        String suffixUpperCase = suffix.toUpperCase();
        String headerUpperCase = header.toUpperCase();
        String suffixValueA = FILE_TYPE_MAP.get(suffixLowerCase);
        String suffixValueB = FILE_TYPE_MAP.get(suffixUpperCase);
        if (FILE_TYPE_MAP.containsKey(headerUpperCase)) {
            /*涵盖了文件头*/
            String headerValue = FILE_TYPE_MAP.get(headerUpperCase);
            if (headerValue.equals(suffixLowerCase) || headerValue.equals(suffixUpperCase)) {
                /*文件头校验成功*/
                return;
            } else if (headerValue.equals(headerUpperCase)) {
                /*文件头对应多后缀*/
                if (headerUpperCase.equals(suffixValueA) || headerUpperCase.equals(suffixValueB)) {
                    /*文件头校验成功*/
                    return;
                }
            }
        }
        if (FILE_TYPE_MAP.containsKey(suffixLowerCase) || FILE_TYPE_MAP.containsKey(suffixUpperCase)) {
            if (FileConstants.NO_NEED.equals(suffixValueA) || FileConstants.NO_NEED.equals(suffixValueB)) {
                return;
            }
        }
        /*针对文件头不确定的只截取前四位进行校验*/
        String headerSubstring = headerUpperCase.substring(0, 4);
        if (FILE_TYPE_MAP.containsKey(headerSubstring)) {
            String headerValue = FILE_TYPE_MAP.get(headerSubstring);
            if (headerValue.equals(suffixLowerCase) || headerValue.equals(suffixUpperCase)) {
                /*文件头校验成功*/
                return;
            }
        }
        log.info("校验文件失败，文件头为：{}，文件后缀为：{}，文件名为：{}", header, suffix, originalFilename);
        throw BizException.FILE_EXCEPTION.newInstance("文件头校验失败");
    }
}
