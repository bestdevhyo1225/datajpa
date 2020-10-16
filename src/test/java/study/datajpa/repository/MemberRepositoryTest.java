package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");

        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberRepository.findAll();

        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();

        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(members.get(0).getUsername()).isEqualTo("AAA");
        assertThat(members.get(0).getAge()).isEqualTo(20);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void testQuery() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findMembers("AAA", 10);

        assertThat(members.get(0).getUsername()).isEqualTo("AAA");
        assertThat(members.get(0).getAge()).isEqualTo(10);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void findUsernames() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernames = memberRepository.findUsernames();

        assertThat(usernames.get(0)).isEqualTo("AAA");
        assertThat(usernames.get(1)).isEqualTo("BBB");
    }

    @Test
    public void findMemberDtos() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member member1 = new Member("member1", 10, teamA);
        memberRepository.save(member1);

        List<MemberDto> memberDtos = memberRepository.findMemberDtos();

        for (MemberDto memberDto : memberDtos) {
            System.out.println("memberDto.getMemberId() = " + memberDto.getMemberId());
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getTeamName() = " + memberDto.getTeamName());
        }
    }

    @Test
    public void findByNames() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void findOptionalMemberByUsername() {
        Member member1 = new Member("AAA", 10);
        memberRepository.save(member1);

        Member member = memberRepository.findOptionalMemberByUsername("AAA")
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        assertThat(member.getUsername()).isEqualTo("AAA");
    }

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        /*
         * page 1 -> offset = 0, limit = 10
         * page 2 -> offset = 10, limit = 10
         * */

        int age = 10;

        // 주의) page는 1이 아니라 0부터 시작이다.
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        /*
         * 반환 타입에 따라서 count 쿼리를 날릴지를 결정
         * Page 타입의 경우, count 쿼리가 실행됨
         * */
        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

        /*
         * Page를 사용할 때, Member 엔티티를 그대로 반환하면, 안된다.
         * 따라서 꼭! DTO로 변환해야 하는데 아래와 같이 map을 사용해서 변환하면 된다.
         */

        // then
        Page<MemberDto> memberDtos = page.map(
                member -> new MemberDto(member.getId(), member.getUsername(), null)
        );

        List<MemberDto> members = memberDtos.getContent();

        assertThat(members.size()).isEqualTo(3);
        assertThat(memberDtos.getTotalElements()).isEqualTo(5);
        assertThat(memberDtos.getNumber()).isEqualTo(0); // page 번호
        assertThat(memberDtos.getTotalPages()).isEqualTo(2); // page 갯수 총 2개 -> '(5, 4, 3), (2, 1)' 로 쪼개지기 때문
        assertThat(memberDtos.isFirst()).isTrue(); // 첫 번째 페이지이기 때문에 true
        assertThat(memberDtos.hasNext()).isTrue(); // 다음 페이지가 있나?
    }

    @Test
    public void slice() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        /*
         * 반환 타입에 따라서 count 쿼리를 날릴지를 결정
         * Slice 타입의 경우, count 쿼리가 실행되지 않음
         * */
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        // then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0); // page 번호
        assertThat(page.isFirst()).isTrue(); // 첫 번째 페이지이기 때문에 true
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있나?
    }

    @Test
    public void blukUpdate() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 29));
        memberRepository.save(new Member("member5", 40));

        // when
        // 실은 JPQL이 실행되기 전에, 영속성 컨텍스트를 flush하고 나서(Database에 반영) JPQL이 실행된다.
        int resultCount = memberRepository.bulkAgePlus(20);

        // Spring Data JPA는 @Modifying(clearAutomatically = true) 옵션으로 영속성 컨텍스트를 clear 해준다.
        // entityManager.clear();

        /*
         * 만약 여기서 find로 member5를 조회하고, member5의 나이는 몇일까?
         *
         * < 아직 트랜잭션이 커밋되지 않은 상태 >
         * age는 41이 아니고, 40이 나온다. 왜그럴까?
         * 이유는 다음과 같은데 위에서 Member 5명을 save 했다.
         * 이는 영속성 컨텍스트에만 있고, 실제 Database에는 반영된 상태가 아니다. (왜냐면, 아직 트랜잭션이 커밋된 상태가 아니기 때문)
         * 이 때, bulk Update는 영속성 컨텍스트를 무시하고, 바로 Database에 쿼리를 날린다.
         * 이로 인해 영속성 컨텍스트에 있는 Member5의 age는 40인데, Database에는 41인 상태이다.
         * 그리고 find를 통해 데이터를 가져올때, 먼저 영속성 컨텍스트 1차 캐시를 조회한다.
         * 만약 Member5가 있으면 가져오고, 없으면 Database에 직접 조회하는데,
         * 현재는 영속성 컨텍스트에 Member5의 정보가 있는 상태이기 때문에 1차 캐시에 데이터를 가져온다.
         * 즉, 데이터가 꼬이는 상황이 발생하게 된다.
         * 그렇기 때문에 bulk 연산을 수행한 후에는 영속성 컨텍스트를 모두 날려야 한다.
         *
         * */
        List<Member> members = memberRepository.findMembersByUsername("member5");
        Member member5 = members.get(0);
        System.out.println("member5 = " + member5);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        // given
        // Member1 -> TeamA
        // Member2 -> TeamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Member> members = memberRepository.findMembersEntityGraph();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        // given
        Member member = new Member("member1");
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.changeUsername("member2");

        entityManager.flush();
    }

    @Test
    public void lock() {
        // given
        Member member = new Member("member1");
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Member> members = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void findMemberCustom() {
        List<Member> members = memberRepository.findMemberCustom();
    }

    @Test
    public void projections() {
        // given
        Team teamA = new Team("A");
        entityManager.persist(teamA);

        Member member1 = new Member("member1", 0, teamA);
        Member member2 = new Member("member2", 0, teamA);
        entityManager.persist(member1);
        entityManager.persist(member2);

        entityManager.flush();
        entityManager.clear();

        // when
        List<NestedClosedProjections> result = memberRepository.findProjectionsByUsername("member1", NestedClosedProjections.class);

        for (NestedClosedProjections nestedClosedProjections : result) {
            System.out.println("username = " + nestedClosedProjections.getUsername());
            System.out.println("teamName = " + nestedClosedProjections.getTeam().getName());
        }
    }

    /**
     * Dirty Checking 테스트
     */
    @Test
    public void change_username() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("hyoseok", 29, team);
        memberRepository.save(member);

        entityManager.flush();
        entityManager.clear();

        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException(""));

        System.out.println("findMember.getUsername() = " + findMember.getUsername());

        findMember.changeUsername("change hyoseok");

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Dirty Checking 테스트
     */
    @Test
    public void unchange_username() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("hyoseok", 29, team);
        memberRepository.save(member);

        entityManager.flush();
        entityManager.clear();

        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException(""));

        findMember.changeUsername("hyoseok");

        entityManager.flush();
        entityManager.clear();
    }
}