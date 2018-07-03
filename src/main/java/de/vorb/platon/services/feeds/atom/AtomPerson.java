package de.vorb.platon.services.feeds.atom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

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

}
