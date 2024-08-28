package uz.smartup.academy.bloggingplatform.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import uz.smartup.academy.bloggingplatform.entity.Comment;

import java.util.List;

@Repository
public class CommentDaoImpl implements CommentDao{

    private final EntityManager entityManager;

    public CommentDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Comment comment) {
        entityManager.persist(comment);
    }

    @Override
    public void update(Comment comment) {
        entityManager.merge(comment);
    }

    @Override
    public void delete(Comment comment) {
        entityManager.remove(comment);
    }

    @Override
    public Comment getById(int id) {
        return null;
    }

    @Override
    public Comment getComment(int id) {
        return entityManager.find(Comment.class, id);
    }

    @Override
    public List<Comment> getComments() {
        TypedQuery<Comment> comments = entityManager.createQuery("FROM Comment", Comment.class);
        return comments.getResultList();
    }

    @Override
    public List<Comment> getCommentsByPostId(int id) {
        TypedQuery<Comment> query = entityManager.createQuery("SELECT c FROM Comment c WHERE c.post.id = :id", Comment.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @Override
    public List<Comment> findNewComments() {
        TypedQuery<Comment> query = entityManager.createQuery("FROM Comment c WHERE c.newNotification=true", Comment.class);
        return query.getResultList();
    }
}
