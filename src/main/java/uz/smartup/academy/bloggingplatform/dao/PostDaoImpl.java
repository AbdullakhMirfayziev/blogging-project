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
import java.util.StringJoiner;

@Repository
public class PostDaoImpl implements PostDao{

    private final EntityManager entityManager;

    private final SessionFactory sessionFactory;

    public PostDaoImpl(EntityManager entityManager, SessionFactory sessionFactory) {
        this.entityManager = entityManager;
        this.sessionFactory = sessionFactory;
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
    public Page<Post> findPosts(Pageable pageable, Post.Status status) {
        // Create the base query
        String jpql = "SELECT p FROM Post p WHERE p.status = :status";
        String countJpql = "SELECT COUNT(p) FROM Post p WHERE p.status = :status";

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            jpql += " ORDER BY ";
            StringJoiner joiner = new StringJoiner(", ");
            pageable.getSort().forEach(order ->
                    joiner.add("p." + order.getProperty() + " " + order.getDirection().name())
            );
            jpql += joiner.toString();
        }

        // Create and execute the main query
        TypedQuery<Post> query = entityManager.createQuery(jpql, Post.class);
        query.setParameter("status", status);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Post> posts = query.getResultList();

        // Execute count query
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("status", status);
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
