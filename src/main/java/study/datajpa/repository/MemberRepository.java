package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*
* 구현체를 개발자가 만들어준 적이 없고, Interface만 있는데 어떻게 동작할까?
* -> Spring Data JPA가 Interface를 보고 구현체를 직접 만들어서 주입하기 때문에 동작했던 것이다.
*
* @Repository 어노테이션 생략 가능
* -> Spring Data JPA가 Component Scan을 자동으로 처리하기 때문에 생략해도 된다.
*/
public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    /*
     * 메소드 이름으로 쿼리 생성 (반드시 관례를 지켜서 작성해야함)
     *
     * https://docs.spring.io/spring-data/jpa/docs/2.3.4.RELEASE/reference/html/#jpa.query-methods.query-creation
     * 해당 URL을 참고하면, 어떻게 관례를 지켜서 작성해야하는지 알 수 있음
     *
     * 심각한 문제는? 조건에 따라 메소드명이 너무길어짐
     *
     * 2개 조건까지는 괜찮지만, 그 이상을 넘어가면 @Query 어노테이션을 사용해서 해결하자
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /*
    * @Query 어노테이션 활용
    *
    * 1. 메소드명을 간단하게 사용할 수 있다는 장점이 있다.
    * 2. Query가 오타인 상태에서 애플리케이션이 실행을 위한 로딩을 할 때, 에러를 잡는다는 장점이 있다.
    *
    * -> 실무에서 많이 쓰는 방식이다!
    * -> 쿼리가 복잡할 때, 다음과 같은 방식을 쓰도록 하자!
    * */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findMembers(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernames();

    // DTO 조회가 필요하다면, Querydsl을 사용하도록 하자...
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDtos();

    // In절은 실무에서 많이 사용하기 때문에 다음과 같이 사용하면 된다.
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    // 리스트
    List<Member> findMembersByUsername(String username);

    // 옵셔녈 단 건 -> 데이터가 있을수도 있고, 없을수도 있으면 Optional을 쓰는게 맞다.
    Optional<Member> findOptionalMemberByUsername(String username);

    // 단 건
    Member findMemberByUsername(String username);

    /*
    * < 페이징 처리 >
    *
    * 반환 타입에 따라서 count 쿼리를 날릴지를 결정
    * Page 타입의 경우, count 쿼리가 실행됨
    *
    * 실무를 진행하다 보면, 쿼리가 복잡해지거나, 데이터가 많아지면 덩달아 count 쿼리도 해당되는 모든 데이터를 가져오기 때문에
    * 성능이 굉장히 느려진다. 따라서 다른 방법으로 해결해야 한다.
    * -> @Query 어노테이션을 사용해서 쿼리를 분리할 수 있다. (value, countQuery)
    *
    * */
    @Query(value = "select m from Member m left join fetch m.team t",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findPageByAge(int age, Pageable pageable);

    /*
    * < Slice 처리 >
    *
    * Slice는 count 쿼리를 실행하지 않는다.
    * limit + 1 쿼리를 실행한다.
    * -> limit가 3이면, +1 해서 limit 4의 쿼리를 실행한다.
    *
    * */
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    /*
    * @Modifying이 있어야 executeUpdate()가 실행된다.
    * 없으면, getResultList(), getSingleResult()를 호출하고 다음과 같은 에러를 발생시킨다.
    * -> org.springframework.dao.InvalidDataAccessApiUsageException:
    * -> org.hibernate.hql.internal.QueryExecutionRequestException: Not supported for DML operations
    *
    * JPA에서 Bulk 연산은 주의사항이 있다.
    *
    * < 아직 트랜잭션이 커밋되지 않은 상황에서 발생하는 문제 - MemberRepositoryTest 참고 >
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
    * clearAutomatically = true
    * -> 영속성 컨텍스트를 clear 해주는 옵션
    * */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMembersFetchJoin();

    /* -------------- @EntityGraph를 사용하면, Fetch Join을 편리하게 할 수 있다. -------------- */
    /*
    * JPQL 없이도 fetch join을 사용하는 방법은 다음과 같다.
    * -> @EntityGraph(attributePaths = { "team" })
    * -> 그냥 @EntityGraph는 fetch join이라고 보면 된다.
    * */
    @Override
    @EntityGraph(attributePaths = { "team" })
    List<Member> findAll();

    /*
    * 이렇게 해도 fetch join으로 사용할 수 있다.
    * */
    @Query("select m from Member m")
    @EntityGraph(attributePaths = { "team" })
    List<Member> findMembersEntityGraph();

    /*
    * 메소드 이름으로 fetch join 하는 방법
    * */
    @EntityGraph(attributePaths = ("team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    /*
    * @QueryHints의 readOnly를 사용하면
    * 말 그대로 조회만 가능하고, 변경 감지 체크를 안한다.
    * 왜냐하면, 스냅샷이 없기 때문에 영속성 컨텍스트 내부적으로 읽기 전용이구나 판단하고, 최적화를 해버린다.
    *
    * 해당 기능은 충분한 성능 테스트를 해보고, 판단하에 필요한 곳에만 적용하도록 하자.
    * 조회 성능이 느리다고, 이걸 적용하기 보다는 이미 캐시를 적용했어야 한다.
    * */
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    /*
    * select for update
    * */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);


    <T> List<T> findProjectionsByUsername(@Param("username") String username, Class<T> type);
    /*
    * 그렇다면 동적 쿼리는 어떻게 해야할까? -> 그냥 동적 쿼리를 편하게 작성할 수 있는 Querydsl을 쓰자!
    * */
}
