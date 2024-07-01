package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UnsubscribeActionTest {
    @Mock
    private TgUserService tgUserService;
    @Mock
    private Message messageMock;

    private UnsubscribeAction unsubscribeAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unsubscribeAction = new UnsubscribeAction(tgUserService);
    }

    @Test
    void testHandleUnsubscribeSuccess() {
        long chatId = 123456789L;
        String expectedText = "Телеграм успешно отвязан.";

        when(messageMock.getChatId()).thenReturn(chatId);
        when(tgUserService.checkUserExists(chatId)).thenReturn(true);

        BotApiMethod<Message> response = unsubscribeAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals(String.valueOf(chatId), sendMessage.getChatId());
        assertEquals(expectedText, sendMessage.getText());

        verify(tgUserService, times(1)).checkUserExists(chatId);
        verify(tgUserService, times(1)).deleteByChatId(chatId);
        verifyNoMoreInteractions(tgUserService);
    }

    @Test
    void testHandleUserNotRegistered() {
        long chatId = 123456789L;
        String expectedText = "Вы ещё не зарегистрированы в системе.\n"
                + "Для регистрации, пожалуйста, используйте команду /new.";

        when(messageMock.getChatId()).thenReturn(chatId);
        when(tgUserService.checkUserExists(chatId)).thenReturn(false);

        BotApiMethod<Message> response = unsubscribeAction.handle(messageMock);

        assertEquals(SendMessage.class, response.getClass());
        SendMessage sendMessage = (SendMessage) response;
        assertEquals(String.valueOf(chatId), sendMessage.getChatId());
        assertEquals(expectedText, sendMessage.getText());

        verify(tgUserService, times(1)).checkUserExists(chatId);
        verifyNoMoreInteractions(tgUserService);
    }
}