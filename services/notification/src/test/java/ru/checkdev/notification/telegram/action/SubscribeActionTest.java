package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SubscribeActionTest {
    @Mock
    private TgUserService tgUserService;
    @Mock
    private TgAuthCallWebClint authCallWebClient;
    @Mock
    private Message messageMock;

    private SubscribeAction subscribeAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscribeAction = new SubscribeAction(tgUserService, authCallWebClient);
    }

    @Test
    void testHandleAccountAlreadyLinked() {
        long chatId = 123456789L;
        String expectedText = "Ваш аккаунт телеграм уже привязан к аккаунту.\n"
                + "Чтобы узнать данные используйте /check\n"
                + "Чтобы отвязать аккаунт используйте /unsubscribe";

        when(messageMock.getChatId()).thenReturn(chatId);
        when(tgUserService.checkUserExists(chatId)).thenReturn(true);

        BotApiMethod<Message> response = subscribeAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals(String.valueOf(chatId), sendMessage.getChatId());
        assertEquals(expectedText, sendMessage.getText());

        verify(tgUserService, times(1)).checkUserExists(chatId);
        verifyNoMoreInteractions(tgUserService);
    }

    @Test
    void testHandleAccountNotLinked() {
        long chatId = 123456789L;
        String expectedText = "Для привязки аккаунта, введите, пожалуйста, email и пароль через пробел.\n"
                + "Пример: \"example@mail.ru password\"";

        when(messageMock.getChatId()).thenReturn(chatId);
        when(tgUserService.checkUserExists(chatId)).thenReturn(false);

        BotApiMethod<Message> response = subscribeAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals(String.valueOf(chatId), sendMessage.getChatId());
        assertEquals(expectedText, sendMessage.getText());

        verify(tgUserService, times(1)).checkUserExists(chatId);
        verifyNoMoreInteractions(tgUserService);
    }
}