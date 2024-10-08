package uz.smartup.academy.bloggingplatform.service;

import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dto.*;

import uz.smartup.academy.bloggingplatform.entity.Role;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(int id);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);


    void updateUser(UserDTO userDTO);

    void deleteUser(int id);

    void registerUser(UserDTO userDTO, List<Role> roles);

    void registerUserWithConfirmation(UserDTO userDTO, List<Role> roles);

    List<PostDao> getAllPostsOfUser(int id);

    void addDraftPostByUserId(int userId, PostDto postDto);

    void addPublishedPostByUserId(int userId, PostDto postDto);

    void addExistCategoriesToPost(int categoryId, int postId);

    void addNewCategoryToPost(CategoryDto categoryDto, int postId);

    void addExistTagToPost(int tagId, int postId);

    void addNewTagToPost(TagDto tagDto, int postId);

    String encodePhotoToBase64(byte[] photo);

//    List<PostDto> userPublishedPosts(int userId);
//
//    List<PostDto> userDraftPosts(int userId);

    void updateUserComment(int userId, int postId, CommentDTO comment);

    void setDefaultPhotoToUser(UserDTO user);

    void changePassword(String username, String newPassword);

    List<Role> userFindByRoles(String username);

    byte[] getDefaultPostPhoto();

    void saveRole(Role role);

    void banUser(int userId);

    void unBanUser(int userId);

    void unbanUsers();

    boolean userExists(String email, String userEmail);

    List<UserDTO> userFindByUsername(String username);

    void removeRolesFromUser(int userId);

    void followUser(int followerId, int followedId);

    void unfollowUser(int followerId, int followedId);

    List<UserDTO> getFollowing(int userId);

    List<UserDTO> getFollowers(int userId);
}
