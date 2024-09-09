package uz.smartup.academy.bloggingplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
    private int id;
    private String title;

    public CategoryDto() {
    }

    public CategoryDto(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
    }

    public static class Builder {
        private int id;
        private String title;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public CategoryDto build() {
            return new CategoryDto(this);
        }

    }

}

