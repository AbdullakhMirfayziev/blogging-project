package uz.smartup.academy.bloggingplatform.service;

import org.springframework.data.domain.Page;
import uz.smartup.academy.bloggingplatform.dto.CommentDTO;
import uz.smartup.academy.bloggingplatform.dto.PostDto;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.util.List;

public interface PostService {
    void createPost(Post post);

    void update(PostDto postDto);

    void removeTags(int postId);

    void delete(int postId);

    PostDto getById(int id);

    List<PostDto> getAllPosts();

    UserDTO getAuthorById(int id);

    List<PostDto> getPostsByAuthor(int authorId);

    List<CommentDTO> getPostComments(int id);

    List<PostDto> getDraftPost();

    List<PostDto> getPublishedPost();

    List<PostDto> getDraftPostsByAuthorId(int authorId);

    List<PostDto> getPublishedPostsByAuthorId(int authorId);

    Post getPostWithLikeCount(int postId);
  
    void addCommentToPost(int userId, int posIid, CommentDTO commentDTO);

  void switchPostDraftToPublished(int id);

    void switchPublishedToDraft(int id);

    List<PostDto> getPostsByCategory(String categoryTitle);

    List<PostDto> getPostsByTag(String tagTitle);

    List<PostDto> searchPosts(String keyword);

    void removeCategoryFromPost(int postId, int categoryId);

    List<String> separate_string(String s);

    void addExistCategoriesToPost(int categoryId, int postId);

    Page<PostDto> getPosts(int page, int size);

}
