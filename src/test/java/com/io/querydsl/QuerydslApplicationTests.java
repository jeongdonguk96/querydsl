package com.io.querydsl;

import com.io.querydsl.persistence.Hello;
import com.io.querydsl.persistence.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Commit
@Transactional
@SpringBootTest
class QuerydslApplicationTests {

	@Autowired EntityManager entityManager;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		entityManager.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(entityManager);
//		QHello qHello = new QHello("h");
		QHello qHello = QHello.hello;

		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
