package org.jaspercloud.m3u8downloader.support;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
public class M3u8FileWriter {

    public void write(File dir, String fileName, List<String> metaList) throws Exception {
        File file = new File(dir, fileName);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8")))) {
            for (String meta : metaList) {
                writer.println(meta);
            }
        }
    }
}
