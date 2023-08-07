package org.jaspercloud.m3u8downloader.support;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.jaspercloud.m3u8downloader.exception.DownloadException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Component
public class TsDownloader {

    @Resource
    private OkHttpClient okHttpClient;

    public void download(String baseUrl, String downloadUrl, File file) {
        if (!downloadUrl.startsWith("http")) {
            downloadUrl = FilenameUtils.getPath(baseUrl) + downloadUrl;
        }
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (200 != response.code()) {
                throw new DownloadException("httpCode: " + response.code());
            }
            long length = response.body().contentLength();
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                try (BufferedInputStream in = new BufferedInputStream(response.body().byteStream())) {
                    StreamUtils.copyRange(in, out, 0, length);
                }
            }
        } catch (DownloadException e) {
            throw e;
        } catch (Throwable e) {
            throw new DownloadException(e.getMessage(), e);
        }
    }
}
