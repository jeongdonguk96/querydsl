package com.io.querydsl.repository;

import com.io.querydsl.domain.MemberSearchCondition;
import com.io.querydsl.dto.MemberTeamDto;
import com.io.querydsl.dto.QMemberTeamDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.io.querydsl.persistence.QMember.member;
import static com.io.querydsl.persistence.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    // 검색
    @Override
    public List<MemberTeamDto> searchMember(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(memberTeamDtoEq2(condition))
                .fetch();
    }


    // 단순 페이징
    @Override
    public Page<MemberTeamDto> searchMemberPagingSimple(MemberSearchCondition condition,
                                                        Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(memberTeamDtoEq2(condition))
                .offset(pageable.getOffset()) //
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }


    // 복잡한 페이징
    @Override
    public Page<MemberTeamDto> searchMemberPagingComplex(MemberSearchCondition condition,
                                                         Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(memberTeamDtoEq2(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(memberTeamDtoEq2(condition))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    private BooleanBuilder memberTeamDtoEq2(MemberSearchCondition memberSearchCondition) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder.and(usernameEq(memberSearchCondition.getUsername()))
                .and(teamNameEq(memberSearchCondition.getTeamName()))
                .and(ageGoe(memberSearchCondition.getAgeGoe()))
                .and(ageLoe(memberSearchCondition.getAgeLoe()));
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}

