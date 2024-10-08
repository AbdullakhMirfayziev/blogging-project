package uz.smartup.academy.bloggingplatform.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.smartup.academy.bloggingplatform.dao.CategoryDao;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dao.TagDao;
import uz.smartup.academy.bloggingplatform.dao.UserDao;
import uz.smartup.academy.bloggingplatform.dto.*;
import uz.smartup.academy.bloggingplatform.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PostServiceImpl implements PostService {
    private final PostDao dao;
    private final UserDao userDao;
    private final PostDtoUtil dtoUtil;
    private final CommentDtoUtil commentDtoUtil;
    private final LikeService likeService;
    private final CategoryDao categoryDao;
    private final TagDao tagDao;
    private final CategoryService categoryService;
    private final UserDtoUtil userDtoUtil;
    private final TagService tagService;
    private final UserService userService;
    private final TagDtoUtil tagDtoUtil;
    private final MailSenderServiceImpl mailSenderServiceImpl;
    private final NotificationService notificationService;


    public PostServiceImpl(PostDao dao, PostDtoUtil dtoUtil, CommentDtoUtil commentDtoUtil, LikeService likeService, PostDtoUtil postDtoUtil, UserDao userDao, CategoryDao categoryDao, TagDao tagDao, CategoryService categoryService, UserDtoUtil userDtoUtil, TagService tagService, UserService userService, TagDtoUtil tagDtoUtil, MailSenderServiceImpl mailSenderServiceImpl, NotificationService notificationService) {
        this.dao = dao;
        this.dtoUtil = dtoUtil;
        this.commentDtoUtil = commentDtoUtil;
        this.likeService = likeService;
        this.userDao = userDao;
        this.categoryDao = categoryDao;
        this.tagDao = tagDao;
        this.categoryService = categoryService;
        this.userDtoUtil = userDtoUtil;
        this.tagService = tagService;
        this.userService = userService;
        this.tagDtoUtil = tagDtoUtil;
        this.mailSenderServiceImpl = mailSenderServiceImpl;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void createPost(Post post) {
        dao.save(post);
    }

    @Override
    @Transactional
    public void update(PostDto postDto) {
        Post post = dao.getById(postDto.getId());

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setPhoto(postDto.getPhoto());
//        post.setStatus(postDto.getStatus());

        post.setComments(dao.getPostComments(post.getId()));
        post.setAuthor(dao.getAuthorById(post.getId()));
        post.setCreatedAt(dao.getById(post.getId()).getCreatedAt());

        PostSchedule postSchedule = dao.getScheduleByPostId(post.getId());

        System.out.println(postDto.getTagsString());
        List<Category> categories1 = new ArrayList<>();
        List<String> tagTitle = separateString(postDto.getTagsString());


        postDto.getCategories().forEach(categoryId -> {
            categories1.add(categoryDao.findCategoryById(categoryId));
        });

        removeTags(post.getId());
        List<Tag> tags = new ArrayList<>();

        for(String tag : tagTitle) {
            tag = tag.toLowerCase();
            TagDto tagDto = tagService.getTagByName(tag);

            if(tagDto == null) {
                tagDto = new TagDto();
                tagDto.setTitle(tag);
                tags.add(tagDtoUtil.toEntity(tagDto));
                userService.addNewTagToPost(tagDto, post.getId());
            } else {
                tags.add(tagDtoUtil.toEntity(tagDto));
                userService.addExistTagToPost(tagDto.getId(), post.getId());
            }
        }

        post.getCategories().clear();
        if (postDto.getCategories() != null) {
            for (int categoryId : postDto.getCategories()) {
                Category category = categoryDao.findCategoryById(categoryId);
                post.addCategories(category);
            }
        }

        if(!tags.isEmpty())
            post.setTags(tags);


        if(postDto.getScheduleTime() != null) {
            if(postSchedule != null) {
                postSchedule.setPostScheduleDate(postDto.getScheduleTime());
            }
            else {
                postSchedule = new PostSchedule();
                postSchedule.setPostScheduleDate(postDto.getScheduleTime()) ;
                postSchedule.setPost(post);
                post.setPostSchedule(postSchedule);
            }
        }else if(postSchedule != null) {
            post.setPostSchedule(postSchedule);
        }

        dao.update(post);
    }

    @Override
    @Transactional
    public void removeTags(int postId) {
        Post post = dao.getById(postId);
        List<Tag> tags = post.getTags();

        if(!tags.isEmpty())
            for(int i = 0; i < tags.size(); i++) {
                post.removeTag(tags.get(i));
            }
    }

    @Override
    @Transactional
    public void delete(int postId) {
        List<LikeDTO> likes = likeService.getLikesByPostId(postId);

        for(LikeDTO like : likes)
            likeService.removeLike(like.getUserId(), postId);

        dao.delete(dao.getById(postId));
    }

    @Override
    public PostDto getById(int id) {
        return dtoUtil.toDto(dao.getById(id));
    }

    @Override
    public List<PostDto> getAllPosts() {
        return dtoUtil.toDTOs(dao.getAllPosts());
    }

    @Override
    public UserDTO getAuthorById(int id) {
        return userDtoUtil.toDTO(dao.getAuthorById(id));
    }

    @Override
    public List<PostDto> getPostsByAuthor(int authorId) {
        return dtoUtil.toDTOs(dao.getPostsByAuthor(authorId));
    }

    @Override
    public List<CommentDTO> getPostComments(int id) {
        List<Comment> comments = dao.getPostComments(id);

        return commentDtoUtil.toDTOs(comments);
    }

    @Override
    public List<PostDto> getDraftPost() {
        List<Post> posts = dao.findPostsByStatus(Post.Status.DRAFT);

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPublishedPost() {
        List<Post> posts = dao.findPostsByStatus(Post.Status.PUBLISHED);

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getDraftPostsByAuthorId(int authorId) {
        List<Post> posts = dao.findPostByStatusAndAuthorId(Post.Status.DRAFT, authorId);

//        System.out.println(dtoUtil.toDTOs(posts));
        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPublishedPostsByAuthorId(int authorId) {
        List<Post> posts = dao.findPostByStatusAndAuthorId(Post.Status.PUBLISHED, authorId);

//        System.out.println(dtoUtil.toDTOs(posts));

        return dtoUtil.toDTOs(posts);
    }

    @Override
    public Post getPostWithLikeCount(int postId) {
        Post post = dtoUtil.toEntity(getById(postId));
        long likeCount = likeService.countLikesByPostId(postId);
        post.setLikesCount(likeCount);
        return post;
    }

    @Transactional
    @Override
    public void addCommentToPost(int userId, int postId, CommentDTO commentDTO) {
        Post post = dao.getById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found with ID: " + postId);
        }

        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        Comment comment = commentDtoUtil.toEntity(commentDTO);
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setPost(post);

        post.addComments(comment);
        if(!Objects.equals(user.getUsername(), post.getAuthor().getUsername())) {
            notificationService.addNotification(post.getAuthor().getId(), comment.getAuthor().getId(), postId,user.getUsername() + " commented your post", "/posts/" + post.getId(), NotificationTypes.C);
        }
        dao.save(post);
    }


    @Override
    @Transactional
    public void switchPostDraftToPublished(int id) {
        Post post = dao.getById(id);

        post.setStatus(Post.Status.PUBLISHED);
        post.setCreatedAt(LocalDateTime.now());

        PostSchedule postSchedule = dao.getScheduleByPostId(id);

        UserDTO author = userService.getUserById(post.getAuthor().getId());
        List<UserDTO> followers = userService.getFollowers(author.getId());
        for(int i = 0; i < followers.size(); ++i) {
            notificationService.addNotification(followers.get(i).getId(), author.getId(), 0, author.getUsername() + " added new post", "/posts/author/" + author.getUsername(), NotificationTypes.N);
        }
        dao.update(post);
    }

    @Override
    @Transactional
    public void switchPublishedToDraft(int id) {
        Post post = dao.getById(id);

        PostSchedule postSchedule = dao.getScheduleByPostId(id);

        post.setStatus(Post.Status.DRAFT);

        dao.update(post);

        if(postSchedule != null) {
            dao.deleteScheduleData(postSchedule);
        }
    }

    @Override
    public List<PostDto> getPostsByCategory(String categoryTitle) {
        List<Post> posts = dao.getPostsByCategory(categoryDao.findCategoryByTitle(categoryTitle));
        return posts == null ? null : dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> getPostsByTag(String tagTitle) {
        List<Post> posts = dao.getPostsByTag(tagDao.findTagByTitle(tagTitle));
        return dtoUtil.toDTOs(posts);
    }

    @Override
    public List<PostDto> searchPosts(String keyword) {
        return dtoUtil.toDTOs(dao.searchPosts(keyword));
    }

    @Override
    @Transactional
    public void removeCategoryFromPost(int postId, int categoryId) {
        Post post = dao.getById(postId);

        post.removeCategory(categoryDao.findCategoryById(categoryId));

        dao.update(post);
    }

    @Override
    public List<String> separateString(String s) {
        List<String> list = new ArrayList<>();
        String  word = "";

        for(int i = 0; i < s.length(); i++) {
            if(s.charAt(i) != ' ' && s.charAt(i) != '\n') word += s.charAt(i);
            else {
                list.add(word);
                word = "";
            }
        }

        if(!word.isEmpty()) list.add(word);

        System.out.println("-".repeat(100));
        System.out.println(list);
        System.out.println("-".repeat(100));

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).charAt(0) == '#') {
                list.set(i, list.get(i).substring(1));
            }
        }

        return list;
    }

    @Override
    @Transactional
    public void addExistCategoriesToPost(int categoryId, int postId) {
        Post post = dao.getById(postId);
        Category category = categoryDao.findCategoryById(categoryId);

        post.addCategories(category);

        dao.update(post);
    }

    @Override
    public Page<PostDto> getPosts(int page, int size, String category, String tag, String keyword, int topPostId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = dao.findPosts(pageable, Post.Status.PUBLISHED, category, tag, keyword, topPostId);

        return postPage.map(dtoUtil::toDto);
    }

    @Override
    @Transactional
    public void savePostTags(PostDto postDto, List<Integer> tags) {
        Post post = dao.getById(postDto.getId());
        if (post == null) {
            throw new IllegalArgumentException("Post not found with ID: " + postDto.getId());
        }
        for (Integer tagId : tags) {
            Tag tag = (Tag) tagDao.getTagsByPostId(tagId);
            if (tag != null) {
                post.getTags().add(tag);
            }
        }
        dao.save(post);
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoPublishPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> postsToPublish = dao.findDraftsScheduledForPublish(now, Post.Status.DRAFT);

        for(int i = 0; i < postsToPublish.size(); i++) {
            switchPostDraftToPublished(postsToPublish.get(i).getId());
            dao.deleteScheduleData(postsToPublish.get(i).getPostSchedule());
        }
    }

    @Override
    public LocalDateTime scheduleDatePost(int postId) {
        return dao.getScheduleDateByPostId(postId);
    }

}
