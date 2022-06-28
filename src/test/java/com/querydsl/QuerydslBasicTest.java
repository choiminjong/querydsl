package com.querydsl;

import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        /* ... */
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    }

    @Test
    public void startJPQL() {
        String jpqlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(jpqlString, Member.class)
                            .setParameter("username", "member1")
                            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        Member findMember = queryFactory.selectFrom(qMember) // select + from
                            .where(qMember.username.eq("member1"))
                            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
