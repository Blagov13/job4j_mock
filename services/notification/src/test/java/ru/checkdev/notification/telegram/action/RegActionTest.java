package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegActionTest {
    private RegAction regAction;
    private TgUserService tgUserService;
    private TgAuthCallWebClint authCallWebClint;
    private TgConfig tgConfig;

    @BeforeEach
    void setUp() {
        tgUserService = mock(TgUserService.class);
        authCallWebClint = mock(TgAuthCallWebClint.class);
        tgConfig = mock(TgConfig.class);

        regAction = new RegAction(authCallWebClint, "http://auth.url", tgUserService);
    }

    @Test
    void testHandleUserAlreadyRegistered() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);

        when(tgUserService.checkUserExists(123456789L)).thenReturn(true);

        BotApiMethod<Message> response = regAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertEquals("Вы уже зарегистрированы в системе.\nЧтобы узнать регистрационные данные используйте /check", sendMessage.getText());
    }

    @Test
    void testHandleUserNotRegistered() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);

        when(tgUserService.checkUserExists(123456789L)).thenReturn(false);

        BotApiMethod<Message> response = regAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertEquals("Введите email для регистрации:", sendMessage.getText());
    }

    @Test
    void testCallbackInvalidEmail() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);
        when(messageMock.getText()).thenReturn("invalid_email");

        User userMock = mock(User.class);
        when(userMock.getUserName()).thenReturn("username");
        when(messageMock.getFrom()).thenReturn(userMock);

        when(tgConfig.isEmail("invalid_email")).thenReturn(false);

        BotApiMethod<Message> response = regAction.callback(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertEquals("Email: invalid_email не корректный.\nпопробуйте снова.\n/new", sendMessage.getText());
    }

    @Test
    void testCallbackAuthServiceError() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);
        when(messageMock.getText()).thenReturn("test@example.com");

        User userMock = mock(User.class);
        when(userMock.getUserName()).thenReturn("username");
        when(messageMock.getFrom()).thenReturn(userMock);

        when(tgConfig.isEmail("test@example.com")).thenReturn(true);
        when(tgConfig.getPassword()).thenReturn("password");

        when(authCallWebClint.doPost(any(String.class), any(PersonDTO.class))).thenThrow(new RuntimeException("Auth service error"));

        BotApiMethod<Message> response = regAction.callback(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertEquals("Сервис авторизации не доступен попробуйте позже\n/start", sendMessage.getText());
    }
}