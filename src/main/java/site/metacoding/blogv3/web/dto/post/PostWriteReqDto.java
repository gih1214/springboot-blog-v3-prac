package site.metacoding.blogv3.web.dto.post;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.user.User;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostWriteReqDto {

    @NotBlank
    private Integer categoryId;

    @Size(min = 1, max = 60)
    @NotBlank
    private String title;

    private MultipartFile thumnailFile;

    @NotNull // 공백은 가능하지만 키값은 전송해야 됨
    private String content; // nullable

    public Post toEntity(String thumnail, User principal, Category category) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setThumnail(thumnail);
        post.setUser(principal);
        post.setCategory(category);
        return post;
    }
}
