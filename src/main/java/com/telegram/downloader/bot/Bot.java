package com.telegram.downloader.bot;

import com.telegram.downloader.bot.service.YoutubeService;
import com.telegram.downloader.bot.enums.Commands;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${youtube.base-video-url}")
    private String baseVideoUrl;

    @Lazy //todo: fix circular
    @Autowired
    private YoutubeService youtubeService;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {

            String text = update.getMessage().getText();
            String chatId = String.valueOf(update.getMessage().getChatId());
            Integer messageId = update.getMessage().getMessageId();

            if (text.equals(Commands.START.toString())) {

                onStart(chatId, messageId);

            }else if (text.startsWith(baseVideoUrl)) {

                getFileInfo(chatId, messageId, text);

            }else if (text.startsWith("Quality: ")) {

                String[] textArray = text.split(": ");

                String quality = textArray[1].replace("    Link", "");
                String downloadUrl = textArray[3];

                onDownload(chatId, downloadUrl, quality);
            }else {

                execute(SendMessage.builder()
                        .text("Wrong command !!!")
                        .build());
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void onStart(String chatId, Integer messageId) throws TelegramApiException {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("Hello, nice to meet you ! \n\n Send url like -> https://www.youtube.com/... to start");

        execute(sendMessage);
    }

    private void getFileInfo(String chatId, Integer messageId, String url) throws TelegramApiException {

        List<String> qualities = youtubeService.getVideoQualities(url);

        KeyboardRow keyboardRow = new KeyboardRow();
        qualities.forEach(quality -> keyboardRow.add("Quality: " + quality + ": " + "Link: " + url));

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("Choose quality: \uD83C\uDF08");
        sendMessage.setReplyMarkup(ReplyKeyboardMarkup.builder()
                .keyboard(Collections.singleton(keyboardRow))
                .selective(true)
                .inputFieldPlaceholder(url)
                .build());

        execute(sendMessage);
    }

    private void onDownload(String chatId, String url, String quality) throws Exception {

        File downloadedFile = youtubeService.download(chatId, url, quality);

        if (downloadedFile != null && downloadedFile.length() != 0) {

            SendVideo sendVideo = SendVideo.builder()
                    .caption("Congratulations \uD83C\uDF89")
                    .chatId(chatId)
                    .video(new InputFile(downloadedFile))
                    .build();

            execute(sendVideo);

            FileUtils.forceDelete(downloadedFile);
        }else {

            SendMessage sendMessage = SendMessage.builder()
                    .text("Oops ... Download failed !!!")
                    .chatId(chatId)
                    .build();

            execute(sendMessage);
        }
    }
}
