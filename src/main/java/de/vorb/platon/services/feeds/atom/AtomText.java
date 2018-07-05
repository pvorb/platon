package de.vorb.platon.services.feeds.atom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomText {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(TextType.Adapter.class)
    private TextType type;

    @XmlValue
    private String text;

    public static AtomText of(TextType type, String text) {
        return new AtomText(type, text);
    }

    public static AtomText plainText(String text) {
        return AtomText.of(TextType.TEXT, text);
    }

    public static AtomText html(String html) {
        return AtomText.of(TextType.HTML, html);
    }

}
