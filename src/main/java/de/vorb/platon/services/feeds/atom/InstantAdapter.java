package de.vorb.platon.services.feeds.atom;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

public class InstantAdapter extends XmlAdapter<String, Instant> {

    @Override
    public Instant unmarshal(String value) {
        return Instant.parse(value);
    }

    @Override
    public String marshal(Instant value) {
        return value.toString();
    }

}
