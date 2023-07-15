package com.io.querydsl;

import com.io.querydsl.domain.Member;
import com.io.querydsl.domain.QMember;
import com.io.querydsl.domain.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.io.querydsl.domain.QMember.member;
import static com.io.querydsl.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class QuerydslBasicTests {

	@Autowired EntityManager em;
	JPAQueryFactory queryFactory;

	@Test
	@BeforeEach
	void before() {
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team(1L, "teamA");
		Team teamB = new Team(2L, "teamB");

		em.persist(teamA);
		em.persist(teamB);

		Member memberA = new Member(1L, "memberA", 10, teamA);
		Member memberB = new Member(2L, "memberB", 20, teamA);
		Member memberC = new Member(3L, "memberC", 30, teamB);
		Member memberD = new Member(4L, "memberD", 40, teamB);
		Member memberE = new Member(5L, null, 100);
		Member memberF = new Member(6L, "memberF", 100);
		Member memberG = new Member(7L, "memberG", 100);

		em.persist(memberA);
		em.persist(memberB);
		em.persist(memberC);
		em.persist(memberD);
		em.persist(memberE);
		em.persist(memberF);
		em.persist(memberG);

		em.flush();
		em.clear();
	}

	@Test
	void jpqlTest() {
		// memberA 조회하기
		Member findmember = em.createQuery("SELECT m FROM Member m WHERE m.username = :username", Member.class)
				.setParameter("username", "memberA")
				.getSingleResult();

		assertThat(findmember.getUsername()).isEqualTo("memberA");
	}

	@Test
	void querydslTest1() {
		QMember m = new QMember("m");

		Member findMember = queryFactory
				.select(m)
				.from(m)
				.where(m.username.eq("memberA"))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("memberA");
	}

	@Test
	void querydslTest2() {
		Member findMember = queryFactory
				.select(member)
				.from(member)
				.where(member.username.eq("memberA"))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("memberA");
	}

	@Test
	void search() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("memberA")
						.and(member.age.eq(10)))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("memberA");
	}

	@Test
	void searchAndParam() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(
						member.username.eq("memberA"),
						member.age.eq(10)
				)
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("memberA");
	}

	@Test
	void resultFetch() {
//		Member findMember = queryFactory
//				.selectFrom(member)
//				.fetchOne();
//		System.out.println("findMember = " + findMember);

		List<Member> findMembers = queryFactory
				.selectFrom(member)
				.fetch();
		System.out.println("findMembers.size = " + findMembers.size());

		Member findFirstMember = queryFactory
				.selectFrom(member)
				.fetchFirst();
		System.out.println("findFirstMember = " + findFirstMember);

		long count = queryFactory
				.selectFrom(member)
				.fetchCount();
		System.out.println("count = " + count);

		QueryResults<Member> findResults = queryFactory
				.selectFrom(member)
				.fetchResults();

		long total = findResults.getTotal();
		List<Member> findResultMembers = findResults.getResults();
		System.out.println("total = " + total);
		System.out.println("findResultMembers = " + findResultMembers);

	}

	@Test
	void sort() {
		// 1. 회원 나이 내림차순
		// 2. 회원 이름 올림차순
		// 3. 회원 이름이 없으면 마지막에 출력
		List<Member> members = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();

		Member memberF = members.get(0);
		Member memberG = members.get(1);
		Member memberNull = members.get(2);

		assertThat(memberF.getUsername()).isEqualTo("memberF");
		assertThat(memberG.getUsername()).isEqualTo("memberG");
		assertThat(memberNull.getUsername()).isNull();
	}

	@Test
	void paging1() {
		List<Member> members = queryFactory
				.selectFrom(member)
				.orderBy(member.id.asc())
				.offset(0)
				.limit(2)
				.fetch();

		System.out.println("members = " + members);

		assertThat(members.size()).isEqualTo(2);
		assertThat(members.get(0).getUsername()).isEqualTo("memberA");
		assertThat(members.get(1).getUsername()).isEqualTo("memberB");
	}

	@Test
	void paging2() {
		QueryResults<Member> memberQueryResults = queryFactory
				.selectFrom(member)
				.orderBy(member.id.asc())
				.offset(0)
				.limit(2)
				.fetchResults();

		System.out.println("memberQueryResults = " + memberQueryResults);

		assertThat(memberQueryResults.getTotal()).isEqualTo(7);
		assertThat(memberQueryResults.getOffset()).isEqualTo(0);
		assertThat(memberQueryResults.getLimit()).isEqualTo(2);
		assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
	}

	@Test
	void aggregation() {
		List<Tuple> result = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min()
				)
				.from(member)
				.fetch();

		Tuple tuple = result.get(0);

		assertThat(tuple.get(member.count())).isEqualTo(7);
		assertThat(tuple.get(member.age.sum())).isEqualTo(400);
	}

	// 팀의 이름과 팀의 평균 연령 구하기
	@Test
	void group() {
		List<Tuple> tuples = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.fetch();

		Tuple teamA = tuples.get(0);
		Tuple teamB = tuples.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}

	// teamA에 속한 모든 멤버 조회
	@Test
	void join() {
		List<Member> result = queryFactory
				.selectFrom(member)
				.join(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();

		System.out.println("result = " + result);

		assertThat(result)
				.extracting("username")
				.containsExactly("memberA", "memberB");
	}

	// 회원 이름과 팀 이름이 같은 회원 조회
	@Test
	void theta_join() {
		em.persist(new Member(8L, "teamA"));
		em.persist(new Member(9L, "teamB"));
		em.persist(new Member(10L, "teamC"));

		List<Member> result = queryFactory
				.select(member)
				.from(member, team)
				.where(member.username.eq(team.name))
				.fetch();

		System.out.println("result = " + result);

		assertThat(result)
				.extracting("username")
				.containsExactly("teamA", "teamB");
	}

	// 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	@Test
	void joinOnFiltering() {
		List<Tuple> result = queryFactory
				.select(member, team) // 조회 대상이 2개 이상이면 Tuple로 조회
				.from(member)
				.leftJoin(member.team, team) // leftjoin으로 member는 전부, team은 조건으로 필터
				.on(team.name.eq("teamA")) // 조인의 조건
				.fetch();

		System.out.println("result = " + result);

	}

	// 연관관계가 없는 회원 이름과 팀 이름이 같은 회원 조회
	@Test
	void theta_join2() {
		em.persist(new Member(8L, "teamA"));
		em.persist(new Member(9L, "teamB"));
		em.persist(new Member(10L, "teamC"));

		List<Tuple> result = queryFactory
				.select(member, team) // 회원과 팀을 각각 조회
				.from(member)
				.leftJoin(team).on(member.username.eq(team.name)) // team을 통째로 조인하는데, 조인될 때 조건이 회원 이름 = 팀 이름
				.fetch();

		System.out.println("result = " + result);

	}

	@Test
	void fetchJoin1() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team)
				.where(member.username.eq("memberA"))
				.fetchOne();

		System.out.println("findMember = " + findMember);

		assertThat(findMember.getTeam().getName()).isEqualTo("teamA");
	}

	@Test
	void fetchJoi2() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team)
				.fetchJoin()
				.where(member.username.eq("memberA"))
				.fetchOne();

		System.out.println("findMember = " + findMember);

		assertThat(findMember.getTeam().getName()).isEqualTo("teamA");
	}
}
