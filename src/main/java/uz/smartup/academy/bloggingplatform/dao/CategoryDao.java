package uz.smartup.academy.bloggingplatform.dao;

import uz.smartup.academy.bloggingplatform.entity.Category;

import java.util.List;

public interface CategoryDao {
    void save(Category category);

    void update(Category category);

    void delete(Category category);

    List<Category> getAllCategories();

    Category findCategoryById(int id);

    Category findCategoryByTitle(String title);

    List<Category> getCategoriesByPostId(int postId);

}

