package ru.checkdev.notification.telegram.action;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

/**
 * Класс реализует действие по привязке телеграм аккаунта к существующему аккаунту пользователя.
 */
@AllArgsConstructor
@Slf4j
public class SubscribeAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String PROFILE_URL = "/profile/current";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgUserService tgUserService;
    private final TgAuthCallWebClint authCallWebClient;

    /**
     * Метод обрабатывает входящие сообщения от пользователей
     * и проверяет, привязан ли телеграм аккаунт к существующему аккаунту.
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
            text = "Ваш аккаунт телеграм уже привязан к аккаунту." + sl
                    + "Чтобы узнать данные используйте /check" + sl
                    + "Чтобы отвязать аккаунт используйте /unsubscribe";
            return new SendMessage(String.valueOf(chatId), text);
        }

        text = "Для привязки аккаунта, введите, пожалуйста, email и пароль через пробел." + sl
                + "Пример: \"example@mail.ru password\"";
        return new SendMessage(String.valueOf(chatId), text);
    }

    /**
     * Метод обрабатывает callback сообщение от пользователя,
     * получает токен и данные профиля, сохраняет привязку аккаунта.
     *
     * @param message Message - сообщение от пользователя
     * @return BotApiMethod<Message> - ответное сообщение
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        String chatId = message.getChatId().toString();
        String[] emailAndPassword = message.getText().split(" ");
        String text;

        if (emailAndPassword.length != 2) {
            text = "Пожалуйста, введите email и пароль как указано выше.";
            return new SendMessage(chatId, text);
        }

        String email = emailAndPassword[0];
        String password = emailAndPassword[1];
        String sl = System.lineSeparator();

        /*Проверка корректности email*/
        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова /subscribe";
            return new SendMessage(chatId, text);
        }

        Object tokenResult;
        try {
            Map<String, String> params = Map.of(
                    "username", email,
                    "password", password
            );
            tokenResult = authCallWebClient.token(params).block();
        } catch (Exception e) {
            log.error("WebClient token error: {}", e.getMessage());
            text = "Сервис авторизации не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }

        var tokenResultMap = tgConfig.getObjectToMap(tokenResult);
        if (tokenResultMap.containsKey(ERROR_OBJECT)) {
            text = "Ошибка привязки аккаунтов:" + sl
                    + "Введенные email или пароль не верны." + sl
                    + "Попробуйте ещё раз /subscribe";
            return new SendMessage(chatId, text);
        }
        String token = tokenResultMap.get("access_token");

        Object profileResult;
        try {
            profileResult = authCallWebClient.doGet(PROFILE_URL, token).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис данных о профиле не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }
        var profileResultMap = tgConfig.getObjectToMap(profileResult);
        int profileId = Integer.parseInt(profileResultMap.get("id"));

        /*Сохранение привязки аккаунта*/
        TgUser tgUser = new TgUser(0, profileResultMap.get("username"), email, message.getChatId(), profileId);
        tgUserService.save(tgUser);

        text = "Аккаунт с почтой " + email + " успешно привязан.";
        return new SendMessage(chatId, text);
    }
}

