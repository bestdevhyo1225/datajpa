package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.AUTO;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@ToString(of = { "id", "name" })
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team", cascade = ALL)
    private final List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
