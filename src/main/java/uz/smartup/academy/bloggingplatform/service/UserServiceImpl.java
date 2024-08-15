package uz.smartup.academy.bloggingplatform.service;



import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uz.smartup.academy.bloggingplatform.dao.CategoryDao;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dao.TagDao;
import uz.smartup.academy.bloggingplatform.dao.UserDao;
import uz.smartup.academy.bloggingplatform.dto.*;

import java.time.LocalDate;

import java.util.*;

import uz.smartup.academy.bloggingplatform.entity.*;
import uz.smartup.academy.bloggingplatform.exceptions.UserAlreadyExistsException;


import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Value("classpath:static/css/photos/userPhoto.jpg")
    private Resource defaultPhotoResource;

    @Value("classpath:static/css/photos/GSW_news.jpg")
    private Resource defaultPhotoResourcePost;



    private byte[] defaultPhoto;
    private byte[] defaultPhotoPost;

    private final UserDao userDao;
    private final UserDtoUtil dtoUtil;
    private final PostDao postDao;
    private final PostDtoUtil postDtoUtil;
    private final CategoryDtoUtil categoryDtoUtil;
    private final CategoryDao categoryDao;
    private final CommentDtoUtil commentDtoUtil;
    private final TagDao tagDao;
    private final TagDtoUtil tagDtoUtil;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final EmailVerificationService emailVerificationService;

//    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    public UserServiceImpl(UserDao userDao, UserDtoUtil dtoUtil, PostDao postDao, PostDtoUtil postDtoUtil, CategoryDtoUtil categoryDtoUtil, CategoryDao categoryDao, CommentDtoUtil commentDtoUtil, TagDao tagDao, TagDtoUtil tagDtoUtil, PasswordEncoder passwordEncoder, MailSenderService mailSenderService, EmailVerificationService emailVerificationService) {
        this.userDao = userDao;
        this.dtoUtil = dtoUtil;
        this.postDao = postDao;
        this.postDtoUtil = postDtoUtil;
        this.categoryDtoUtil = categoryDtoUtil;
        this.categoryDao = categoryDao;
        this.commentDtoUtil = commentDtoUtil;
        this.tagDao = tagDao;
        this.tagDtoUtil = tagDtoUtil;
        this.passwordEncoder = passwordEncoder;
