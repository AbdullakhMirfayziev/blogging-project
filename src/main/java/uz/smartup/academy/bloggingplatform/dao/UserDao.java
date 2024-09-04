package uz.smartup.academy.bloggingplatform.dao;

import uz.smartup.academy.bloggingplatform.entity.*;

import java.util.List;
import java.util.Set;


public interface UserDao {

    User save(User user);

    List<User> getALlUsers();

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User getUserById(int id);

//    UserFollows getUserFollowsById(int user_id);

    void update(User user);

    void delete(User user);
  
    List<Post> getUserAllPosts(int userId);

    List<Role> userFindByRoles(String userName);

    Set<Role> getUserRoles(int userId);

    void updateUserComment(int userId, int postId, Comment comment);

    User findByEmail(String email);

    void saveRole(Role role);

    List<User> findAllByEnabledIsNull();

    List<User> userFindByUserName(String username);

    List<Notification> getUsersNeedingEmailNotification();


}

/*
userDao:
    update
    delete
    getUserById
    register = save
    getUsers
    getUserByUsername



like:
    save
    delete
    getLikesByPostId

Category:
    save
    update
    delete
    getAllCategories
    getCategoryByTitle

tag:
    save
    update
    delete
    getAllTags
    getTagByTitle

 */