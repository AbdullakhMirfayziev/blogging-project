package uz.smartup.academy.bloggingplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDto {
    private int id;
    private String title;

    public TagDto() {
    }

    public TagDto(TagDto.Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
    }

    public static class Builder {
        private int id;
        private String title;

        public TagDto.Builder id(int id) {
            this.id = id;
            return this;
        }

        public TagDto.Builder title(String title) {
            this.title = title;
            return this;
        }

        public TagDto build() {
            return new TagDto(this);
        }

    }

    @Override
    public String toString() {
        return "TagDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
