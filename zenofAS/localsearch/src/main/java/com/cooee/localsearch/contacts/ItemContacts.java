package com.cooee.localsearch.contacts;
import com.cooee.localsearch.base.ItemBase;
import com.cooee.t9search.model.PinyinUnit;

import java.util.List;

/*
* 联系人
*/
public class ItemContacts extends ItemBase {
    public Contacts contact;
    public ItemContacts(Contacts contact) {
        this.contact = contact;
    }

    @Override
    public boolean match(
        String text,
        List<PinyinUnit> srcUnit) {
        StringBuffer
            chineseKeyWord =
            new StringBuffer();// In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
        if (T9Match(contact.getName(), srcUnit) || contact.getName().toLowerCase()
            .contains(text)) {
            contact.setSearchByType(Contacts.SearchByType.SearchByName);
            contact.setMatchKeywords(chineseKeyWord.toString());
            chineseKeyWord.delete(0, chineseKeyWord.length());
            return true;
        } else {
            return contact.getName().toLowerCase().contains(text.toLowerCase());
        }
    }
}
