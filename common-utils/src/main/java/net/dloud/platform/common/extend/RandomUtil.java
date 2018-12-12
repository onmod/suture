package net.dloud.platform.common.extend;


import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author QuDasheng
 * @create 2016-09-22 11:03
 */
public class RandomUtil {
    private static java.util.Random random;

    /**
     * 双重校验锁获取一个Random单例
     *
     * @return
     */
    public static Random getRandom() {
        if (random == null) {
            synchronized (RandomUtil.class) {
                if (random == null) {
                    random = new java.util.Random();
                }
            }
        }

        return random;
    }

    /**
     * 获得一个[0,max)之间的整数。
     *
     * @param max
     * @return
     */
    public static int getRandomInt(int max) {
        int abs = Math.abs(getRandom().nextInt());
        if (Integer.MIN_VALUE == abs) {
            return abs;
        } else {
            return abs % max;
        }
    }

    /**
     * 获得一个[min,max)之间的整数。
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandomInt(int min, int max) {
        return getRandom().nextInt(max) % (max - min) + min;
    }

    /**
     * 获得一个[0,max)之间的整数。
     *
     * @param max
     * @return
     */
    public static long getRandomLong(long max) {
        long abs = Math.abs(getRandom().nextLong());
        if (Long.MIN_VALUE == abs) {
            return abs;
        } else {
            return abs % max;
        }
    }

    /**
     * 获得一个[0,max)之间的浮点数。
     *
     * @param max
     * @return
     */
    public static double getRandomDouble(double max) {
        double abs = Math.abs(getRandom().nextDouble());
        if (Double.MIN_VALUE == abs) {
            return abs;
        } else {
            return abs % max;
        }
    }

    /**
     * 从list中随机取得一个元素
     *
     * @param list
     * @return
     */
    public static <E> E getRandomElement(List<E> list) {
        return list.get(getRandomInt(list.size()));
    }

    /**
     * 从set中随机取得一个元素
     *
     * @param set
     * @return
     */
    public static <E> E getRandomElement(Set<E> set) {
        int rn = getRandomInt(set.size());
        int i = 0;
        for (E e : set) {
            if (i == rn) {
                return e;
            }
            i++;
        }
        return null;
    }

    /**
     * 从map中随机取得一个key
     *
     * @param map
     * @return
     */
    public static <K, V> K getRandomKeyFromMap(Map<K, V> map) {
        int rn = getRandomInt(map.size());
        int i = 0;
        for (K key : map.keySet()) {
            if (i == rn) {
                return key;
            }
            i++;
        }
        return null;
    }

    /**
     * 从map中随机取得一个value
     *
     * @param map
     * @return
     */
    public static <K, V> V getRandomValueFromMap(Map<K, V> map) {
        int rn = getRandomInt(map.size());
        int i = 0;
        for (V value : map.values()) {
            if (i == rn) {
                return value;
            }
            i++;
        }
        return null;
    }

    /**
     * 获取一定长度的随机字符串
     *
     * @param length 指定字符串长度
     * @return 一定长度的字符串
     */
    public static String getRandomStringByLength(int length) {
        return getRandomStringByLength(length, 1);
    }

    public static String getRandomNumberByLength(int length) {
        return getRandomStringByLength(length, 2);
    }

    public static String getRandomMixinByLength(int length) {
        return getRandomStringByLength(length, 0);
    }

    public static String getRandomStringByLength(int length, int type) {
        String base;
        switch (type) {
            case 1:
                base = "abcdefghijklmnopqrstuvwxyz";
                break;
            case 2:
                base = "0123456789";
                break;
            default:
                base = "abcdefghijklmnopqrstuvwxyz0123456789";
        }

        java.util.Random random = new java.util.Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}