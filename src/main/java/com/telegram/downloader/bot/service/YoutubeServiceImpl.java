package com.telegram.downloader.bot.service;

import com.telegram.downloader.bot.projection.video.GetVideoDownloadUrl;
import com.telegram.downloader.bot.entity.VideoEntity;
import com.telegram.downloader.bot.enums.VideoExtension;
import com.telegram.downloader.bot.model.Format;
import com.telegram.downloader.bot.model.VideoInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YoutubeServiceImpl implements YoutubeService {

    private final VideoService videoService;
    private final BotService botService;

    private final WebClient webClient = WebClient.create();

    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    @Value("${youtube.base-info-url}")
    private String baseInfoUrl;

    @Value("${youtube.base-video-url}")
    private String baseVideoUrl;

    public YoutubeServiceImpl(VideoService videoService, BotService botService) {

        this.videoService = videoService;
        this.botService = botService;
    }

    private VideoInfo getVideoInfo(String videoId) {

        final String body = "{  \n" +
                "  \"context\": {\n" +
                "    \"client\": {\n" +
                "      \"hl\": \"en\",\n" +
                "      \"clientName\": \"WEB\",\n" +
                "      \"clientVersion\": \"2.20210721.00.00\",\n" +
                "      \"clientFormFactor\": \"UNKNOWN_FORM_FACTOR\",\n" +
                "      \"clientScreen\": \"WATCH\",\n" +
                "      \"mainAppWebInfo\": {\n" +
                "        \"graftUrl\": \"/watch?v=" + videoId + "\",\n" +
                "        }\n" +
                "        },\n" +
                "        \"user\": {      \n" +
                "          \"lockedSafetyMode\": false\n" +
                "        },\n" +
                "          \"request\": {      \n" +
                "     \"useSsl\": true,\n" +
                "     \"internalExperimentFlags\": [],\n" +
                "     \"consistencyTokenJars\": []\n" +
                "    } },\n" +
                "    \"videoId\": \"" + videoId + "\",\n" +
                "    \"playbackContext\": {\n" +
                "      \"contentPlaybackContext\": {\n" +
                "        \"vis\": 0,\n" +
                "        \"splay\": false,\n" +
                "        \"autoCaptionsDefaultOn\": false,\n" +
                "        \"autonavState\": \"STATE_NONE\",\n" +
                "        \"html5Preference\": \"HTML5_PREF_WANTS\",\n" +
                "        \"lactMilliseconds\": \"-1\"\n" +
                "        }  \n" +
                "      \n" +
                "    },\n" +
                "    \"racyCheckOk\": false,\n" +
                "    \"contentCheckOk\": false\n" +
                "  \n" +
                "}";

        VideoInfo videoInfo = webClient
                .post()
                .uri(baseInfoUrl + youtubeApiKey)
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(VideoInfo.class)
                .block();

        List<Format> formats = videoInfo.getStreamingData().getFormats();

        if (!formats.isEmpty()) {
            for (Format format : formats) {

                boolean exists = videoService.existsByVideoId(videoId);

                if (!exists) {

                    videoService.create(VideoEntity.builder()
                            .videoId(videoId)
                            .downloadUrl(format.getUrl())
                            .quality(format.getQuality())
                            .url(baseVideoUrl + videoId)
                            .build());
                }
            }
        }

        return videoInfo;
    }

    @Override
    public File download(String chatId, String videoUrl, String quality) {

        String videoId = videoUrl.replace(baseVideoUrl, "");

        try {

            String extension = getExtension();

            String downloadUrl = getUrlWithQuality(videoId, quality);
            String filename = videoId;

            filename = cleanFilename(filename);
            if (filename.length() == 0) {
                filename = videoId;
            } else {
                filename += "_" + videoId;
            }

            filename += "." + extension;
            File outputFile = new File(filename);

            if (downloadUrl != null) {
                downloadWithHttpClient(chatId, downloadUrl, outputFile);
            }

            return outputFile;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

    @Override
    public List<String> getVideoQualities(String videoUrl) {

        String videoId = videoUrl.replace(baseVideoUrl, "");
        VideoInfo videoInfo = getVideoInfo(videoId);

        if (videoInfo != null) {

            return videoInfo.getStreamingData().getFormats()
                    .stream()
                    .map(Format::getQuality)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void downloadWithHttpClient(String chatId, String downloadUrl, File outputFile) throws Throwable {

        HttpResponse response = getDownloadContent(downloadUrl);

        if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
            long length = response.getEntity().getContentLength();

            InputStream inputStream = response.getEntity().getContent();

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            try {
                byte[] buffer = new byte[2048];
                int count = -1;
                double sumCount = 0.0;
                long lastPercent = 0;

                botService.sendMessageToChat(chatId, "Download started: \nfile length is " + length / 1000000 + "MB \n\nWe'll send your file in a few minutes ");

                while ((count = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, count);

                    sumCount += count;

                    long percent = Math.round((sumCount / length * 100.0));

                    if (lastPercent != percent) {
                        System.out.println("Downloading process: " + percent + "%");
                    }

                    lastPercent = percent;
                }

                fileOutputStream.flush();
            } finally {
                fileOutputStream.close();
            }
        }

    }

    private String getExtension() {
        return VideoExtension.MP4.toString();
    }

    private String cleanFilename(String fileName) {

        final char[] illegalFileNameChars = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

        for (char c : illegalFileNameChars) {
            fileName = fileName.replace(c, '_');
        }

        return fileName;
    }

    private HttpResponse getDownloadContent(String downloadUrl) throws Exception {

        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13");

        HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(httpGet);
    }


    private String getUrlWithQuality(String videoId, String quality) {

        GetVideoDownloadUrl videoDownloadUrl = videoService.getDownloadUrlByVideoIdAndQuality(videoId, quality);
        return videoDownloadUrl == null ? null : videoDownloadUrl.getDownloadUrl();
    }
}
