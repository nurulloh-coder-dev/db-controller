package com.example.util;

import org.springframework.stereotype.Component;
import com.example.model.entity.enums.WebLang;

import java.util.Locale;

@Component
public class Utils {

    private String appKnowledgePath = "";

    public Locale getLocaleByLanguage(WebLang language) {
        return switch (language) {
            case UZB -> new Locale("uz");
            case RU -> new Locale("ru");
            default -> Locale.ENGLISH;
        };
    }
    public Locale getLocaleByLanguage(String language) {
        return switch (language) {
            case "UZB" -> new Locale("uz");
            case "RU" -> new Locale("ru");
            default -> Locale.ENGLISH;
        };
    }
}
