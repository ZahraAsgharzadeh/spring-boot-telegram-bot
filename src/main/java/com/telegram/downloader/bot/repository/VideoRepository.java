package com.telegram.downloader.bot.repository;

import com.telegram.downloader.bot.projection.video.GetVideoDownloadUrl;
import com.telegram.downloader.bot.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, String> {

    GetVideoDownloadUrl findByVideoIdAndQuality(String videoId, String quality);

    boolean existsByVideoId(String videoId);
}
