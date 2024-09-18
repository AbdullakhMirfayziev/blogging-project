package uz.smartup.academy.bloggingplatform.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uz.smartup.academy.bloggingplatform.dao.CategoryDao;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dto.CategoryDto;
import uz.smartup.academy.bloggingplatform.dto.CategoryDtoUtil;
import uz.smartup.academy.bloggingplatform.entity.Category;
import uz.smartup.academy.bloggingplatform.entity.Post;

import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService {
    private final PostDao postDao;
    private final CategoryDtoUtil categoryDtoUtil;
    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDtoUtil categoryDtoUtil, CategoryDao categoryDao, PostDao postDao) {
        this.categoryDtoUtil = categoryDtoUtil;
        this.categoryDao = categoryDao;
        this.postDao = postDao;
    }

    @Transactional
    @Override
    public void createCategory(CategoryDto categoryDto) {
        Category category = categoryDtoUtil.toEntity(categoryDto);
        categoryDao.save(category);

    }

    @Transactional
    @Override
    public void update(CategoryDto categoryDto) {
        Category category = categoryDtoUtil.toEntity(categoryDto);
        List<Post> posts = postDao.getPostsByCategory(category);
        category.setPosts(posts);
        categoryDao.update(category);
    }

    @Transactional
    @Override
    public void delete(int id) {
        categoryDao.delete(categoryDao.findCategoryById(id));
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryDtoUtil.toDTOs(categoryDao.getAllCategories());
    }

    @Override
    public CategoryDto findCategoryById(int id) {
        return categoryDtoUtil.toDto(categoryDao.findCategoryById(id));
    }

    @Override
    public List<CategoryDto> getCategoriesByPostId(int postId) {

        return categoryDtoUtil.toDTOs(categoryDao.getCategoriesByPostId(postId));
    }
}
