package uz.smartup.academy.bloggingplatform.service;

import uz.smartup.academy.bloggingplatform.dto.TagDto;

import java.util.List;

public interface TagService {
    void createTag(TagDto tagDto);

    void update(TagDto tagDto);

    void delete(int id);

    List<TagDto> getAllTags();

    TagDto findTagById(int id);

    List<TagDto> getTagsByPostId(int postId);

    TagDto getTagByName(String name);
}
