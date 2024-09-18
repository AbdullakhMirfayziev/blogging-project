package uz.smartup.academy.bloggingplatform.dto;

import org.springframework.stereotype.Component;
import uz.smartup.academy.bloggingplatform.entity.Tag;

import java.util.List;

@Component
public class TagDtoUtil {
    public Tag toEntity(TagDto tagDto) {
        Tag tag = new Tag();
        tag.setId(tagDto.getId());
        tag.setTitle(tagDto.getTitle());
        return tag;
    }

    public TagDto toDto(Tag tag) {
        return tag == null ? null : new TagDto.Builder()
                .id(tag.getId())
                .title(tag.getTitle())
                .build();
    }

    public List<TagDto> toDTOs(List<Tag> tags) {
        return tags.stream().map(this::toDto).toList();
    }
}
