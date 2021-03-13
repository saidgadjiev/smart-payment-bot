package ru.gadjini.telegram.smart.payment.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.payment.dao.UserDao;
import ru.gadjini.telegram.smart.payment.exception.TelegramApiRequestException;

import java.util.Locale;

@Service
public class UserService {

    private UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Locale getLocaleOrDefault(int userId) {
        String locale = userDao.getLocale(userId);

        if (StringUtils.isNotBlank(locale)) {
            return new Locale(locale);
        }

        return Locale.getDefault();
    }

    public void blockUser(int userId) {
        userDao.blockUser(userId);
    }

    public boolean handleBotBlockedByUser(Throwable ex) {
        int apiRequestExceptionIndexOf = ExceptionUtils.indexOfThrowable(ex, TelegramApiRequestException.class);

        if (apiRequestExceptionIndexOf != -1) {
            TelegramApiRequestException exception = (TelegramApiRequestException) ExceptionUtils.getThrowableList(ex).get(apiRequestExceptionIndexOf);
            if (exception.getErrorCode() == 403) {
                blockUser(Integer.parseInt(exception.getChatId()));

                return true;
            }
        }

        return false;
    }

    public boolean isAdmin(int userId) {
        return userId == 171271164;
    }
}
