package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

/*
* 규칙) XXXXImpl -> 구현 Repository에는 마지막에 RepositoryImpl을 붙여야한다.
* */
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberCustomRepository {

    private final EntityManager entityManager;

    @Override
    public List<Member> findMemberCustom() {
        return entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}
