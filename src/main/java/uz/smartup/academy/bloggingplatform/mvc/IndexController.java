package uz.smartup.academy.bloggingplatform.mvc;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.smartup.academy.bloggingplatform.dto.*;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.service.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Controller
public class IndexController {
    private final PostService postService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LikeService likeService;
    private final TagService tagService;
    private final CommentService commentService;


    public IndexController(PostService postService, CategoryService categoryService, UserService userService, LikeService likeService, TagService tagService, CommentService commentService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.likeService = likeService;
        this.tagService = tagService;
        this.commentService = commentService;
    }


    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "5") int size) {

        int postsSize = postService.getPublishedPost().size();

        System.out.println(page * size);

        if(postsSize < page * size) {
            page = postsSize / size + 1;
        }

        Page<PostDto> postPage = postService.getPosts(page - 1, size);
        List<PostDto> posts = postPage.getContent();



        if (posts != null && !posts.isEmpty()) {
            for (PostDto post : posts) {
                post.setLikesCount(likeService.countLikesByPostId(post.getId()));
                post.setHashedPhoto(post.getPhoto() == null
                        ? userService.encodePhotoToBase64(userService.getDefaultPostPhoto())
                        : userService.encodePhotoToBase64(post.getPhoto()));

                if (getLoggedUser() != null) {
                    post.setLiked(likeService.findByUserAndPost(
                            userService.getUserByUsername(getLoggedUser().getUsername()).getId(),
                            post.getId()) != null);
                } else {
                    post.setLiked(false);
                }
            }
        }

        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if (userDTO != null) {
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());
        }

        List<CategoryDto> categories = categoryService.getAllCategories();

        PostDto topPost = !posts.isEmpty() ? posts.getFirst() : null;
        if (topPost != null) {
            String safeContent = Jsoup.clean(topPost.getContent(), Safelist.basic());
            topPost.setContent(safeContent);
        }

