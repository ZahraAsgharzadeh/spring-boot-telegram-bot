package com.telegram.downloader.bot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotService {

    void sendMessageToChat(String chatId, String text) throws TelegramApiException;
}
