package site.metacoding.blogv3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Service
public class PostService {

    @Value("${file.path}") // application.yml 에 등록된 어떤 키값을 가져올 때 사용하는 어노테이션
    private String uploadFolder;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public List<Category> 게시글쓰기화면(User principal) {
        return categoryRepository.findByUserId(principal.getId());
    }

    // 하나의 서비스는 여러 가지 일을 한 번에 처리한다. (여러 가지 일이 하나의 트랜잭션이다.)
    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto, User principal) {
        // 서비스는 여러 가지 로직이 공존한다. 그래서 디버깅하기 힘들다.

        // 1. UUID로 파일쓰고 경로 리턴 받기
        String thumnail = null;
        if (!postWriteReqDto.getThumnailFile().isEmpty()) {
            thumnail = UtilFileUpload.write(uploadFolder, postWriteReqDto.getThumnailFile());
        }

        // postman으로 공격 당할 수 있으므로 디비에 데이터가 있는지 확인해야 한다.
        // Category category = new Category();
        // category.setId(postWriteReqDto.getCategoryId());

        // 2. 카테고리 있는지 확인
        Optional<Category> categoryOp = categoryRepository.findById(postWriteReqDto.getCategoryId());

        // 3. post DB 저장
        if (categoryOp.isPresent()) {
            Post post = postWriteReqDto.toEntity(thumnail, principal, categoryOp.get());
            postRepository.save(post);
        } else {
            throw new CustomException("해당 카테고리가 존재하지 않습니다.");
        }

        // INSERT INTO post(caterogyId, title, content, userId, thumnail) VALUES(?, ?,
        // ?, ?, ?);
        // 아래처럼 쓰면 postman으로 공격당할 수 있다. (없는 카테고리 id 넣거나)
        // postRepository.mSave(
        // postWriteReqDto.getCategoryId(),
        // principal.getId(),
        // postWriteReqDto.getTitle(),
        // postWriteReqDto.getContent(),
        // thumnail);
    }

    public PostRespDto 게시글목록보기(Integer userId, Pageable Pageable) { // Pageable은 domain으로 import
        Page<Post> postsEntity = postRepository.findByUserId(userId, Pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(userId);

        // 페이징 번호
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }

        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity,
                userId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers);
        return postRespDto;
    }

    public PostRespDto 게시글카테고리별보기(Integer userId, Integer categoryId, Pageable Pageable) {
        Page<Post> postsEntity = postRepository.findByUserIdAndCategoryId(userId, categoryId, Pageable);
        List<Category> categorysEntity = categoryRepository.findByUserId(userId);

        // 페이징 번호
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }

        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categorysEntity,
                userId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers);
        return postRespDto;
    }
}
