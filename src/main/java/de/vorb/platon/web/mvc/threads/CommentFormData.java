package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.web.mvc.comments.CommentAction;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
class CommentFormData {

    @NotNull
    private CommentAction action;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    @NotBlank
    private String author;

    @URL
    private String url;

    private boolean acceptCookie;

}
