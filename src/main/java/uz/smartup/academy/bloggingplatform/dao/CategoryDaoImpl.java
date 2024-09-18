package uz.smartup.academy.bloggingplatform.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import uz.smartup.academy.bloggingplatform.entity.Category;
import uz.smartup.academy.bloggingplatform.entity.Post;

import java.util.List;

@Repository
public class CategoryDaoImpl implements CategoryDao {
    private final EntityManager entityManager;

    public CategoryDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Category category) {
        entityManager.persist(category);
    }

    @Override
    public void update(Category category) {
        entityManager.merge(category);
    }

    @Override
    public void delete(Category category) {
        entityManager.remove(category);
    }

    @Override
    public List<Category> getAllCategories() {
        TypedQuery<Category> query = entityManager.createQuery("FROM Category", Category.class);
        return query.getResultList();
    }

    @Override
    public Category findCategoryById(int id) {
        return entityManager.find(Category.class, id);
    }

    @Override
    public Category findCategoryByTitle(String title) {
        TypedQuery<Category> query = entityManager.createQuery("FROM Category WHERE title = :title", Category.class);

        query.setParameter("title", title);

        return query.getSingleResult();
    }

    @Override
    public List<Category> getCategoriesByPostId(int postId) {
        Post post = entityManager.find(Post.class, postId);
        TypedQuery<Category> query = entityManager.createQuery("FROM Category WHERE :post MEMBER OF posts", Category.class);
        query.setParameter("post", post);

        return query.getResultList();
    }
}

