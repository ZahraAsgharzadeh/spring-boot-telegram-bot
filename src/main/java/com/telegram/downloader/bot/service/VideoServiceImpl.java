package com.telegram.downloader.bot.service;

import com.telegram.downloader.bot.repository.VideoRepository;
import com.telegram.downloader.bot.projection.video.GetVideoDownloadUrl;
import com.telegram.downloader.bot.entity.VideoEntity;
import org.springframework.stereotype.Service;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository repository;

    public VideoServiceImpl(VideoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(VideoEntity entity) {
        repository.save(entity);
    }


    @Override
    public GetVideoDownloadUrl getDownloadUrlByVideoIdAndQuality(String videoId, String quality) {
        return repository.findByVideoIdAndQuality(videoId, quality);
    }

    @Override
    public boolean existsByVideoId(String videoId) {
        return repository.existsByVideoId(videoId);
    }
}
