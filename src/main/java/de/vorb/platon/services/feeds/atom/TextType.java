package de.vorb.platon.services.feeds.atom;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public enum TextType {

    TEXT,
    HTML,
    XHTML;

    public static class Adapter extends XmlAdapter<String, TextType> {

        @Override
        public TextType unmarshal(String v) throws Exception {
            return TextType.valueOf(v.toUpperCase());
        }

        @Override
        public String marshal(TextType v) throws Exception {
            return v.toString().toLowerCase();
        }

    }

}
