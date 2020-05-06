package com.vx.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author zty
 * @date 2020/4/17 下午2:56
 * @description:
 */
@Slf4j
public class UploadImageUtil {
    public static String uploadFile(MultipartFile file) {
        String imageFilePath = "/home/zty/image/";
        String imageUrl = "https://ztyztyztylhm.mynatapp.cc/";
        // 获取文件名
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String newFileName = System.currentTimeMillis() + "." + suffix;
        try {
            log.info("上传文件文件名称：{}",newFileName);
            log.info("上传文件大小 ：{}",file.getSize());
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(imageFilePath + newFileName));
        } catch (IOException e) {
            return null;
        }
        return imageUrl + "/image/" + newFileName;
    }
}
