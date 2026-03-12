package com.example.canteen.repository;

import com.example.canteen.entity.CanteenWindow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CanteenWindowRepository extends JpaRepository<CanteenWindow, Long> {

    @Query("""
            select w from CanteenWindow w
            join w.canteen c
            where w.status = 'OPEN'
              and (:keyword is null or :keyword = ''
                   or lower(w.windowName) like lower(concat('%', :keyword, '%'))
                   or lower(w.cuisineType) like lower(concat('%', :keyword, '%'))
                   or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:canteenName is null or :canteenName = ''
                   or lower(c.name) like lower(concat('%', :canteenName, '%')))
              and (:cuisineType is null or :cuisineType = ''
                   or lower(w.cuisineType) like lower(concat('%', :cuisineType, '%')))
            """)
    Page<CanteenWindow> search(
            @Param("keyword") String keyword,
            @Param("canteenName") String canteenName,
            @Param("cuisineType") String cuisineType,
            Pageable pageable
    );

    List<CanteenWindow> findByStatusOrderByAvgScoreDescReviewCountDesc(String status, Pageable pageable);

    List<CanteenWindow> findByStatusOrderByReviewCountDescAvgScoreDesc(String status, Pageable pageable);
}
