package com.io.querydsl.repository;

import com.io.querydsl.persistence.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface MemberRepository extends JpaRepository<Member, Long>,
                                          MemberRepositoryCustom {
}
