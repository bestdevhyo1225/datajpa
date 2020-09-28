package study.datajpa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import study.datajpa.entity.Member;

@Setter
@Getter
@AllArgsConstructor
public class MemberDto {
    private Long memberId;
    private String username;
    private String teamName;

    public MemberDto(Member member) {
        this.memberId = member.getId();
        this.username = member.getUsername();
    }
}
