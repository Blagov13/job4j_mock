package ru.checkdev.notification.telegram.action;

import java.util.Optional;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

/**
 * Класс реализует пункт меню проверки зарегистрированного пользователя
 * в телеграм бот.
 */
@AllArgsConstructor
public class CheckAction implements Action {
    private final TgUserService tgUserService;

    /**
     * Метод обрабатывает входящие сообщения от пользователей
     * и проверяет, зарегистрирован ли пользователь в системе.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        Optional<TgUser> existedUser = tgUserService.findByChatId(chatId);
        String text;
        String sl = System.lineSeparator();

        /*Проверка на наличие пользователя в системе*/
        if (existedUser.isEmpty()) {
            text = "Вы не зарегистрированы в системе." + sl
                    + "Для регистрации, используйте команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }

        /*Формирование ответа с данными зарегистрированного пользователя*/
        text = "Аккаунт привязан к следующим данным: " + sl
                + "имя пользователя: " + existedUser.get().getUsername() + sl
                + "почта: " + existedUser.get().getEmail();
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

