package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InfoActionTest {

    private InfoAction infoAction;
    private List<String> actions;

    @BeforeEach
    void setUp() {
        actions = List.of(
                "/start - нажмите, чтобы показать что я могу;",
                "/new - нажмите, чтобы зарегистрироваться;",
                "/check - нажмите, чтобы я показал вашу почту и имя пользователя;",
                "/subscribe - нажмите, для привязки вашего аккаунта к Телеграм;",
                "/unsubscribe - нажмите, для того чтобы отвязать ваш аккаунт от Телеграм;"
        );
        infoAction = new InfoAction(actions);
    }

    @Test
    void testHandleStartCommand() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);
        when(messageMock.getText()).thenReturn("/start");

        BotApiMethod<Message> response = infoAction.handle(messageMock);

        assertTrue(response instanceof SendMessage);
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertTrue(sendMessage.getText().contains("Выберите действие:"));
        actions.forEach(action -> assertTrue(sendMessage.getText().contains(action)));
    }

    @Test
    void testHandleUnknownCommand() {
        Message messageMock = mock(Message.class);
        when(messageMock.getChatId()).thenReturn(123456789L);
        when(messageMock.getText()).thenReturn("/unknown");

        BotApiMethod<Message> response = infoAction.handle(messageMock);

        assertTrue(response instanceof SendMessage);
        SendMessage sendMessage = (SendMessage) response;
        assertEquals("123456789", sendMessage.getChatId());
        assertTrue(sendMessage.getText().contains("Команда не найдена. Пожалуйста, используйте действительную команду."));
    }
}
