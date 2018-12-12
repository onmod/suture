package net.dloud.platform.extend.wrapper;

import net.dloud.platform.common.platform.BaseExceptionEnum;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.exception.PassedException;

import java.util.Collection;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-12 10:38
 **/
public class AssertWrapper {
    public static void isTrue(boolean input, String message) {
        if (!input) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void isTrue(boolean input, BaseExceptionEnum message) {
        if (!input) {
            throw new PassedException(message);
        }
    }

    public static void isFalse(boolean input, String message) {
        if (input) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void isFalse(boolean input, BaseExceptionEnum message) {
        if (input) {
            throw new PassedException(message);
        }
    }

    public static void notNull(Object input, String message) {
        if (null == input) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void notNull(Object input, BaseExceptionEnum message) {
        if (null == input) {
            throw new PassedException(message);
        }
    }

    public static void notBlank(String input, String message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void notBlank(String input, BaseExceptionEnum message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(message);
        }
    }

    public static void notEmpty(String input, String message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void notEmpty(String input, BaseExceptionEnum message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(message);
        }
    }

    public static void notEmpty(Collection<?> input, String message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void notEmpty(Collection<?> input, BaseExceptionEnum message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(message);
        }
    }

    public static void notEmpty(Map<?, ?> input, String message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(PlatformConstants.APPID + "403", message);
        }
    }

    public static void notEmpty(Map<?, ?> input, BaseExceptionEnum message) {
        if (null == input || input.isEmpty()) {
            throw new PassedException(message);
        }
    }
}
