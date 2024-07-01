package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CheckActionTest {
    private TgUserService tgUserService;
    private CheckAction checkAction;

    @BeforeEach
    public void setUp() {
        tgUserService = mock(TgUserService.class);
        checkAction = new CheckAction(tgUserService);
    }

    @Test
    public void testHandle_UserNotRegistered() {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(123456L);
        when(tgUserService.findByChatId(anyLong())).thenReturn(Optional.empty());
        SendMessage response = (SendMessage) checkAction.handle(message);
        assertEquals("Вы не зарегистрированы в системе.\nДля регистрации, используйте команду /new.", response.getText());
        assertEquals(String.valueOf(123456L), response.getChatId());
    }

    @Test
    public void testHandle_UserRegistered() {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(123456L);
        TgUser tgUser = new TgUser(1, "testUser", "test@mail.com", 123456L, 1001);
        when(tgUserService.findByChatId(anyLong())).thenReturn(Optional.of(tgUser));
        SendMessage response = (SendMessage) checkAction.handle(message);
        String expectedText = "Аккаунт привязан к следующим данным: \n"
                + "имя пользователя: testUser\n"
                + "почта: test@mail.com";
        assertEquals(expectedText, response.getText());
        assertEquals(String.valueOf(123456L), response.getChatId());
    }

    @Test
    public void testCallback() {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(123456L);
        when(tgUserService.findByChatId(anyLong())).thenReturn(Optional.empty());
        SendMessage response = (SendMessage) checkAction.callback(message);
        assertEquals("Вы не зарегистрированы в системе.\nДля регистрации, используйте команду /new.", response.getText());
        assertEquals(String.valueOf(123456L), response.getChatId());
    }
}
