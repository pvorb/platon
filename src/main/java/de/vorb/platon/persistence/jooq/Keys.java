/*
 * This file is generated by jOOQ.
*/
package de.vorb.platon.persistence.jooq;


import de.vorb.platon.persistence.jooq.tables.Comment;
import de.vorb.platon.persistence.jooq.tables.CommentThread;
import de.vorb.platon.persistence.jooq.tables.Property;
import de.vorb.platon.persistence.jooq.tables.records.CommentRecord;
import de.vorb.platon.persistence.jooq.tables.records.CommentThreadRecord;
import de.vorb.platon.persistence.jooq.tables.records.PropertyRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>public</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<CommentRecord, Long> IDENTITY_COMMENT = Identities0.IDENTITY_COMMENT;
    public static final Identity<CommentThreadRecord, Long> IDENTITY_COMMENT_THREAD = Identities0.IDENTITY_COMMENT_THREAD;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<CommentRecord> COMMENT_PKEY = UniqueKeys0.COMMENT_PKEY;
    public static final UniqueKey<CommentThreadRecord> COMMENT_THREAD_PKEY = UniqueKeys0.COMMENT_THREAD_PKEY;
    public static final UniqueKey<PropertyRecord> PROPERTY_PKEY = UniqueKeys0.PROPERTY_PKEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<CommentRecord, CommentThreadRecord> COMMENT__COMMENT_THREAD_ID_FKEY = ForeignKeys0.COMMENT__COMMENT_THREAD_ID_FKEY;
    public static final ForeignKey<CommentRecord, CommentRecord> COMMENT__COMMENT_PARENT_ID_FKEY = ForeignKeys0.COMMENT__COMMENT_PARENT_ID_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<CommentRecord, Long> IDENTITY_COMMENT = Internal.createIdentity(Comment.COMMENT, Comment.COMMENT.ID);
        public static Identity<CommentThreadRecord, Long> IDENTITY_COMMENT_THREAD = Internal.createIdentity(CommentThread.COMMENT_THREAD, CommentThread.COMMENT_THREAD.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<CommentRecord> COMMENT_PKEY = Internal.createUniqueKey(Comment.COMMENT, "comment_pkey", Comment.COMMENT.ID);
        public static final UniqueKey<CommentThreadRecord> COMMENT_THREAD_PKEY = Internal.createUniqueKey(CommentThread.COMMENT_THREAD, "comment_thread_pkey", CommentThread.COMMENT_THREAD.ID);
        public static final UniqueKey<PropertyRecord> PROPERTY_PKEY = Internal.createUniqueKey(Property.PROPERTY, "property_pkey", Property.PROPERTY.KEY);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<CommentRecord, CommentThreadRecord> COMMENT__COMMENT_THREAD_ID_FKEY = Internal.createForeignKey(de.vorb.platon.persistence.jooq.Keys.COMMENT_THREAD_PKEY, Comment.COMMENT, "comment__comment_thread_id_fkey", Comment.COMMENT.THREAD_ID);
        public static final ForeignKey<CommentRecord, CommentRecord> COMMENT__COMMENT_PARENT_ID_FKEY = Internal.createForeignKey(de.vorb.platon.persistence.jooq.Keys.COMMENT_PKEY, Comment.COMMENT, "comment__comment_parent_id_fkey", Comment.COMMENT.PARENT_ID);
    }
}
