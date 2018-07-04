package com.brahma.dao.utils;

import lombok.experimental.UtilityClass;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@UtilityClass
public class MessagingUtils {

    public static void error(final Messager messager,
                             final Element element,
                             final String message,
                             final Object... args) {
        printMessage(messager, Diagnostic.Kind.ERROR, element, message, args);
    }

    private static void printMessage(final Messager messager,
                                     final Diagnostic.Kind kind,
                                     final Element element,
                                     String message,
                                     final Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        messager.printMessage(kind, message, element);
    }
}
