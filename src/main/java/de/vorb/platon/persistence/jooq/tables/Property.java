/*
 * This file is generated by jOOQ.
*/
package de.vorb.platon.persistence.jooq.tables;


import de.vorb.platon.persistence.jooq.Indexes;
import de.vorb.platon.persistence.jooq.Keys;
import de.vorb.platon.persistence.jooq.Public;
import de.vorb.platon.persistence.jooq.tables.records.PropertyRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Property extends TableImpl<PropertyRecord> {

    private static final long serialVersionUID = -1455209178;

    /**
     * The reference instance of <code>public.property</code>
     */
    public static final Property PROPERTY = new Property();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PropertyRecord> getRecordType() {
        return PropertyRecord.class;
    }

    /**
     * The column <code>public.property.key</code>.
     */
    public final TableField<PropertyRecord, String> KEY = createField("key", org.jooq.impl.SQLDataType.VARCHAR(32).nullable(false), this, "");

    /**
     * The column <code>public.property.value</code>.
     */
    public final TableField<PropertyRecord, String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * Create a <code>public.property</code> table reference
     */
    public Property() {
        this(DSL.name("property"), null);
    }

    /**
     * Create an aliased <code>public.property</code> table reference
     */
    public Property(String alias) {
        this(DSL.name(alias), PROPERTY);
    }

    /**
     * Create an aliased <code>public.property</code> table reference
     */
    public Property(Name alias) {
        this(alias, PROPERTY);
    }

    private Property(Name alias, Table<PropertyRecord> aliased) {
        this(alias, aliased, null);
    }

    private Property(Name alias, Table<PropertyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PROPERTY_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PropertyRecord> getPrimaryKey() {
        return Keys.PROPERTY_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PropertyRecord>> getKeys() {
        return Arrays.<UniqueKey<PropertyRecord>>asList(Keys.PROPERTY_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property as(String alias) {
        return new Property(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property as(Name alias) {
        return new Property(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Property rename(String name) {
        return new Property(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Property rename(Name name) {
        return new Property(name, null);
    }
}
