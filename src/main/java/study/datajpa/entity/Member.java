package study.datajpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.AUTO;
import static lombok.AccessLevel.*;

/*
 * protected로 만든 이유?
 * JPA 표준 스펙에서 Entity는 기본적으로 Default 생성자가 있어야 한다.
 *
 * protected Member() {} 해놔야 함
 *
 * 싱단에 @NoArgsConstructor(access = AccessLevel.PROTECTED) 로 대체할 수 있음
 * */

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@ToString(of = { "id", "username", "age" }) // team을 적으면, 큰일 남. 연관 관계까지 다 출력하기 때문
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            this.changeTeam(team);
        }
    }

    /* 연관 관계 편의 메소드 */
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    public void changeUsername(String username) {
        this.username = username;
    }
}
