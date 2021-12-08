package com.telegram.downloader.bot.service;

import com.telegram.downloader.bot.projection.video.GetVideoDownloadUrl;
import com.telegram.downloader.bot.entity.VideoEntity;

public interface VideoService {

    void create(VideoEntity entity);

    GetVideoDownloadUrl getDownloadUrlByVideoIdAndQuality(String videoId, String quality);

    boolean existsByVideoId(String videoId);
}