//        System.out.println("_".repeat(100));
//        System.out.println(size);
//        System.out.println("_".repeat(100));

        model.addAttribute("topPost", topPost);
        model.addAttribute("posts", posts);
        model.addAttribute("photo", photo);
        model.addAttribute("categories", categories);
        model.addAttribute("loggedIn", userDTO);
        model.addAttribute("postPage", postPage);
        model.addAttribute("size", size);
        model.addAttribute("postsSize", postsSize);

        return "index";
    }


    @GetMapping("/posts/{postId}")
    public String getPostById(@PathVariable("postId") int postId, Model model) {
        PostDto post = postService.getById(postId);

        if((getLoggedUser() == null && post.getStatus() == Post.Status.DRAFT) || (post.getStatus() == Post.Status.DRAFT && !getLoggedUser().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EDITOR")) )) {
            return "redirect:/";
        }

        post.setLikesCount(likeService.countLikesByPostId(postId));
        List<CommentDTO> comments = postService.getPostComments(postId);
        List<CategoryDto> categories = categoryService.getAllCategories();
        post.setLikesCount(likeService.countLikesByPostId(postId));

        List<TagDto> tags = tagService.getTagsByPostId(postId);
        UserDTO author = postService.getAuthorById(postId);
        String authorPhoto = userService.encodePhotoToBase64(author.getPhoto());

        comments.forEach(commentDTO -> commentDTO.setUsername(userService.getUserById(commentDTO.getAuthorId()).getUsername()));
        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if(userDTO != null)
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());

        if(getLoggedUser() != null)
            post.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(getLoggedUser().getUsername()).getId(), post.getId()) != null);

        if(post.getPhoto() == null) post.setHashedPhoto(userService.encodePhotoToBase64(userService.getDefaultPostPhoto()));
        else post.setHashedPhoto(userService.encodePhotoToBase64(post.getPhoto()));

        for (CommentDTO commentDTO : comments)
            commentDTO.setHashedPhoto(userService.encodePhotoToBase64(userService.getUserById(commentDTO.getAuthorId()).getPhoto()));

        comments = comments.reversed();

        if(userDTO != null)
            model.addAttribute("loggedInId", userDTO.getId());
        model.addAttribute("photo", photo);
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("commentsSize", comments.size());
        model.addAttribute("tags", tags);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("categories", categories);
        model.addAttribute("newComment", new CommentDTO());
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("authorPhoto", authorPhoto);
        model.addAttribute("author", author);

        return "getPost";
    }

    @PostMapping("/posts/{postId}/submitComment/{username}")
    public String createComment(RedirectAttributes attributes, @PathVariable("postId") int postId, @PathVariable("username") String username, @ModelAttribute("newComment") CommentDTO comment) {
        postService.addCommentToPost(userService.getUserByUsername(username).getId(), postId, comment);


        attributes.addAttribute("id", postId);

        return "redirect:/posts/{id}";
    }

    @PostMapping("/posts/{postId}/likes/{username}")
    public String createLike(@PathVariable("postId") int postId, @PathVariable("username") String username) {
        likeService.addLike(userService.getUserByUsername(username).getId(), postId);

        return "redirect:/";
    }

    @PostMapping("/{postId}/likes/{username}")
    public String likePost(@PathVariable("postId") int postId, @PathVariable("username") String username, RedirectAttributes attributes) {
        likeService.addLike(userService.getUserByUsername(username).getId(), postId);
        PostDto postDto = postService.getById(postId);

        postDto.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(username).getId(), postId) != null);

        attributes.addAttribute("postId", postId);

        return "redirect:/posts/{postId}";
    }

    @PostMapping("/categories/{categoryTitle}/{postId}/likes/{username}")
    public String likeCategory(@PathVariable("categoryTitle") String categoryTitle, @PathVariable("postId") int postId, @PathVariable("username") String username, RedirectAttributes attributes) {
        likeService.addLike(userService.getUserByUsername(username).getId(), postId);

        attributes.addAttribute("categoryTitle", categoryTitle);

        return "redirect:/categories/{categoryTitle}";
    }

    @PostMapping("/{authorUsername}/posts/{postId}/likes/{username}")
    public String likeAuthor(@PathVariable("authorUsername") String authorUsername, @PathVariable("postId") int postId, @PathVariable("username") String username, RedirectAttributes attributes) {
        likeService.addLike(userService.getUserByUsername(username).getId(), postId);

        attributes.addAttribute("username", authorUsername);

        return "redirect:/posts/author/{username}";
    }

    @GetMapping("/categories/{categoryTitle}")
    public String categoryPost(@PathVariable("categoryTitle") String categoryTitle, Model model) {
        List<PostDto> posts = postService.getPostsByCategory(categoryTitle);

        if (posts != null) {
            posts = posts.stream()
                    .filter(postDto -> postDto.getStatus().equals(Post.Status.PUBLISHED))
                    .sorted((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()))
                    .toList();

            if (posts.size() > 20) {
                posts = posts.stream()
                        .limit(20)
                        .toList();
            }

            for (PostDto post : posts) {
                post.setLikesCount(likeService.countLikesByPostId(post.getId()));
            }

            for(PostDto postDto : posts) {
                if(postDto.getPhoto() == null) postDto.setHashedPhoto(userService.encodePhotoToBase64(userService.getDefaultPostPhoto()));
                else postDto.setHashedPhoto(userService.encodePhotoToBase64(postDto.getPhoto()));
            }

            if(getLoggedUser() != null)
                for(PostDto postDto : posts)
                    postDto.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(getLoggedUser().getUsername()).getId(), postDto.getId()) != null);
            else
                for (PostDto postDto : posts)
                    postDto.setLiked(false);
        }

        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if(userDTO != null)
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());

        List<CategoryDto> categories = categoryService.getAllCategories();

        PostDto topPost = (posts != null && !posts.isEmpty()) ? posts.getFirst() : null;

        if(topPost != null) {
            String safeContent = Jsoup.clean(topPost.getContent(), Safelist.basic());
            topPost.setContent(safeContent);
        }

        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("photo", photo);
        model.addAttribute("posts", posts);
        model.addAttribute("topPost", topPost);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryTitle", categoryTitle);

        return "categoryPosts";
    }

    @GetMapping("/profile/{username}")
    public String profile(@PathVariable("username") String username, Model model) {
        UserDTO user = userService.getUserByUsername(username);
        List<CategoryDto> categories = categoryService.getAllCategories();


        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if(userDTO != null)
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());

        String base64EncodedPhoto = userService.encodePhotoToBase64(user.getPhoto());
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("loggedInPhoto", photo);
        model.addAttribute("photo", base64EncodedPhoto);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/profile/{username}/uploadPhoto")
    public String uploadPhoto(RedirectAttributes attributes,@RequestParam("file") MultipartFile file, Model model, @PathVariable("username") String username) {
        UserDTO user = userService.getUserByUsername(username);

        try {
            byte[] bytes = file.getBytes();
            if(!file.isEmpty() && file != null) {
                user.setPhoto(bytes);
                userService.updateUser(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to upload the photo. Please try again.");
        }

        attributes.addAttribute("username", username);

        return "redirect:/profile/{username}";
    }


    @GetMapping("/profile/{userId}/edit")
    public String editProfile(Model model, @PathVariable("userId") String  username) {
        if(getLoggedUser() == null || !getLoggedUser().getUsername().equals(username)) {
            return "redirect:/";
        }

        UserDTO user = userService.getUserByUsername(username);
        List<CategoryDto> categories = categoryService.getAllCategories();

        String base64EncodedPhoto = userService.encodePhotoToBase64(user.getPhoto());
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("base64EncodedPhoto", base64EncodedPhoto);
        model.addAttribute("user", user);
        model.addAttribute("categories", categories);

        return "editUser";
    }

    @PostMapping("/profile/{userId}/update")
    public String updateUser(@PathVariable("userId") int userId,Model model, @ModelAttribute("user") UserDTO userDTO, RedirectAttributes attributes, @RequestParam(value = "file", required = false) MultipartFile photo) throws IOException {
        try {
//            System.out.println(userDTO.getId());
            UserDTO user = userService.getUserById(userId);
            if (photo == null || photo.isEmpty()) {
                userDTO.setPhoto(user.getPhoto());
            }else {
                byte[] photoBytes = photo.getBytes();
                userDTO.setPhoto(photoBytes);
            }
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword(user.getPassword());
            userDTO.setRegistered(user.getRegistered());
            userService.updateUser(userDTO);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to update the profile. Please try again.");
        }

        attributes.addAttribute("username", userDTO.getUsername());

        return "redirect:/profile/{username}";
    }




    private UserDetails getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails)
            return (UserDetails) principal;

        return null;
    }

    @PostMapping("/deletePhoto/{userId}")
    public String deletePhoto(@PathVariable("userId") int userId, RedirectAttributes attributes) {
        UserDTO userDTO = userService.getUserById(userId);

        userService.setDefaultPhotoToUser(userDTO);

        attributes.addAttribute("username", userDTO.getUsername());

        return "redirect:/profile/{username}";
    }

    @PostMapping("/posts/{postId}/delete/{commentId}")
    public String deleteComment(@PathVariable("commentId") int commentId, @PathVariable("postId") int postId, RedirectAttributes attributes) {
        UserDTO loggedIn = userService.getUserByUsername(getLoggedUser().getUsername());
        CommentDTO commentDTO = commentService.getComment(commentId);
        if(commentDTO.getAuthorId() == loggedIn.getId())
            commentService.deleteComment(commentId);

        attributes.addAttribute("postId", postId);

        return "redirect:/posts/{postId}";
    }

    @GetMapping("/posts/{postId}/edit/{commentId}")
    public String editComment(@PathVariable("postId") int postId, @PathVariable("commentId") int commentId, Model model, RedirectAttributes attributes) {
        UserDTO user = userService.getUserByUsername(getLoggedUser().getUsername());
        CommentDTO commentDTO = commentService.getComment(commentId);

        String base64EncodedPhoto = userService.encodePhotoToBase64(user.getPhoto());
        model.addAttribute("photo", base64EncodedPhoto);
        model.addAttribute("comment", commentDTO);
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("postId", postId);

        return "editComment";
    }

    @PostMapping("/{commentId}/updateComment/{postId}")
    public String updateComment(@PathVariable("commentId") int commentId ,@PathVariable("postId") int postId, @ModelAttribute("comment") CommentDTO comment, RedirectAttributes attributes) {
        comment.setId(commentId);
        commentService.updateComment(comment);

        attributes.addAttribute("postId", postId);

        return "redirect:/posts/{postId}";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam("keyword") String keyword, Model model) {
        List<PostDto> posts = postService.searchPosts(keyword);

        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if(userDTO != null){
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());
        }

        if (posts != null) {

            posts = posts.stream()
                    .filter(postDto -> postDto.getStatus().equals(Post.Status.PUBLISHED))
                    .sorted((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()))
                    .toList();

            if (posts.size() > 20) {
                posts = posts.stream()
                        .limit(20)
                        .toList();
            }

            for (PostDto post : posts) {
                post.setLikesCount(likeService.countLikesByPostId(post.getId()));
            }


            for(PostDto postDto : posts) {
                if(postDto.getPhoto() == null) postDto.setHashedPhoto(userService.encodePhotoToBase64(userService.getDefaultPostPhoto()));
                else postDto.setHashedPhoto(userService.encodePhotoToBase64(postDto.getPhoto()));
            }

            if(getLoggedUser() != null)
                for(PostDto postDto : posts)
                    postDto.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(getLoggedUser().getUsername()).getId(), postDto.getId()) != null);
            else
                for (PostDto postDto : posts)
                    postDto.setLiked(false);

        }

        model.addAttribute("photo", photo);
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("loggedIn", getLoggedUser());

        return "searchResults";
    }


    @GetMapping("/posts/tags/{tagTitle}")
    public String findPostsByTag(@PathVariable("tagTitle") String tagTitle, Model model) {
        List<PostDto> posts = postService.getPostsByTag(tagTitle);

//        posts.removeIf(postDto -> postDto.getStatus().equals(Post.Status.DRAFT));
        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());

        if(userDTO != null){
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());
        }

        if (posts != null) {

            posts = posts.stream()
                    .filter(postDto -> postDto.getStatus().equals(Post.Status.PUBLISHED))
                    .sorted((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()))
                    .toList();

            if (posts.size() > 20) {
                posts = posts.stream()
                        .limit(20)
                        .toList();
            }

            for (PostDto post : posts) {
                post.setLikesCount(likeService.countLikesByPostId(post.getId()));
            }


            for(PostDto postDto : posts) {
                if(postDto.getPhoto() == null) postDto.setHashedPhoto(userService.encodePhotoToBase64(userService.getDefaultPostPhoto()));
                else postDto.setHashedPhoto(userService.encodePhotoToBase64(postDto.getPhoto()));
            }

            if(getLoggedUser() != null)
                for(PostDto postDto : posts)
                    postDto.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(getLoggedUser().getUsername()).getId(), postDto.getId()) != null);
            else
                for (PostDto postDto : posts)
                    postDto.setLiked(false);

        }

        model.addAttribute("photo", photo);
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", tagTitle);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("loggedIn", getLoggedUser());

        return "searchResults";
    }


    @GetMapping("/posts/author/{username}")
    public String authorPost(@PathVariable("username") String username, Model model) {
        UserDTO author = userService.getUserByUsername(username);
        List<PostDto> posts = postService.getPostsByAuthor(author.getId());
        String firstName = author.getFirst_name();
        String lastName = author.getLast_name();

        String photo = userService.encodePhotoToBase64(author.getPhoto());
        if(getLoggedUser() != null) {
            UserDTO userDTO = userService.getUserByUsername(getLoggedUser().getUsername());
            String userPhoto = userService.encodePhotoToBase64(userDTO.getPhoto());
            model.addAttribute("userPhoto", userPhoto);
        }

        if (posts != null) {

            posts = posts.stream()
                    .filter(postDto -> postDto.getStatus().equals(Post.Status.PUBLISHED))
                    .sorted((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()))
                    .toList();

            if (posts.size() > 20) {
                posts = posts.stream()
                        .limit(20)
                        .toList();
            }

            for (PostDto post : posts) {
                post.setLikesCount(likeService.countLikesByPostId(post.getId()));
            }


            for(PostDto postDto : posts) {
                if(postDto.getPhoto() == null) postDto.setHashedPhoto(userService.encodePhotoToBase64(userService.getDefaultPostPhoto()));
                else postDto.setHashedPhoto(userService.encodePhotoToBase64(postDto.getPhoto()));
            }

            if(getLoggedUser() != null)
                for(PostDto postDto : posts)
                    postDto.setLiked(likeService.findByUserAndPost(userService.getUserByUsername(getLoggedUser().getUsername()).getId(), postDto.getId()) != null);
            else
                for (PostDto postDto : posts)
                    postDto.setLiked(false);

        }



        model.addAttribute("posts", posts);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("loggedIn", getLoggedUser());
        model.addAttribute("username", username);
        model.addAttribute("photo", photo);


        return "postsWithAuthor";
    }

    @PostMapping("posts/{postId}/likes/{username}/{keyword}")
    public String searchLike(@PathVariable("postId") int postId, @PathVariable("username") String username, @PathVariable("keyword") String keyword) {
        UserDTO userDTO = userService.getUserByUsername(username);

        likeService.addLike(userDTO.getId(), postId);

        return "redirect:/search?keyword=" + keyword;
    }

    @PostMapping("/posts/tags/posts/{postId}/likes/{username}/{keyword}")
    public String tagLike(@PathVariable("postId") int postId, @PathVariable("username") String username, @PathVariable("keyword") String keyword) {
        UserDTO userDto = userService.getUserByUsername(username);

        likeService.addLike(userDto.getId(), postId);

        return "redirect:/posts/tags/" + keyword;
    }
}