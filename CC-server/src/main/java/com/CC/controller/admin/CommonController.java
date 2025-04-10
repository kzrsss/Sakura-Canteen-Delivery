package com.CC.controller.admin;

import com.CC.constant.MessageConstant;
import com.CC.result.Result;
import com.CC.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:" + file.getOriginalFilename());
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件后缀
            String extention = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String objectname = UUID.randomUUID().toString() + extention;

            //文件的请求路径
            String filepath = aliOssUtil.upload(file.getBytes(), objectname);
            return Result.success(filepath);
        } catch (IOException e) {
            log.error(MessageConstant.UPLOAD_FAILED + ":" + e);

        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
