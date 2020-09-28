package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 회원은 존재하지 않습니다."));

        return member.getUsername();
    }

    /*
     * < 도메인 클래스 컨버터 사용 >
     * @PathVariable("id") Long id -----> @PathVariable("id") Member member
     *
     * 참고) 권장하지 않는 방법... 간단하면 쓸 수 있는데 복잡해지면, 못 씀
     *      도메인 클래스 컨버터로 받을거면, 단순 조회용으로 써야하고, 변경에 쓰이면 안된다.
     *      ( 트랜잭션 범위가 없는 상황에서 조회했기 때문에 영속성 컨텍스트에서 애매하다. )
     * */
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

//    @PostConstruct
    public void init() {
        for (int i = 0 ; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
