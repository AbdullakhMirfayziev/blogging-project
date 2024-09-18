package uz.smartup.academy.bloggingplatform.dto;

import org.springframework.stereotype.Component;
import uz.smartup.academy.bloggingplatform.entity.Category;

import java.util.List;

@Component
public class CategoryDtoUtil {
    public Category toEntity(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setTitle(categoryDto.getTitle());
        return category;
    }

    public CategoryDto toDto(Category category) {
        return new CategoryDto.Builder()
                .id(category.getId())
                .title(category.getTitle())
                .build();
    }

    public List<CategoryDto> toDTOs(List<Category> categories) {
        return categories.stream().map(this::toDto).toList();
    }
}

