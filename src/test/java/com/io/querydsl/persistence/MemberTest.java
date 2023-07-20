package com.io.querydsl.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SpringBootTest
class MemberTest {

    @Autowired EntityManager em;

    @Test
    void entityTest() {
        Team teamA = new Team(1L, "teamA");
        Team teamB = new Team(2L, "teamB");

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

        List<Member> members = em.createQuery("SELECT m FROM Member m", Member.class).getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }
}