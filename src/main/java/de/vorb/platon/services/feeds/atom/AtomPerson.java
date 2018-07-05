package de.vorb.platon.services.feeds.atom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.List;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomPerson {

    @XmlElement(name = "name", required = true)
    private String name;

    @XmlElement(name = "uri")
    private URI uri;

    @XmlElement(name = "email")
    private String email;

    @XmlAnyElement
    private List<Element> extensionElements;

}
