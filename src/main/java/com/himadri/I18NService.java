package com.himadri;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18NService {
    private final Locale locale;

    @Autowired
    private MessageSource messageSource;

    public I18NService(@Value("${pdfLang}") String pdfLang) {
        locale = new Locale(pdfLang);
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, null, locale);
    }
}
