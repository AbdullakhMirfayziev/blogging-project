package uz.smartup.academy.bloggingplatform.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uz.smartup.academy.bloggingplatform.dao.UserDaoImpl;
import uz.smartup.academy.bloggingplatform.dto.*;
import uz.smartup.academy.bloggingplatform.entity.Role;
import uz.smartup.academy.bloggingplatform.entity.RoleKey;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.service.*;

import java.util.List;

@Controller
public class AdminPanelController {

    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final TagService tagService;
    private final UserDtoUtil userDtoUtil;
    private final UserDaoImpl dao;


    public AdminPanelController(CategoryService categoryService, UserService userService, PostService postService, CommentService commentService, LikeService likeService, TagService tagService, UserDtoUtil userDtoUtil, UserDaoImpl dao) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.tagService = tagService;

        this.userDtoUtil = userDtoUtil;
        this.dao = dao;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<UserDTO> userDTOList = userService.getAllUsers();
        List<User> userDTOS = dao.getUsersWithEditorRole();
        List<User> userDTOSs = dao.findAllByEnabledIsNull();
        List<PostDto> postDtos = postService.getAllPosts();
        List<CategoryDto> categories = categoryService.getAllCategories();

        List<User> userss = dao.getUsersWithoutEditorRole();

        model.addAttribute("userEditor",userDTOS);
        model.addAttribute("userBan",userDTOSs);
        model.addAttribute("userViewer",userss);
        model.addAttribute("users",userDTOList.size());
        model.addAttribute("posts",postDtos.size());
        model.addAttribute("categories",categories);

