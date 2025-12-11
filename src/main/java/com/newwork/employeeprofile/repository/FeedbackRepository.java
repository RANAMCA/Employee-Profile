package com.newwork.employeeprofile.repository;

import com.newwork.employeeprofile.domain.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

    List<Feedback> findByEmployeeId(Long employeeId);

    List<Feedback> findByAuthorId(Long authorId);

    List<Feedback> findByEmployeeIdAndVisible(Long employeeId, Boolean visible);

    @Query("{ 'employeeId': ?0, 'visible': true }")
    List<Feedback> findVisibleFeedbackByEmployeeId(Long employeeId);

    @Query("{ 'employeeId': ?0, 'authorId': ?1 }")
    List<Feedback> findByEmployeeIdAndAuthorId(Long employeeId, Long authorId);

    @Query("{ 'employeeId': ?0, 'rating': { $gte: ?1 } }")
    List<Feedback> findByEmployeeIdWithMinRating(Long employeeId, Integer minRating);

    @Query("{ 'isPolished': true }")
    List<Feedback> findAllPolishedFeedback();

    long countByEmployeeId(Long employeeId);

    long countByAuthorId(Long authorId);
}
