package org.jaspercloud.m3u8downloader.domain;

public class DownloadInfo {

    private String videoName;
    private String url;

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
