package ru.gadjini.telegram.smart.payment.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocalisationService {

    public static final String RU_LOCALE = "ru";

    public static final String EN_LOCALE = "en";

    public String getMessage(String messageCode, Locale locale) {
        return null;
    }
}
