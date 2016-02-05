package de.vorb.platon.persistence;

import de.vorb.platon.model.Comment;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter(autoApply = true)
public class CommentStatusConverter implements AttributeConverter<Comment.Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Comment.Status attribute) {
        return attribute.getValue();
    }

    @Override
    public Comment.Status convertToEntityAttribute(Integer dbData) {
        return Comment.Status.fromValue(dbData);
    }

}
