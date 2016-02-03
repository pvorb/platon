package de.vorb.platon.persistence;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {

    @Query("select c from Comment c where c.thread = :thread and c.deleted = false order by c.creationDate asc")
    List<Comment> findByThread(@Param("thread") CommentThread thread);

    @Query("update Comment c set c.deleted = true where c.id = :commentId")
    @Modifying
    void markAsDeleted(@Param("commentId") Long commentId);

}
