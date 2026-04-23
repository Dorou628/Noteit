package com.example.noteit.file.domain;

public interface OssPresignGateway {

    // 为客户端直传生成签名信息。
    // 当前先落阿里云 OSS 表单直传，后续如果切换 STS 或预签名 URL，也只需要替换这一层。
    OssPresignResult presignUpload(OssPresignRequest request);
}
