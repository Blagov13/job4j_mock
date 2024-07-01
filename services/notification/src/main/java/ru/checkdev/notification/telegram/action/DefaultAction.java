package ru.checkdev.notification.telegram.action;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Класс реализует действие по умолчанию для неизвестных команд
 * в телеграм бот.
 */
public class DefaultAction implements Action {

    /**
     * Метод обрабатывает входящие сообщения от пользователей
     * и отвечает, что команда не найдена.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        return new SendMessage(chatId, "Команда не найдена. Пожалуйста, используйте действительную команду.");
    }

    /**
     * Метод обрабатывает callback сообщение от пользователя
     * и перенаправляет его на метод handle.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}

