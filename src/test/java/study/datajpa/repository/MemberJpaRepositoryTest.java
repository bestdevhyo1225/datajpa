package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false) // 실무에선 옵션 끄도록
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember() {
        Member member = new Member("hyoseok");

        // entityManager.persist(member) 했기 때문에 Member는 영속성 컨텍스트에 존재함
        Member savedMember = memberJpaRepository.save(member);

        /*
         * SQL이 나가지 않은 이유? Member는 영속성 컨텍스트 1차캐시에 있기 때문에 DB에 직접 조회하지 않음
         * 즉, 1차 캐시에 있는 Member를 가져오기 때문에 SQL이 발생되지 않은 것!
         * */
        Member findMember = memberJpaRepository.find(savedMember.getId());

        /*
         * JPA 특성상 같은 트랜잭션 안에서는 영속성 컨텍스트의 동일성을 보장하기 때문에 member == savedMember == findMember 이다.
         * */
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberJpaRepository.findAll();

        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();

        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> members = memberJpaRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(members.get(0).getUsername()).isEqualTo("AAA");
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void paging() {
        // given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 10));
        memberJpaRepository.save(new Member("member3", 10));
        memberJpaRepository.save(new Member("member4", 10));
        memberJpaRepository.save(new Member("member5", 10));

        /*
        * page 1 -> offset = 0, limit = 10
        * page 2 -> offset = 10, limit = 10
        * */

        int age = 10;
        int offset = 0;
        int limit  = 3;

        // when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        // then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);

        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void blukUpdate() {
        // given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 19));
        memberJpaRepository.save(new Member("member3", 20));
        memberJpaRepository.save(new Member("member4", 29));
        memberJpaRepository.save(new Member("member5", 40));

        // when
        int resultCount = memberJpaRepository.bulkAgePlus(20);

        // then
        assertThat(resultCount).isEqualTo(3);
    }
}