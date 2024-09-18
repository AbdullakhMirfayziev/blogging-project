package uz.smartup.academy.bloggingplatform.dao;

import uz.smartup.academy.bloggingplatform.entity.Tag;

import java.util.List;

public interface TagDao {
    void save(Tag tag);

    void update(Tag tag);

    void delete(Tag tag);

    List<Tag> getAllTags();

    Tag findTagById(int id);

    Tag findTagByTitle(String title);

    List<Tag> getTagsByPostId(int postId);
}
