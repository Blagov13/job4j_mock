package ru.checkdev.notification.telegram.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.repository.TgUserRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TgUserService {
    private final TgUserRepository tgUserRepository;

    public TgUser save(TgUser tgUser) {
        return tgUserRepository.save(tgUser);
    }

    public boolean checkUserExists(long chatId) {
        return tgUserRepository.existsByChatId(chatId);
    }

    public Optional<TgUser> findByChatId(long chatId) {
        return tgUserRepository.findByChatId(chatId);
    }

    public void deleteByChatId(long chatId) {
        tgUserRepository.deleteByChatId(chatId);
    }
}
