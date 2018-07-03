package de.vorb.platon.services.feeds.atom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

@XmlRootElement(name = "feed")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomFeed {
    static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    @XmlAttribute(name = "lang")
    private String language;

    @XmlElement(name = "id", required = true)
    private String id;

    @XmlElement(name = "link")
    private List<AtomLink> links;

    @XmlElement(name = "category")
    private List<AtomCategory> categories;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "subtitle")
    private String subtitle;

    @XmlElement(name = "updated")
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant updated;

    @XmlElement(name = "entry")
    private List<AtomEntry> entries;

}