//        this.mailSenderService = mailSenderService;
        this.mailSenderService = mailSenderService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostConstruct
    public void init() throws IOException {
        defaultPhoto = StreamUtils.copyToByteArray(defaultPhotoResource.getInputStream());
        defaultPhotoPost = StreamUtils.copyToByteArray(defaultPhotoResourcePost.getInputStream());
    }

    @Override
    public String encodePhotoToBase64(byte[] photo) {
        if (photo == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(photo);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userDao.getALlUsers();
        return dtoUtil.toEntities(users);
    }

    @Override
    public UserDTO getUserById(int id) {
        User user = userDao.getUserById(id);
        return dtoUtil.toDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userDao.getUserByUsername(username);
        return dtoUtil.toDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userDao.getUserByEmail(email);
        return dtoUtil.toDTO(user);
    }

    @Override
    @Transactional
    public void updateUser(UserDTO userDTO) {
        User user = userDao.getUserById(userDTO.getId());

        System.out.println("-".repeat(100));
        System.out.println(userDTO.getEnabled());
        System.out.println("-".repeat(100));

        if(userDTO.getEnabled() != null && userDTO.getEnabled().equals("0"))
            user.setEnabled("0");

        userDao.update(dtoUtil.userMergeEntity(user, userDTO));
    }

    @Transactional
    @Override
    public void deleteUser(int id) {
        User user = userDao.getUserById(id);
        userDao.delete(user);
    }



    @Override
    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userDao.getUserByUsername(username);

        if(user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userDao.update(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public List<Role> userFindByRoles(String username) {
        return userDao.userFindByRoles(username);
    }

    @Override
    public byte[] getDefaultPostPhoto() {
        return defaultPhotoPost;
    }

    public User save(User user) {
        userDao.save(user);
        return userDao.getUserById(user.getId());
    }


    @Transactional
    @Override
    public void registerUser(UserDTO userDTO, List<Role> roles) {
        User user = dtoUtil.toEntity(userDTO);

        if (user.getPhoto() == null || user.getPhoto().length == 0) {
            user.setPhoto(defaultPhoto);
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        user.setRoles(roles);
        user.setEnabled("1");

        userDao.save(user);
    }

    @Override
    @Transactional
    public void registerUserWithConfirmation(UserDTO userDTO, List<Role> roles) {
        User user = dtoUtil.toEntity(userDTO);
        if (user.getPhoto() == null || user.getPhoto().length == 0) {
            user.setPhoto(defaultPhoto);
        }
//        if (userExists(user.getUsername(), user.getEmail())) {
//            throw new UserAlreadyExistsException("A user with this username or email already exists.");
//        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setRoles(roles);
        user.setEnabled("0");

        User saved = userDao.save(user);

        try{
            String token = UUID.randomUUID().toString();
            mailSenderService.save(saved, token);
            emailVerificationService.sendHtmlMail(saved);
        } catch (Exception e){
            e.printStackTrace();
//                logger.error("Error during user registration: {}", e.getMessage(), e);
        }
    }


    @Override
    public List<PostDao> getAllPostsOfUser(int id) {
        return List.of();
    }


    @Transactional
    @Override
    public void addDraftPostByUserId(int userId, PostDto postDto) {
        User user = userDao.getUserById(userId);

        Post post = postDtoUtil.toEntity(postDto);

        if (postDto.getPhoto() == null || postDto.getPhoto().length == 0) {
            post.setPhoto(defaultPhotoPost);
        }

        post.setStatus(Post.Status.DRAFT);
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());
        postDao.save(post);
        userDao.update(user);
    }

    @Override
    @Transactional
    public void addPublishedPostByUserId(int userId, PostDto postDto) {
        User user = userDao.getUserById(userId);

        Post post = postDtoUtil.toEntity(postDto);

        if (postDto.getPhoto() == null || postDto.getPhoto().length == 0) {
            post.setPhoto(defaultPhotoPost);
        }

        post.setStatus(Post.Status.PUBLISHED);
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());
        postDao.save(post);
        userDao.update(user);
    }

    @Override
    @Transactional
    public void addExistCategoriesToPost(int categoryId, int postId) {
        Post post = postDao.getById(postId);
        Category category = categoryDao.findCategoryById(categoryId);

        System.out.println(categoryId);

        post.addCategories(category);

        postDao.update(post);
    }

    @Override
    @Transactional
    public void addNewCategoryToPost(CategoryDto categoryDto, int postId) {
        Post post = postDao.getById(postId);

        post.addCategories(categoryDtoUtil.toEntity(categoryDto));

        postDao.update(post);
    }

    @Override
    @Transactional
    public void addExistTagToPost(int tagId, int postId) {
        Post post = postDao.getById(postId);
        Tag tag = tagDao.findTagById(tagId);

        post.addTag(tag);

        postDao.update(post);
    }

    @Override
    @Transactional
    public void addNewTagToPost(TagDto tagDto, int postId) {
        Post post = postDao.getById(postId);

        post.addTag(tagDtoUtil.toEntity(tagDto));

        postDao.update(post);
    }

//    @Override
//    public List<PostDto> userPublishedPosts(int userId) {
//        return postService.getPublishedPostsByAuthorId(userId);
//    }
//
//    @Override
//    public List<PostDto> userDraftPosts(int userId) {
//        return postService.getDraftPostsByAuthorId(userId);
//    }

    @Override
    @Transactional
    public void updateUserComment(int userId, int postId, CommentDTO comment) {
        Comment comment1 = commentDtoUtil.toEntity(comment);

        userDao.updateUserComment(userId, postId, comment1);
    }

    @Override
    @Transactional
    public void setDefaultPhotoToUser(UserDTO user) {
        User user1 = userDao.getUserById(user.getId());
        user.setPhoto(defaultPhoto);
        userDao.update(dtoUtil.userMergeEntity(user1, user));
    }

    @Transactional
    @Override
    public void saveRole(Role role) {
        userDao.saveRole(role);
    }

    @Override
    @Transactional
    public void banUser(int userId) {
        User user = userDao.getUserById(userId);
        if (user != null) {
            user.setEnabled(null);
            user.setRegistered(LocalDate.now());  // Ban qilingan vaqtni saqlaymiz
            userDao.save(user);
        }
    }

    @Override
    @Transactional
    public void unBanUser(int userId) {
        User user = userDao.getUserById(userId);
        if (user != null) {
            user.setEnabled("1");
            user.setRegistered(LocalDate.now());  // unBan qilingan vaqtni saqlaymiz
            userDao.save(user);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Har 1 soatda bir marta ishga tushadi
    public void unbanUsers() {

//        System.out.println("ishladi0000000000000000000000000000000000000");

        List<User> bannedUsers = userDao.findAllByEnabledIsNull();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < bannedUsers.size(); i ++) {
            User user = bannedUsers.get(i);
            if (user.getRegistered() != null && user.getRegistered().plusDays(1).isBefore(now)) {
                user.setEnabled("1");
                user.setRegistered(LocalDate.now());  // Tiklangandan vaqtini yangilaymiz
                userDao.update(user);
            }
        }
    }

    @Override
    public boolean userExists(String username, String userEmail) {
        return userDao.getUserByEmail(userEmail) != null || userDao.getUserByUsername(username) != null;
    }

    @Override
    public List<UserDTO> UserByUsername(String username) {
        List<User> users= userDao.userFindByFirstName(username);
        //System.out.println(users);

        return dtoUtil.toDTOs(users);
    }

    @Override
    @Transactional
    public void removeRolesFromUser(int userId) {
        User user = userDao.getUserById(userId);

        List<Role> roles = user.getRoles();

        for(int i = 0; i < roles.size(); ++ i)
            user.removeRole(roles.get(i));

        userDao.update(user);

    }


}
