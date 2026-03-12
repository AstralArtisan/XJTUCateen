package com.example.canteen.repository;

import com.example.canteen.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByWindow_IdOrderByCreatedAtDesc(Long windowId, Pageable pageable);

    Optional<Review> findByUser_IdAndWindow_Id(Long userId, Long windowId);

    @Query("select coalesce(avg(r.score), 0) from Review r where r.window.id = :windowId")
    Double findAvgScoreByWindowId(@Param("windowId") Long windowId);

    long countByWindow_Id(Long windowId);

    @Query("select r from Review r order by r.createdAt desc")
    List<Review> findLatest(Pageable pageable);
}
