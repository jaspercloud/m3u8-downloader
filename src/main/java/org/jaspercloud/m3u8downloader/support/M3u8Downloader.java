package org.jaspercloud.m3u8downloader.support;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FilenameUtils;
import org.jaspercloud.m3u8downloader.domain.M3u8;
import org.jaspercloud.m3u8downloader.exception.DownloadException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class M3u8Downloader {

    @Resource
    private OkHttpClient okHttpClient;

    public M3u8 download(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (200 != response.code()) {
                throw new DownloadException("httpCode: " + response.code());
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), "utf-8"))) {
                M3u8 m3u8 = new M3u8();
                String line;
                while (null != (line = reader.readLine())) {
                    if (line.startsWith("#")) {
                        m3u8.addMeta(line);
                    } else {
                        String tsName = FilenameUtils.getName(line);
                        m3u8.addMeta(tsName);
                        m3u8.addUrl(line);
                    }
                }
                return m3u8;
            }
        } catch (DownloadException e) {
            throw e;
        } catch (Throwable e) {
            throw new DownloadException(e.getMessage(), e);
        }
    }
}
