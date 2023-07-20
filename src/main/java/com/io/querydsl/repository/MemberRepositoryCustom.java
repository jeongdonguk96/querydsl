package com.io.querydsl.repository;

import com.io.querydsl.domain.MemberSearchCondition;
import com.io.querydsl.dto.MemberTeamDto;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> searchMember(MemberSearchCondition condition);
}
