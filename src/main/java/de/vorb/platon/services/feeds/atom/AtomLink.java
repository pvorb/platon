package de.vorb.platon.services.feeds.atom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomLink {

    @XmlAttribute(name = "href", required = true)
    private String href;

    @XmlAttribute(name = "rel")
    private String rel;

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "hreflang")
    private String hreflang;

    @XmlAttribute(name = "name")
    private String title;

    @XmlAttribute(name = "length")
    private String length;

}
