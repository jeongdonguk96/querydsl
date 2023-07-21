package com.io.querydsl.contorller;

import com.io.querydsl.domain.MemberSearchCondition;
import com.io.querydsl.dto.MemberTeamDto;
import com.io.querydsl.repository.MemberJpaRepository;
import com.io.querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/members/v1")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search2(condition);
    }

    @GetMapping("/members/v2")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition,
                                              Pageable pageable) {
        return memberRepository.searchMemberPagingSimple(condition, pageable);
    }

    @GetMapping("/members/v3")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition,
                                              Pageable pageable) {
        return memberRepository.searchMemberPagingComplex(condition, pageable);
    }
}
