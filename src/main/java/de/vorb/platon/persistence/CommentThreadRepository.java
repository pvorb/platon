package de.vorb.platon.persistence;

import de.vorb.platon.model.CommentThread;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentThreadRepository extends PagingAndSortingRepository<CommentThread, Long> {

    @Transactional(readOnly = true)
    @EntityGraph(value = "CommentThread.detail", type = EntityGraph.EntityGraphType.LOAD)
    CommentThread getByUrl(String url);

}
