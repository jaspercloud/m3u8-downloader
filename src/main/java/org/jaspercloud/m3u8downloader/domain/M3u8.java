package org.jaspercloud.m3u8downloader.domain;

import java.util.ArrayList;
import java.util.List;

public class M3u8 {

    private List<String> metaList = new ArrayList<>();
    private List<String> urlList = new ArrayList<>();

    public List<String> getMetaList() {
        return metaList;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void addMeta(String meta) {
        metaList.add(meta);
    }

    public void addUrl(String url) {
        urlList.add(url);
    }
}
