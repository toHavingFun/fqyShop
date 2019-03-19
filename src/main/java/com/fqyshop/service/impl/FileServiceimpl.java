package com.fqyshop.service.impl;

import com.fqyshop.service.IFileService;
import com.fqyshop.util.FTPUtil;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Administrator on 2019/1/21.
 */
@Service("iFileService")
public class FileServiceimpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceimpl.class);
    public String upload(MultipartFile file,String path){

        String filename = file.getOriginalFilename();
        String[] split = filename.split(".");
        String extension = split[split.length-1];//获取文件扩展名
        StringBuilder builder = new StringBuilder(UUID.randomUUID().toString());
        String uploadFileName = builder.append(".").append(extension).toString();
        logger.info("上传文件的文件名：{},上传的路经:{},新文件名：{}",filename,path,uploadFileName);
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile =new File(path, uploadFileName);
        try {
            file.transferTo(targetFile);
            FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
