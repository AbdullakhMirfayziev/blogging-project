package uz.smartup.academy.bloggingplatform.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jdk.jfr.Registered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import uz.smartup.academy.bloggingplatform.dto.CommentDTO;
import uz.smartup.academy.bloggingplatform.entity.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Repository
public class PostDaoImpl implements PostDao{

    private final EntityManager entityManager;

    public PostDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public void save(Post post) {
        entityManager.persist(post);
    }

    @Override
    public void update(Post post) {
        entityManager.merge(post);
    }

    @Override
    public void delete(Post post) {
        entityManager.remove(post);
    }

    @Override
    public Post getById(int id) {
        return entityManager.find(Post.class, id);
    }

    @Override
    public List<Post> getAllPosts() {
        TypedQuery<Post> query = entityManager.createQuery("FROM Post", Post.class);

        return query.getResultList();
    }


    @Override
    public List<Post> searchPosts(String keyword) {
        TypedQuery<Post> query = entityManager.createQuery(
                "SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
                        "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))", Post.class
        );
        query.setParameter("keyword", keyword);

        return query.getResultList();
    }

    @Override
    public Page<Post> findPosts(Pageable pageable, Post.Status status, String category, String tag, String keyword) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Post p WHERE p.status = :status");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(p) FROM Post p WHERE p.status = :status");

        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" AND LOWER(p.title) LIKE LOWER(:likeKeyword) OR (CAST(LEVENSHTEIN(LOWER(p.title), LOWER(:keyword)) AS DOUBLE) / GREATEST(LENGTH(p.title), LENGTH(:keyword))) <= 0.2 ");
            countJpql.append(" AND LOWER(p.title) LIKE LOWER(:likeKeyword) OR (CAST(LEVENSHTEIN(LOWER(p.title), LOWER(:keyword)) AS DOUBLE) / GREATEST(LENGTH(p.title), LENGTH(:keyword))) <= 0.2");
        }

        Category category1 = null;
        Tag tag1 = null;

        if (category != null && !category.isEmpty()) {
            TypedQuery<Category> categoryTypedQuery = entityManager.createQuery("SELECT c FROM Category c WHERE c.title = :category", Category.class);
            categoryTypedQuery.setParameter("category", category);
            category1 = categoryTypedQuery.getSingleResult();
            jpql.append(" AND :category1 MEMBER OF p.categories");
            countJpql.append(" AND :category1 MEMBER OF p.categories");
        }

        if (tag != null && !tag.isEmpty()) {
            TypedQuery<Tag> tagTypedQuery = entityManager.createQuery("SELECT c FROM Tag c WHERE c.title = :tag", Tag.class);
            tagTypedQuery.setParameter("tag", tag);
            tag1 = tagTypedQuery.getSingleResult();
            jpql.append(" AND :tag1 MEMBER OF p.tags");
            countJpql.append(" AND :tag1 MEMBER OF p.tags");
        }

        if (pageable.getSort().isSorted()) {
            jpql.append(" ORDER BY ");
            StringJoiner joiner = new StringJoiner(", ");
            pageable.getSort().forEach(order ->
                    joiner.add("p." + order.getProperty() + " " + order.getDirection().name())
            );
            jpql.append(joiner.toString());
        }

        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class);
        query.setParameter("status", status);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        countQuery.setParameter("status", status);

        if (category != null && !category.isEmpty()) {
            query.setParameter("category1", category1);
            countQuery.setParameter("category1", category1);
        }
        if (tag != null && !tag.isEmpty()) {
            query.setParameter("tag1", tag1);
            countQuery.setParameter("tag1", tag1);
        }
        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", keyword.toLowerCase());
            query.setParameter("likeKeyword", "%" + keyword.toLowerCase() + "%");
            countQuery.setParameter("keyword", keyword.toLowerCase());
            countQuery.setParameter("likeKeyword", "%" + keyword.toLowerCase() + "%");
        }

        List<Post> posts = query.getResultList();
        long total = countQuery.getSingleResult();

        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public List<Post> findDraftsScheduledForPublish(LocalDateTime now, Post.Status status) {
        TypedQuery<Post> query = entityManager.createQuery("FROM Post p WHERE p.status = :status AND p.postSchedule.postScheduleDate <= :now", Post.class);

        query.setParameter("status", status);
        query.setParameter("now", now);

        return query.getResultList();
    }

    @Override
    public void saveSchedule(PostSchedule postSchedule) {
        entityManager.persist(postSchedule);
    }

    @Override
    public LocalDateTime getScheduleDateByPostId(int postId) {

        try {
            TypedQuery<PostSchedule> query = entityManager.createQuery("FROM PostSchedule p WHERE p.post.id = :postId", PostSchedule.class);

            query.setParameter("postId", postId);

            return query.getSingleResult().getPostScheduleDate();

        }catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public PostSchedule getScheduleByPostId(int postId) {

        try {
            TypedQuery<PostSchedule> query = entityManager.createQuery("FROM PostSchedule p WHERE p.post.id = :postId", PostSchedule.class);

            query.setParameter("postId", postId);

            return query.getSingleResult();

        }catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void saveSchedulePost(PostSchedule postSchedule) {
        entityManager.persist(postSchedule);
    }

    @Override
    public List<Post> getPostsByTag(Tag tag) {
        TypedQuery<Post> posts = entityManager.createQuery("FROM Post WHERE :tag MEMBER OF tags", Post.class);
        posts.setParameter("tag", tag);

        return posts.getResultList();
    }

    @Override
    public List<Post> getPostsByCategory(Category category) {
        TypedQuery<Post> posts = entityManager.createQuery("FROM Post WHERE :category MEMBER OF categories", Post.class);
        posts.setParameter("category", category);

        return posts.getResultList();
    }

    @Override
    public User getAuthorById(int id) {
        Post post = entityManager.find(Post.class, id);
        return post.getAuthor();
    }

    @Override
    public List<Post> getPostsByAuthor(int authorId) {
        TypedQuery<Post> query = entityManager.createQuery("FROM Post WHERE author.id = :authorId", Post.class);
        query.setParameter("authorId", authorId);

        return query.getResultList();
    }

    @Override
    public List<Comment> getPostComments(int id) {
        TypedQuery<Comment> query = entityManager.createQuery("FROM Comment WHERE post.id = :id", Comment.class);
        query.setParameter("id", id);

        return query.getResultList();
    }

    @Override
    public List<Post> findPostsByStatus(Post.Status status) {
        TypedQuery<Post> query = entityManager.createQuery("SELECT p FROM Post p WHERE p.status = :status", Post.class);

        query.setParameter("status", status);

        return query.getResultList();
    }


    @Override
    public List<Post> findPostByStatusAndAuthorId(Post.Status status, int authorId) {
        TypedQuery<Post> query = entityManager.createQuery(
                "SELECT p FROM Post p WHERE p.status = :status AND p.author.id = :authorId", Post.class);

        query.setParameter("status", status);
        query.setParameter("authorId", authorId);

        return query.getResultList();
    }

    @Override
    public Post.Status findPostStatusById(int postId) {
        Post post = getById(postId);
        return post.getStatus();
    }



}
