package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.GenerationType.AUTO;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Item extends BaseTimeEntity implements Persistable<String> {
    /*
    * id는 영속성 컨텍스트에 persist 되어야 생성된다. 그전에는 생성되지 않는다.
    * */
//    @Id
//    @GeneratedValue(strategy = AUTO)
//    private Long id;

    /*
     * 조금 심화로 들어가서 테이블을 분할해야 하는 상황에서는 ID에 @GeneratedValue를 사용하기 힘든데
     * 이때 Persistable 인터페이스의 getId(), isNew() 메소드를 오버라이드해서 직접 구현해야한다.
     * */
    @Id
    private String id;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return super.getCreatedDate() == null;
    }
}
