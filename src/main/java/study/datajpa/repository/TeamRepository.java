package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Team;

/*
 * @Repository 어노테이션 생략 가능
 * -> Spring Data JPA가 Component Scan을 자동으로 처리하기 때문에 생략해도 된다.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
}
