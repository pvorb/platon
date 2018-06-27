/*
 * This file is generated by jOOQ.
*/
package de.vorb.platon.persistence.jooq.tables;


import de.vorb.platon.persistence.jooq.Indexes;
import de.vorb.platon.persistence.jooq.Keys;
import de.vorb.platon.persistence.jooq.Public;
import de.vorb.platon.persistence.jooq.tables.records.CommentThreadRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
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
public class CommentThread extends TableImpl<CommentThreadRecord> {

    private static final long serialVersionUID = -537616766;

    /**
     * The reference instance of <code>public.comment_thread</code>
     */
    public static final CommentThread COMMENT_THREAD = new CommentThread();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CommentThreadRecord> getRecordType() {
        return CommentThreadRecord.class;
    }

    /**
     * The column <code>public.comment_thread.id</code>.
     */
    public final TableField<CommentThreadRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('comment_thread_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.comment_thread.url</code>.
     */
    public final TableField<CommentThreadRecord, String> URL = createField("url", org.jooq.impl.SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>public.comment_thread.title</code>.
     */
    public final TableField<CommentThreadRecord, String> TITLE = createField("title", org.jooq.impl.SQLDataType.VARCHAR(512), this, "");

    /**
     * Create a <code>public.comment_thread</code> table reference
     */
    public CommentThread() {
        this(DSL.name("comment_thread"), null);
    }

    /**
     * Create an aliased <code>public.comment_thread</code> table reference
     */
    public CommentThread(String alias) {
        this(DSL.name(alias), COMMENT_THREAD);
    }

    /**
     * Create an aliased <code>public.comment_thread</code> table reference
     */
    public CommentThread(Name alias) {
        this(alias, COMMENT_THREAD);
    }

    private CommentThread(Name alias, Table<CommentThreadRecord> aliased) {
        this(alias, aliased, null);
    }

    private CommentThread(Name alias, Table<CommentThreadRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.COMMENT_THREAD_PKEY, Indexes.COMMENT_THREAD_URL_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<CommentThreadRecord, Long> getIdentity() {
        return Keys.IDENTITY_COMMENT_THREAD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<CommentThreadRecord> getPrimaryKey() {
        return Keys.COMMENT_THREAD_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<CommentThreadRecord>> getKeys() {
        return Arrays.<UniqueKey<CommentThreadRecord>>asList(Keys.COMMENT_THREAD_PKEY, Keys.COMMENT_THREAD_URL_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentThread as(String alias) {
        return new CommentThread(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentThread as(Name alias) {
        return new CommentThread(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CommentThread rename(String name) {
        return new CommentThread(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CommentThread rename(Name name) {
        return new CommentThread(name, null);
    }
}
