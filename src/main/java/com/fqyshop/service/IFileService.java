package com.fqyshop.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2019/1/21.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
