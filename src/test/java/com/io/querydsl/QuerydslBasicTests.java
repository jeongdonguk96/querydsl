package com.io.querydsl;

import com.io.querydsl.domain.Member;
import com.io.querydsl.domain.QMember;
import com.io.querydsl.domain.Team;
import com.io.querydsl.dto.MemberDto;
import com.io.querydsl.dto.QMemberDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
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
import static com.querydsl.jpa.JPAExpressions.select;
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
//		Member memberE = new Member(5L, null, 100);
//		Member memberF = new Member(6L, "memberF", 100);
//		Member memberG = new Member(7L, "memberG", 100);

		em.persist(memberA);
		em.persist(memberB);
		em.persist(memberC);
		em.persist(memberD);
//		em.persist(memberE);
//		em.persist(memberF);
//		em.persist(memberG);

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
	void fetchJoin2() {
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

	// 나이가 가장 많은 회원 조회
	@Test
	void subQuery() {
		QMember subMember = new QMember("subMember");
		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(
						select(subMember.age.max())
								.from(subMember)
				))
				.fetch();

		assertThat(result)
				.extracting("age")
				.containsExactly(40);
	}

	// 나이가 평균 이상인 회원 조회
	@Test
	void subQuery2() {
		QMember subMember = new QMember("subMember");
		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.goe(
						select(subMember.age.avg())
								.from(subMember)
				))
				.fetch();

		assertThat(result)
				.extracting("age")
				.containsExactly(30, 40);
	}

	// 나이가 10살 이상인 회원 조회
	@Test
	void subQuery3() {
		QMember subMember = new QMember("subMember");
		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.in(
						select(subMember.age)
								.from(subMember)
								.where(member.age.gt(10))
				))
				.fetch();

		assertThat(result)
				.extracting("age")
				.containsExactly(20, 30, 40);
	}

	@Test
	void subQuery4() {
		QMember subMember = new QMember("subMember");

		List<Tuple> result = queryFactory
				.select(member.username,
						select(subMember.age.avg())
								.from(subMember))
				.from(member)
				.fetch();


	}

	@Test
	void basicCase() {
		List<Tuple> result = queryFactory
				.select(member.username, member.age
						.when(10).then("열살")
						.when(20).then("스무살")
						.when(30).then("서른살")
						.otherwise("기타")
				)
				.from(member)
				.fetch();
	}

	@Test
	void complexCase() {
		List<Tuple> result = queryFactory
				.select(member.username, new CaseBuilder()
						.when(member.age.between(0, 19)).then("미성년자")
						.when(member.age.between(20, 29)).then("청년")
						.when(member.age.between(30, 39)).then("삼촌")
						.otherwise("아저씨")
				)
				.from(member)
				.fetch();

		System.out.println("result = " + result);
	}

	@Test
	void constant() {
		List<Tuple> result = queryFactory
				.select(member.username, Expressions.constant("남성"))
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	void concat() {
		List<String> result = queryFactory
				.select(member.username.concat(" : ").concat(member.team.name))
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// username이라는 하나의 문자열 컬럼만 반환
	@Test
	void StringProjection() {
		List<String> result = queryFactory
				.select(member.username)
				.from(member)
				.fetch();
	}

	// username과 age라는 두 컬럼 Tuple로 반환
	@Test
	void tupleProjection() {
		List<Tuple> result = queryFactory
				.select(member.username, member.age)
				.from(member)
				.fetch();
	}

	// 세터 방식 dto 반환
	@Test
	void dtoBySetter() {
		List<MemberDto> result = queryFactory
				.select(Projections.bean(MemberDto.class,
						member.username,
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// 필드 주입 방식 dto 반환
	@Test
	void dtoByField() {
		List<MemberDto> result = queryFactory
				.select(Projections.fields(MemberDto.class,
						member.username,
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// 생성자 방식 dto 반환
	@Test
	void dtoByConstructor() {
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class,
						member.username,
						member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// @QueryProjection 어노테이션 Dto 반환
	@Test
	void dtoByQueryProjection() {
		List<MemberDto> result = queryFactory
				.select(new QMemberDto(member.username, member.age))
				.from(member)
				.fetch();
	}
}
