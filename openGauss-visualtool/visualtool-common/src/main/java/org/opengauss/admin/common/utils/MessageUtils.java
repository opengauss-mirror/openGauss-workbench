package org.opengauss.admin.common.utils;

import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * I18n Message Tool
 *
 * @author xielibo
 */
public class MessageUtils {
    /**
     * According to the message key and parameters, get the message and delegate to spring messageSource
     *
     * @param code code
     * @param args args
     * @return result
     */
    public static String message(String code, Object... args) {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
