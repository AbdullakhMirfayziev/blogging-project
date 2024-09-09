package uz.smartup.academy.bloggingplatform.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.Tag;

import java.util.List;

@Repository
public class TagDaoImpl implements TagDao {
    private final EntityManager entityManager;

    public TagDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Tag tag) {
        entityManager.persist(tag);
    }

    @Override
    public void update(Tag tag) {
        entityManager.merge(tag);
    }

    @Override
    public void delete(Tag tag) {
        entityManager.remove(tag);
    }

    @Override
    public List<Tag> getAllTags() {
        TypedQuery<Tag> query = entityManager.createQuery("FROM Tag", Tag.class);
        return query.getResultList();
    }

    @Override
    public Tag findTagById(int id) {
        return entityManager.find(Tag.class, id);
    }

    @Override
    public Tag findTagByTitle(String title) {
        try {
            TypedQuery<Tag> query = entityManager.createQuery("FROM Tag WHERE title = :title", Tag.class);

            query.setParameter("title", title);

            List<Tag> results = query.getResultList();

            return results.getFirst();
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<Tag> getTagsByPostId(int postId) {
        Post post = entityManager.find(Post.class, postId);
        TypedQuery<Tag> query = entityManager.createQuery("FROM Tag WHERE :post MEMBER OF posts", Tag.class);

        query.setParameter("post", post);

        return query.getResultList();
    }
}
