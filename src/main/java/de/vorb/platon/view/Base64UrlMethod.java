package de.vorb.platon.view;

import freemarker.template.DefaultArrayAdapter;
import freemarker.template.TemplateMethodModelEx;

import java.util.Base64;
import java.util.List;

public enum Base64UrlMethod implements TemplateMethodModelEx {

    INSTANCE;

    @Override
    public Object exec(List arguments) {
        final byte[] bytes = (byte[]) ((DefaultArrayAdapter) arguments.get(0)).getWrappedObject();
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

}
