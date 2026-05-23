package org.xhy.community.infrastructure.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AliyunOssServiceTest {

    @Test
    void replacePresignedUrlHostUsesCustomDomainScheme() {
        String replaced = AliyunOssService.replacePresignedUrlHost(
                "http://bucket.oss-cn-beijing.aliyuncs.com/uploads/demo.mp4?Expires=1&Signature=abc",
                "bucket.oss-cn-beijing.aliyuncs.com",
                "https://oss.xhyovo.cn"
        );

        assertEquals(
                "https://oss.xhyovo.cn/uploads/demo.mp4?Expires=1&Signature=abc",
                replaced
        );
    }

    @Test
    void replacePresignedUrlHostKeepsOriginalUrlWhenHostDoesNotMatchBucketDomain() {
        String original = "http://other.oss-cn-beijing.aliyuncs.com/uploads/demo.mp4?Expires=1";

        String replaced = AliyunOssService.replacePresignedUrlHost(
                original,
                "bucket.oss-cn-beijing.aliyuncs.com",
                "https://oss.xhyovo.cn"
        );

        assertEquals(original, replaced);
    }

    @Test
    void replacePresignedUrlHostPreservesCustomPathPrefix() {
        String replaced = AliyunOssService.replacePresignedUrlHost(
                "http://bucket.oss-cn-beijing.aliyuncs.com/uploads/demo.mp4?Expires=1",
                "bucket.oss-cn-beijing.aliyuncs.com",
                "https://cdn.xhyovo.cn/private"
        );

        assertEquals(
                "https://cdn.xhyovo.cn/private/uploads/demo.mp4?Expires=1",
                replaced
        );
    }
}
