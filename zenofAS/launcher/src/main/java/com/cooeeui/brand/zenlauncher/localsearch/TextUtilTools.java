package com.cooeeui.brand.zenlauncher.localsearch;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cuiqian on 2016/2/24.
 */
public class TextUtilTools {

    /**
     * 关键字高亮显示
     *
     * @param target  需要高亮的关键字
     * @param text       需要显示的文字
     * @return spannable 处理完后的结果，记得不要toString()，否则没有效果
     */
    public static SpannableStringBuilder highlight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span = null;

        Pattern p = Pattern.compile(target, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        while (m.find()) {
            span = new ForegroundColorSpan(0xff00aae8);// 需要重复！
            spannable.setSpan(span, m.start(), m.end(),
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            break;
        }
        return spannable;
    }

    // 调用
    // SpannableStringBuilder textString = TextUtilTools.highlight(item.getItemName(), KnowledgeActivity.searchKey);
    // vHolder.tv_itemName_search.setText(textString);
}