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
import java.net.URI;

@XmlType(propOrder = {"term"})
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomCategory {

    @XmlAttribute(name = "term", required = true)
    private String term;

    @XmlAttribute(name = "scheme")
    private URI scheme;

    @XmlAttribute(name = "label")
    private String label;

}
