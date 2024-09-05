package uz.smartup.academy.bloggingplatform.exceptions;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoResourceFoundException ex, Model model) {
        model.addAttribute("title", "Page Not Found");
        model.addAttribute("message", "The page you're looking for doesn't exist or has been moved");
        model.addAttribute("errorStatus", 404);

        return "admin_zip/error";
    }


//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public String handle403(AccessDeniedException ex, Model model) {
//        System.err.println("An error occurred: " + ex.getMessage());

//        model.addAttribute("title", "Access Denied");
//        model.addAttribute("message", "You do not have permission to view this page or perform this action");
//        model.addAttribute("errorStatus", 403);

//        return "admin_zip/error";
//    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handle400(BadRequestException ex, Model model) {
        System.err.println("An error occurred: " + ex.getMessage());

        model.addAttribute("title", "Bad Request");
        model.addAttribute("message", "The request could not be processed due to a bad request. Please check the URL or try again");
        model.addAttribute("errorStatus", 400);

        return "admin_zip/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle500(RuntimeException ex, Model model) {
        System.err.println("An error occurred: " + ex.getMessage());

        model.addAttribute("title", "Something Went Wrong");
        model.addAttribute("message", "We encountered an unexpected error. Please try again later.");
        model.addAttribute("errorStatus", 500);

        return "admin_zip/error";
    }

}