        return "admin_zip/admin_panel";
    }
    private UserDetails getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails)
            return (UserDetails) principal;

        return null;
    }

    @GetMapping("/admin/category/{title}")
    public String getPostsByCategory(@PathVariable("title") String title, Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<CategoryDto> categories = categoryService.getAllCategories();
        List<PostDto> posts = postService.getPostsByCategory(title);
        model.addAttribute("postDTO", posts);
        model.addAttribute("categoryTitle", title);
        model.addAttribute("postSize", posts.size());
        model.addAttribute("categories",categories);
        return "admin_zip/category_table";
    }

    @GetMapping("/admin/user")
    public String getAllUsers(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<UserDTO> userDTOS = userService.getAllUsers();

        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("userDTO",userDTOS);
        model.addAttribute("userSize",userDTOS.size());

        model.addAttribute("categories",categories);

        return "admin_zip/user_table";
    }

    @GetMapping("/admin/user/search")
    public String getUserUserName(@RequestParam(name = "search") String username, Model model){
        model.addAttribute("userDTO", userService.userFindByUsername(username));

        List<CategoryDto> categories = categoryService.getAllCategories();
        model.addAttribute("categories",categories);
        return "admin_zip/user_table";
    }

    @GetMapping("/admin/user/search/viewer")
    public String getUserUserNameViewer(@RequestParam(name = "search") String username, Model model){

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<UserDTO> userDTOList = userService.getAllUsers();
        List<User> userDTOS = dao.getUsersWithEditorRole();
        List<User> userDTOSs = dao.findAllByEnabledIsNull();
        List<PostDto> postDtos = postService.getAllPosts();
        List<CategoryDto> categories = categoryService.getAllCategories();


        model.addAttribute("userEditor",userDTOS);
        model.addAttribute("userBan",userDTOSs);
        //model.addAttribute("userss",userss);
        model.addAttribute("users",userDTOList.size());
        model.addAttribute("posts",postDtos.size());
        model.addAttribute("categories",categories);


        List<User> userViewer = dao.userFindByUserName(username);

        model.addAttribute("userViewer", userViewer);

        return "admin_zip/admin_panel";
    }

    @GetMapping("/admin/user/search/editor")
    public String getUserUserNameEditor(@RequestParam(name = "search") String username, Model model){

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<UserDTO> userDTOList = userService.getAllUsers();
        //List<User> userDTOS = dao.getUsersWithEditorRole();
        List<User> userDTOSs = dao.findAllByEnabledIsNull();
        List<PostDto> postDtos = postService.getAllPosts();
        List<CategoryDto> categories = categoryService.getAllCategories();

        List<User> userss = dao.getUsersWithoutEditorRole();

        //model.addAttribute("userEditor",userDTOS);
        model.addAttribute("userBan",userDTOSs);
        model.addAttribute("usersViewer",userss);
        model.addAttribute("users",userDTOList.size());
        model.addAttribute("posts",postDtos.size());
        model.addAttribute("categories",categories);



        model.addAttribute("userEditor", dao.userFindByUserName(username));

        return "admin_zip/admin_panel";
    }

    @GetMapping("/admin/user/search/ban")
    public String getUserUserNameBan(@RequestParam(name = "search") String username, Model model){

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);

        List<UserDTO> userDTOList = userService.getAllUsers();
        List<User> userDTOS = dao.getUsersWithEditorRole();
        //List<User> userDTOSs = dao.findAllByEnabledIsNull();
        List<PostDto> postDtos = postService.getAllPosts();
        List<CategoryDto> categories = categoryService.getAllCategories();

        List<User> userss = dao.getUsersWithoutEditorRole();

        model.addAttribute("userEditor",userDTOS);
        //model.addAttribute("userDTOs",userDTOSs);
        model.addAttribute("userViewer",userss);
        model.addAttribute("users",userDTOList.size());
        model.addAttribute("posts",postDtos.size());
        model.addAttribute("categories",categories);



        model.addAttribute("userBan", dao.userFindByUserName(username));

        return "admin_zip/admin_panel";
    }



    @GetMapping("/admin/user/{userId}/edit")
    public String editProfile(Model model, @PathVariable("userId") String  username) {

        UserDTO user = userService.getUserByUsername(username);
        List<CategoryDto> categories = categoryService.getAllCategories();

        String base64EncodedPhoto = userService.encodePhotoToBase64(user.getPhoto());
        model.addAttribute("base64EncodedPhoto", base64EncodedPhoto);
        model.addAttribute("user", user);
        model.addAttribute("categories", categories);

        return "editUser";
    }

    @GetMapping("/admin/user/{username}/ban")
    public String banUser(@PathVariable("username") String username, Model model) {
        UserDTO userDTO = userService.getUserByUsername(username);

        //System.out.println(userDTO);

        userService.banUser(userDTO.getId());

        return "redirect:/admin";
    }

    @GetMapping("/admin/user/{username}/unban")
    public String unBanUser(@PathVariable("username") String username, Model model) {
        UserDTO userDTO = userService.getUserByUsername(username);

        //System.out.println(userDTO);

        userService.unBanUser(userDTO.getId());

        return "redirect:/admin";
    }

    @GetMapping("/admin/user/{username}/role/add")
    public String addRole(@PathVariable String username){
        UserDTO userDTO = userService.getUserByUsername(username);

        //System.out.println(username);

        RoleKey roleKey = new RoleKey(username, "ROLE_EDITOR");
        Role newRole = new Role();
        newRole.setId(roleKey);


        userService.saveRole(newRole);

        return "redirect:/admin";


    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable int id){
        userService.deleteUser(id);
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/post")
    public String getAllPost(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);


        List<PostDto> postDtos = postService.getAllPosts();

        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("postDTO",postDtos);
        model.addAttribute("userDTO",user);
        model.addAttribute("postSize",postDtos.size());

        model.addAttribute("categories",categories);

        return "admin_zip/post_table";
    }

    @RequestMapping("/admin/post/delete/{id}")
    public String deletePos(@PathVariable int id){
        postService.delete(id);
        return "redirect:/admin/post";
    }

    @GetMapping("/admin/comment")
    public String getAllComment(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);


        List<CommentDTO> commentDTOS = commentService.getAllComments();
        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("commentDTO",commentDTOS);
        model.addAttribute("commentSize",commentDTOS.size());

        model.addAttribute("categories",categories);

        return "admin_zip/comment_table";
    }

    @GetMapping("/admin/like")
    public String getAllLike(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);


        List<LikeDTO> likeDTOS = likeService.getAllLikes();
        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("likeDTO",likeDTOS);
        model.addAttribute("likeSize",likeDTOS.size());

        model.addAttribute("categories",categories);

        return "admin_zip/like_table";
    }

    @GetMapping("/admin/tag")
    public String getAllTag(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);


        List<TagDto> tagDtos = tagService.getAllTags();
        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("tagDTO",tagDtos);
        model.addAttribute("tagSize",tagDtos.size());

        model.addAttribute("categories",categories);

        return "admin_zip/tag_table";
    }

    @GetMapping("/admin/category")
    public String getAllCategories(Model model) {

        UserDTO user =  userService.getUserByUsername(getLoggedUser().getUsername());
        model.addAttribute("user",user);


        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("categoryDTO",categories);
        model.addAttribute("categorySize",categories.size());

        model.addAttribute("categories",categories);

        return "admin_zip/categories_table";
    }

    @PostMapping("/admin/category/add")
    public String addCategory(CategoryDto categoryDto){
        categoryService.createCategory(categoryDto);
        return "redirect:/admin/category";
    }

    int category_id;
    @GetMapping("/admin/category/edit/{id}")
    public String editCategory(@PathVariable int id, Model model){
        category_id = id;
        model.addAttribute("categoryDTO",categoryService.findCategoryById(id));
        return "admin_zip/categories_edit.html";
    }

    @PostMapping("/admin/category/edit")
    public String editCategory(@ModelAttribute CategoryDto categoryDTO){
        categoryDTO.setId(category_id);
        categoryService.update(categoryDTO);
        return "redirect:/admin/category";
    }

    @GetMapping("/admin/category/delete/{id}")
    public String deleteCategory(@PathVariable int id){
        categoryService.delete(id);
        return "redirect:/admin/category";
    }

    @RequestMapping("/access-denied")
    public String accessDenied(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        if (statusCode == null) {
            return "admin_zip/error";
        }

        return "admin_zip/error";
    }
}
