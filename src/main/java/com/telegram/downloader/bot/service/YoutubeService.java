package com.telegram.downloader.bot.service;

import java.io.File;
import java.util.List;

public interface YoutubeService {

    List<String> getVideoQualities(String videoUrl);

    File download(String chatId, String videoUrl, String quality);
}
