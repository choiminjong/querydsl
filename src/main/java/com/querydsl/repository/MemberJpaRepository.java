package com.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.dto.MemberSearchCondition;
import com.querydsl.dto.MemberTeamDto;
import com.querydsl.dto.QMemberTeamDto;
import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.entity.QTeam;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Repository
@Service
@RequiredArgsConstructor
public class MemberJpaRepository {

    //private final EntityManager em;
    //private final JPAQueryFactory queryFactory;

    private JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Member> searchByBuilder(MemberSearchCondition condition) {
        System.out.println("condition = " + condition);

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        List<Member> fetch = queryFactory.selectFrom(member).where(builder).fetch();

        System.out.println("fetch = " + fetch);

        return fetch;
    }
}
