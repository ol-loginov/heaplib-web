package com.github.ol_loginov.heaplibweb.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Setter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class JstlTool {
    private final MessageSource messageSource;
    private final Locale locale;

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * Translate message with MessageSource (l10n/messages)
     */
    public String t(String code) {
        return messageSource.getMessage(code, null, code, locale);
    }

    /**
     * Encode string to HTML-safe text (for use on page or in tag attribute)
     */
    public String h(Object obj) {
        if (obj == null) return null;
        return StringEscapeUtils.escapeHtml4(obj.toString());
    }

    public Object coalesce(Object obj, Object nullValue) {
        return (obj == null) ? nullValue : obj;
    }

    /**
     * Represent object as JSON string
     */
    public String json(Object object) throws JsonProcessingException {
        return JSON.writeValueAsString(object);
    }

    public LocalDateTime localDateTime() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }
}
