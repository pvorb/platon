package de.vorb.platon.persistence;

import de.vorb.platon.model.Comment;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {}
