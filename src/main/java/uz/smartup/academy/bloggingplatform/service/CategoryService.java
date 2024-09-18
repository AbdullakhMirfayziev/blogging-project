package uz.smartup.academy.bloggingplatform.service;

import uz.smartup.academy.bloggingplatform.dto.CategoryDto;
import uz.smartup.academy.bloggingplatform.entity.Category;

import java.util.List;


public interface CategoryService {
    void createCategory(CategoryDto category);

    void update(CategoryDto category);

    void delete(int id);

    List<CategoryDto> getAllCategories();

    CategoryDto findCategoryById(int id);

    List<CategoryDto> getCategoriesByPostId(int postId);
}
