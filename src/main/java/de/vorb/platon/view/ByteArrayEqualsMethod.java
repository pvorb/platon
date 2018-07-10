package de.vorb.platon.view;

import freemarker.template.DefaultArrayAdapter;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.Arrays;
import java.util.List;

public enum ByteArrayEqualsMethod implements TemplateMethodModelEx {

    INSTANCE;

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        try {
            return Arrays.equals(
                    (byte[]) ((DefaultArrayAdapter) arguments.get(0)).getWrappedObject(),
                    (byte[]) ((DefaultArrayAdapter) arguments.get(1)).getWrappedObject()
            );
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
