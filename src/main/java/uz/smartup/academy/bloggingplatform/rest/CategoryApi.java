package uz.smartup.academy.bloggingplatform.rest;

import org.springframework.web.bind.annotation.*;
import uz.smartup.academy.bloggingplatform.dto.CategoryDto;
import uz.smartup.academy.bloggingplatform.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryApi {
    private final CategoryService categoryService;

    public CategoryApi(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public CategoryDto findCategoryById(@PathVariable("id") int id) {

        CategoryDto categoryDto = categoryService.findCategoryById(id);
        if (categoryDto == null) throw new RuntimeException("Category not found by id" + id);
        return categoryDto;
    }

    @GetMapping
    public List<CategoryDto> allCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();

        if (categories.isEmpty()) throw new RuntimeException("Any categories doesn't exist yet");
        return categories;
    }

    @PostMapping
    public void createCategory(@RequestBody CategoryDto categoryDto) {
        categoryService.createCategory(categoryDto);
    }

    @PutMapping
    public void updateCategory(@RequestBody CategoryDto categoryDto) {
        categoryService.update(categoryDto);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable("id") int id) {
        categoryService.delete(id);
    }

}
