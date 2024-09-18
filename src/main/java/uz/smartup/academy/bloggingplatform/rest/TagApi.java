package uz.smartup.academy.bloggingplatform.rest;

import org.springframework.web.bind.annotation.*;
import uz.smartup.academy.bloggingplatform.dto.CategoryDto;
import uz.smartup.academy.bloggingplatform.dto.TagDto;
import uz.smartup.academy.bloggingplatform.entity.Tag;
import uz.smartup.academy.bloggingplatform.service.CategoryService;
import uz.smartup.academy.bloggingplatform.service.TagService;

import java.util.List;

@RestController
@RequestMapping("api/tags")
public class TagApi {
    private final TagService tagService;

    public TagApi(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/{id}")
    public TagDto findTagById(@PathVariable("id") int id){

        TagDto tagDto=tagService.findTagById(id);
        if(tagDto==null) throw new RuntimeException("Tag not found by id"+id);
        return tagDto;
    }

    @GetMapping
    public List<TagDto> allTags(){

        List<TagDto> tags=tagService.getAllTags();
        if(tags.isEmpty()) throw new RuntimeException("Any tags doesn't exist yet");
        return tags;
    }
    @PostMapping
    public void createTag(@RequestBody TagDto tagDto){
        tagService.createTag(tagDto);
    }
    @PutMapping
    public  void updateTag(@RequestBody TagDto tagDto){
        tagService.update(tagDto);
    }
    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable("id") int id){
        tagService.delete(id);
    }

}
