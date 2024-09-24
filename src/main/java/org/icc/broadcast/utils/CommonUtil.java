package org.icc.broadcast.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    /**
     * 1.判断字节是否是中文
     *
     * CJK的意思是“Chinese，Japanese，Korea”的简写 ，实际上就是指中日韩三国的象形文字的Unicode编码
     * Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS             ：4E00-9FBF：CJK 统一表意符号
     * Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS       ：F900-FAFF：CJK 兼容象形文字
     * Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ：3400-4DBF：CJK 统一表意符号扩展 A
     * Character.UnicodeBlock.GENERAL_PUNCTUATION                ：2000-206F：常用标点
     * Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION        ：3000-303F：CJK 符号和标点
     * Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS      ：FF00-FFEF：半角及全角形式
     *
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
    //2.检测是否包含英文
    public static boolean isEnglish(String charaString) {
        return charaString.matches("^[a-zA-Z|0-9| |.|,|?|;|:|'|\"|[|]|{|}|\\\\|\\||`|~|!|@|#|$|%|^|&|*|(|)|-|_|=|+]*");
    }
    //3.检测是否包含中文
    public static boolean isContainChinese(String str) {
        String regEx = "[\\u4E00-\\u9FA5|\\？|\\。|\\，|\\；|\\：|\\、|\\‘|\\’|\\“|\\”|\\（|\\）|\\【|\\】|\\《|\\》|\\！|\\￥|\\%]+";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getConversationId(List<String> ids) {
        Collections.sort(ids);

        return StringUtils.join(ids, "_");
    }
}
