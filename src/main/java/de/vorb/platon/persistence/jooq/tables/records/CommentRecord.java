/*
 * This file is generated by jOOQ.
*/
package de.vorb.platon.persistence.jooq.tables.records;


import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.jooq.tables.Comment;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.UpdatableRecordImpl;


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
public class CommentRecord extends UpdatableRecordImpl<CommentRecord> implements Record10<Long, Long, Long, LocalDateTime, LocalDateTime, CommentStatus, String, String, String, String> {

    private static final long serialVersionUID = 1375009388;

    /**
     * Setter for <code>public.comment.id</code>.
     */
    public CommentRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.comment.thread_id</code>.
     */
    public CommentRecord setThreadId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.thread_id</code>.
     */
    public Long getThreadId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.comment.parent_id</code>.
     */
    public CommentRecord setParentId(Long value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.parent_id</code>.
     */
    public Long getParentId() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>public.comment.creation_date</code>.
     */
    public CommentRecord setCreationDate(LocalDateTime value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.creation_date</code>.
     */
    public LocalDateTime getCreationDate() {
        return (LocalDateTime) get(3);
    }

    /**
     * Setter for <code>public.comment.last_modification_date</code>.
     */
    public CommentRecord setLastModificationDate(LocalDateTime value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.last_modification_date</code>.
     */
    public LocalDateTime getLastModificationDate() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.comment.status</code>.
     */
    public CommentRecord setStatus(CommentStatus value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.status</code>.
     */
    public CommentStatus getStatus() {
        return (CommentStatus) get(5);
    }

    /**
     * Setter for <code>public.comment.text</code>.
     */
    public CommentRecord setText(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.text</code>.
     */
    public String getText() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.comment.author</code>.
     */
    public CommentRecord setAuthor(String value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.author</code>.
     */
    public String getAuthor() {
        return (String) get(7);
    }

    /**
     * Setter for <code>public.comment.email_hash</code>.
     */
    public CommentRecord setEmailHash(String value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.email_hash</code>.
     */
    public String getEmailHash() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.comment.url</code>.
     */
    public CommentRecord setUrl(String value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for <code>public.comment.url</code>.
     */
    public String getUrl() {
        return (String) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row10<Long, Long, Long, LocalDateTime, LocalDateTime, CommentStatus, String, String, String, String> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row10<Long, Long, Long, LocalDateTime, LocalDateTime, CommentStatus, String, String, String, String> valuesRow() {
        return (Row10) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Comment.COMMENT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Comment.COMMENT.THREAD_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return Comment.COMMENT.PARENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field4() {
        return Comment.COMMENT.CREATION_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field5() {
        return Comment.COMMENT.LAST_MODIFICATION_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<CommentStatus> field6() {
        return Comment.COMMENT.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Comment.COMMENT.TEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Comment.COMMENT.AUTHOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return Comment.COMMENT.EMAIL_HASH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field10() {
        return Comment.COMMENT.URL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getThreadId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getParentId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component4() {
        return getCreationDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component5() {
        return getLastModificationDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentStatus component6() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getAuthor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component9() {
        return getEmailHash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component10() {
        return getUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getThreadId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getParentId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value4() {
        return getCreationDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value5() {
        return getLastModificationDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentStatus value6() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getAuthor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getEmailHash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value10() {
        return getUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value2(Long value) {
        setThreadId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value3(Long value) {
        setParentId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value4(LocalDateTime value) {
        setCreationDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value5(LocalDateTime value) {
        setLastModificationDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value6(CommentStatus value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value7(String value) {
        setText(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value8(String value) {
        setAuthor(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value9(String value) {
        setEmailHash(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord value10(String value) {
        setUrl(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentRecord values(Long value1, Long value2, Long value3, LocalDateTime value4, LocalDateTime value5, CommentStatus value6, String value7, String value8, String value9, String value10) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CommentRecord
     */
    public CommentRecord() {
        super(Comment.COMMENT);
    }

    /**
     * Create a detached, initialised CommentRecord
     */
    public CommentRecord(Long id, Long threadId, Long parentId, LocalDateTime creationDate, LocalDateTime lastModificationDate, CommentStatus status, String text, String author, String emailHash, String url) {
        super(Comment.COMMENT);

        set(0, id);
        set(1, threadId);
        set(2, parentId);
        set(3, creationDate);
        set(4, lastModificationDate);
        set(5, status);
        set(6, text);
        set(7, author);
        set(8, emailHash);
        set(9, url);
    }
}
