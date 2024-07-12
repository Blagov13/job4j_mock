package ru.checkdev.notification.repository;

import org.springframework.data.repository.CrudRepository;
import ru.checkdev.notification.domain.TgUser;

import javax.transaction.Transactional;
import java.util.Optional;

public interface TgUserRepository extends CrudRepository<TgUser, Integer> {

    Optional<TgUser> findByChatId(long chatId);

    boolean existsByChatId(long chatId);

    @Transactional
    void deleteByChatId(long chatId);
}
