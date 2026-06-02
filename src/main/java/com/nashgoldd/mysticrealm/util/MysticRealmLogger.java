package com.nashgoldd.mysticrealm.util;

import com.mojang.logging.LogUtils;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import org.slf4j.Logger;

/**
 * Logger centralizado do MysticRealm.
 * Mensagens de debug só são emitidas quando debugLogging=true na config.
 * Todos os logs recebem o prefixo [mysticrealm] automaticamente.
 */
public final class MysticRealmLogger {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[mysticrealm] ";

    private MysticRealmLogger() {}

    public static void debug(String message, Object... args) {
        // Verifica config antes de logar para não poluir o console em produção
        if (MysticConfig.DEBUG_LOGGING.get()) {
            LOGGER.debug(PREFIX + message, args);
        }
    }

    public static void info(String message, Object... args) {
        LOGGER.info(PREFIX + message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(PREFIX + message, args);
    }

    public static void error(String message, Object... args) {
        LOGGER.error(PREFIX + message, args);
    }
}
