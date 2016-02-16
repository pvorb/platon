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

    @Query("select c from Comment c where c.thread = :thread and c.status > 0 order by c.creationDate asc")
    List<Comment> findByThread(@Param("thread") CommentThread thread);

    @Modifying
    @Query("update Comment c set c.status = :status where c.id = :commentId")
    void setStatus(@Param("commentId") Long commentId, @Param("status") Comment.Status status);

}
