package com.cooeeui.brand.zenlauncher.utils;

import android.text.TextUtils;

import com.cooeeui.brand.zenlauncher.Launcher;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;

public class FirstLetterMapper {

    private static final int GB_SP_DIFF = 160;
    // 存放国标一级汉字不同读音的起始区位码
    private static final int[] secPosValueList = {
        1601, 1637, 1833, 2078, 2274, 2302,
        2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
        4086, 4390, 4558, 4684, 4925, 5249, 5600
    };
    // 存放国标一级汉字不同读音的起始区位码对应读音
    private static final char[] firstLetter = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w', 'x',
        'y', 'z'
    };

    public static String getFirstLetter(String oriStr) {
        if (TextUtils.isEmpty(oriStr)) {
            return "#";
        }

        StringBuffer buffer = new StringBuffer();
        String firstLetter = "#";
        int len = oriStr.length();
        char ch = 0;
        for (int i = 0; i < len; i++) {
            ch = oriStr.charAt(i);
            if (ch != 0xa0 && ch != 0x20) {
                firstLetter = oriStr.substring(i, i + 1);
                break;
            }
        }
        switch (getLanguage()) {
            case "ru"://如果为俄语语言环境时按照俄文字母排序！
                if (isChinese(ch)) {
                    // 判断是否为汉字，如果左移7为为0就不是汉字，否则是汉字
                    Character first = getFirstLetterOfHanZi(ch);
                    if (first != null) {
                        char spell = first;
                        buffer.append(String.valueOf(spell));
                        firstLetter = buffer.toString();
                    }
                }

                if (Pattern.compile("[а-я|А-Я]").matcher(firstLetter).matches()) {
                    return firstLetter;
                } else if (Pattern.compile("[a-z|A-Z]").matcher(firstLetter).matches()) {
                    return firstLetter;
                } else {
                    return "#";
                }
            default://如果为非俄语语言环境时按照英文字母排序！
                if (isChinese(ch)) {
                    // 判断是否为汉字，如果左移7为为0就不是汉字，否则是汉字
                    Character first = getFirstLetterOfHanZi(ch);
                    if (first != null) {
                        char spell = first;
                        buffer.append(String.valueOf(spell));
                        firstLetter = buffer.toString();
                    }
                }
                if (Pattern.compile("[a-z|A-Z]").matcher(firstLetter).matches()) {
                    return firstLetter;
                } else {
                    return "#";
                }
        }
    }

    // 获取一个汉字的首字母
    public static Character getFirstLetterOfHanZi(char ch) {

        byte[] uniCode = null;
        try {
            uniCode = String.valueOf(ch).getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉字
            return null;
        } else {
            return convert(uniCode);
        }
    }

    /**
     * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
     * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
     */
    static char convert(byte[] bytes) {
        char result = '-';
        int secPosValue = 0;
        int i;
        for (i = 0; i < bytes.length; i++) {
            bytes[i] -= GB_SP_DIFF;
        }
        secPosValue = bytes[0] * 100 + bytes[1];
        for (i = 0; i < 23; i++) {
            if (secPosValue >= secPosValueList[i]
                && secPosValue < secPosValueList[i + 1]) {
                result = firstLetter[i];
                break;
            }
        }
        return result;
    }

    private static String getLanguage() {
        Locale locale = Launcher.getInstance().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language;
    }

    /**
     * 判断是否为中文字符
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
}
