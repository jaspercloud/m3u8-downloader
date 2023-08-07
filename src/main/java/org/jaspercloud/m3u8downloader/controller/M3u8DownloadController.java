package org.jaspercloud.m3u8downloader.controller;

import org.apache.commons.io.FilenameUtils;
import org.jaspercloud.m3u8downloader.domain.DownloadInfo;
import org.jaspercloud.m3u8downloader.domain.M3u8;
import org.jaspercloud.m3u8downloader.exception.DownloadException;
import org.jaspercloud.m3u8downloader.support.M3u8Downloader;
import org.jaspercloud.m3u8downloader.support.M3u8FileWriter;
import org.jaspercloud.m3u8downloader.support.TsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class M3u8DownloadController implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private M3u8Downloader m3u8Downloader;

    @Resource
    private TsDownloader tsDownloader;

    @Resource
    private M3u8FileWriter m3u8FileWriter;

    @Value("${download.baseDir}")
    private String baseDir;

    @Value("${download.thread}")
    private int thread;

    private ExecutorService executorService;

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newFixedThreadPool(thread);
    }

    @PostMapping("/download")
    public void download(@RequestBody DownloadInfo downloadInfo) throws Exception {
        File videoDir = new File(baseDir, downloadInfo.getVideoName());
        videoDir.mkdirs();
        logger.info("start m3u8: videoName={}", downloadInfo.getVideoName());
        M3u8 m3u8 = m3u8Downloader.download(downloadInfo.getUrl());
        logger.info("completed m3u8: videoName={}", downloadInfo.getVideoName());
        m3u8FileWriter.write(videoDir, "index.m3u8", m3u8.getMetaList());
        List<String> urlList = m3u8.getUrlList();
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        urlList.forEach(e -> queue.offer(e));
        AtomicInteger index = new AtomicInteger();
        for (int i = 0; i < thread; i++) {
            executorService.execute(() -> {
                while (!queue.isEmpty()) {
                    try {
                        String take = queue.take();
                        try {
                            String fileName = FilenameUtils.getName(take);
                            tsDownloader.download(downloadInfo.getUrl(), take, new File(videoDir, fileName));
                            logger.info("completed ts: videoName={}, url={}", downloadInfo.getVideoName(), take);
                            logger.info("progress: videoName={}, {}/{}", downloadInfo.getVideoName(), index.incrementAndGet(), urlList.size());
                        } catch (DownloadException e) {
                            logger.info("failed ts: videoName={}, url={}", downloadInfo.getVideoName(), take);
                            queue.offer(take);
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
        }
    }
}
