package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgUserService;

/**
 * Класс реализует действие по отвязке телеграм аккаунта от существующего аккаунта пользователя.
 */
@AllArgsConstructor
public class UnsubscribeAction implements Action {
    private final TgUserService tgUserService;

    /**
     * Метод обрабатывает входящие сообщения от пользователей
     * и отвязывает телеграм аккаунт, если он привязан.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        String text;
        String sl = System.lineSeparator();

        /*Проверка наличия пользователя в системе*/
        if (!tgUserService.checkUserExists(chatId)) {
            text = "Вы ещё не зарегистрированы в системе." + sl
                    + "Для регистрации, пожалуйста, используйте команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }

        /*Удаление привязки пользователя*/
        tgUserService.deleteByChatId(chatId);
        text = "Телеграм успешно отвязан.";
        return new SendMessage(String.valueOf(chatId), text);
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

