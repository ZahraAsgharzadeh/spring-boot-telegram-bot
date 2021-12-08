package com.telegram.downloader.bot.service;

import com.telegram.downloader.bot.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class BotServiceImpl implements BotService {

    private final Bot bot;

    public BotServiceImpl(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void sendMessageToChat(String chatId, String text) throws TelegramApiException {

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        bot.execute(sendMessage);
    }
}
