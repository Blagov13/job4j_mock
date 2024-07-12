package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Calendar;
import java.util.Collections;

/**
 * 3. Мидл
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот
 *
 * @autor Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@AllArgsConstructor
@Slf4j
public class RegAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_REGISTRATION = "/registration";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final String urlSiteAuth;
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
        String text;
        String sl = System.lineSeparator();

        if (tgUserService.checkUserExists(chatId)) {
            text = "Вы уже зарегистрированы в системе." + sl
                    + "Чтобы узнать регистрационные данные используйте /check";
            return new SendMessage(String.valueOf(chatId), text);
        }

        text = "Введите email для регистрации:";
        return new SendMessage(String.valueOf(chatId), text);
    }

    /**
     * Метод обрабатывает callback сообщение с email для регистрации,
     * выполняет проверку email, отправляет запрос на регистрацию,
     * и обрабатывает ответ от сервиса авторизации.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var email = message.getText();
        var username = message.getFrom().getUserName();
        var text = "";
        var sl = System.lineSeparator();

        /*Проверка на корректность email*/
        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова." + sl
                    + "/new";
            return new SendMessage(chatId, text);
        }

        var password = tgConfig.getPassword();
        var person = new PersonDTO(username, email, password, true, Collections.emptyList(),
                Calendar.getInstance());
        Object result;

        /*Отправка данных в сервис Auth*/
        try {
            result = authCallWebClint.doPost(URL_AUTH_REGISTRATION, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис авторизации не доступен попробуйте позже" + sl
                    + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = mapObject.get(ERROR_OBJECT) + sl
                    + "Если Вы владелец существующего аккаунта с указанной почтой, "
                    + "воспользуйтесь командой /subscribe для привязки аккаунта.";
            return new SendMessage(chatId, text);
        }

        Object personObject = mapObject.get("person");
        var personData = tgConfig.getObjectToMap(personObject);
        int userId = 0;
        try {
            userId = Integer.parseInt(personData.get("id"));
        } catch (NumberFormatException e) {
            log.error("Ошибка преобразования id пользователя из строки в число: {}",
                    e.getMessage(), e);
        }

        /*Сохранение нового пользователя*/
        TgUser tgUser = new TgUser(0, username, email, message.getChatId(), userId);
        tgUserService.save(tgUser);

        text = "Вы зарегистрированы: " + sl
                + "Логин: " + email + sl
                + "Пароль: " + password + sl
                + urlSiteAuth;
        return new SendMessage(chatId, text);
    }
}
