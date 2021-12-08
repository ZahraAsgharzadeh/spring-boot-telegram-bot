package com.telegram.downloader.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Format {

    private Integer itag;

    private String url;

    private String mimeType;

    private Integer  bitrate;

    private Integer width;

    private Integer height;

    private String lastModified;

    private String quality;

    private Integer fps;

    private String qualityLevel;

}

