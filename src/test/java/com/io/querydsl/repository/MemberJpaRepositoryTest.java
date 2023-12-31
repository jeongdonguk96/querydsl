package com.io.querydsl.repository;

import com.io.querydsl.domain.MemberSearchCondition;
import com.io.querydsl.dto.MemberTeamDto;
import com.io.querydsl.persistence.Member;
import com.io.querydsl.persistence.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;
    JPAQueryFactory queryFactory;



    @Test
    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member(1L, "memberA", 10, teamA);
        Member memberB = new Member(2L, "memberB", 20, teamA);
        Member memberC = new Member(3L, "memberC", 30, teamB);
        Member memberD = new Member(4L, "memberD", 40, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        em.flush();
        em.clear();
    }

    // 동적 쿼리 WHERE 다중
    @Test
    void searchTest1() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        for (MemberTeamDto memberTeamDto : result) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }

        assertThat(result).extracting("username").containsExactly("memberD");
    }

    // 동적 쿼리 WHERE 다중
    @Test
    void searchTest2() {
        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setUsername("memberD");
        condition.setTeamName("teamB");
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);

        List<MemberTeamDto> result2 = memberJpaRepository.search2(condition);
        List<Member> members = memberJpaRepository.search3(condition);


        assertThat(result2).extracting("username").containsExactly("memberD");
        assertThat(members).extracting("username").containsExactly("memberD");
    }

}