package com.querydsl.repository;

import com.querydsl.entity.Member;
import com.querydsl.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberRepository  extends JpaRepository<Member, Long>,
                                           QuerydslPredicateExecutor<Member>, MemberRepositoryCustom {
    Member findByUsername(String username);
}
