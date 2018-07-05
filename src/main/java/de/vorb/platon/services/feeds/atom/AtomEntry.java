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
import java.util.ArrayList;
import java.util.List;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AtomEntry {

    @XmlElement(name = "id", required = true)
    private String id;

    @XmlElement(name = "title", required = true)
    private String title;

    @XmlElement(name = "published")
    private AtomDateTime published;

    @XmlElement(name = "updated", required = true)
    private AtomDateTime updated;

    @XmlElement(name = "author")
    private List<AtomPerson> authors = new ArrayList<>();

    @XmlElement(name = "contributor")
    private List<AtomPerson> contributors = new ArrayList<>();

    @XmlElement(name = "category")
    private List<AtomCategory> categories = new ArrayList<>();

    @XmlElement(name = "summary")
    private AtomText summary;

    @XmlElement(name = "content")
    private AtomText content;

    @XmlElement(name = "link")
    private List<AtomLink> links = new ArrayList<>();

    @XmlAnyElement
    private List<Element> extensionElements;

}
