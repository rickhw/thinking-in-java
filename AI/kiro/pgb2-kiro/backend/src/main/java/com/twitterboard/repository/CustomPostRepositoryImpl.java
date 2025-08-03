package com.twitterboard.repository;

import com.twitterboard.entity.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomPostRepositoryImpl implements CustomPostRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<Post> findPostsWithFilters(Long authorId, String content,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         boolean includeDeleted, Pageable pageable) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Post> query = cb.createQuery(Post.class);
        Root<Post> post = query.from(Post.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Author filter
        if (authorId != null) {
            predicates.add(cb.equal(post.get("author").get("id"), authorId));
        }
        
        // Content filter
        if (content != null && !content.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(post.get("content")), 
                                 "%" + content.toLowerCase() + "%"));
        }
        
        // Date range filter
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(post.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(post.get("createdAt"), endDate));
        }
        
        // Deleted filter
        if (!includeDeleted) {
            predicates.add(cb.equal(post.get("deleted"), false));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(post.get("createdAt")));
        
        TypedQuery<Post> typedQuery = entityManager.createQuery(query);
        
        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Post> posts = typedQuery.getResultList();
        
        // Count total results
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Post> countRoot = countQuery.from(Post.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(posts, pageable, total);
    }
    
    @Override
    public PostStatistics getPostStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);
        
        // Total posts
        Long totalPosts = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p", Long.class)
            .getSingleResult();
        
        // Active posts
        Long activePosts = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.deleted = false", Long.class)
            .getSingleResult();
        
        // Deleted posts
        Long deletedPosts = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.deleted = true", Long.class)
            .getSingleResult();
        
        // Posts today
        Long postsToday = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfDay AND p.deleted = false", Long.class)
            .setParameter("startOfDay", startOfDay)
            .getSingleResult();
        
        // Posts this week
        Long postsThisWeek = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfWeek AND p.deleted = false", Long.class)
            .setParameter("startOfWeek", startOfWeek)
            .getSingleResult();
        
        // Posts this month
        Long postsThisMonth = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfMonth AND p.deleted = false", Long.class)
            .setParameter("startOfMonth", startOfMonth)
            .getSingleResult();
        
        return new PostStatistics(totalPosts, activePosts, deletedPosts,
                                postsToday, postsThisWeek, postsThisMonth);
    }
    
    @Override
    public Page<Post> findTrendingPosts(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // For now, just return recent posts ordered by creation date
        // In a real application, this could include engagement metrics
        TypedQuery<Post> query = entityManager.createQuery(
            "SELECT p FROM Post p WHERE p.createdAt >= :since AND p.deleted = false " +
            "ORDER BY p.createdAt DESC", Post.class)
            .setParameter("since", since)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());
        
        List<Post> posts = query.getResultList();
        
        // Count total
        Long total = entityManager.createQuery(
            "SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :since AND p.deleted = false", Long.class)
            .setParameter("since", since)
            .getSingleResult();
        
        return new PageImpl<>(posts, pageable, total);
    }
    
    @Override
    public int bulkUpdatePostsStatus(List<Long> postIds, boolean deleted) {
        if (postIds == null || postIds.isEmpty()) {
            return 0;
        }
        
        return entityManager.createQuery(
            "UPDATE Post p SET p.deleted = :deleted WHERE p.id IN :postIds")
            .setParameter("deleted", deleted)
            .setParameter("postIds", postIds)
            .executeUpdate();
    }
}