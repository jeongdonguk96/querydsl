package com.io.querydsl.repository;

import com.io.querydsl.domain.MemberSearchCondition;
import com.io.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> searchMember(MemberSearchCondition condition);
    Page<MemberTeamDto> searchMemberPagingSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchMemberPagingComplex(MemberSearchCondition condition, Pageable pageable);
}
