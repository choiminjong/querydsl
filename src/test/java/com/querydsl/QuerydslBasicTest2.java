package com.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.dto.*;
import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.entity.QTeam;
import com.querydsl.entity.Team;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.repository.MemberJpaRepository;
import com.querydsl.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest2 {

    @PersistenceContext
    EntityManager em;

    @PersistenceContext
    EntityManager emf;

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    MemberRepository memberRepository;

    QMember member = QMember.member;
    QTeam team = QTeam.team;

    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                           .select(member.username)
                           .from(member)
                           .fetch();

        System.out.println("result = " + result);
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                            .select(member.username, member.age)
                            .from(member)
                            .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            System.out.println("username = " + username);
            Integer age = tuple.get(member.age);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new com.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                                .select(
                                         Projections.bean(MemberDto.class, member.username, member.age)
                                        )
                                .from(member)
                                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                                 .select(
                                          Projections.fields(MemberDto.class, member.username, member.age)
                                        )
                                .from(member)
                                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                                .from(member)
                                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDto() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                                .select(
                                    Projections.fields(
                                    UserDto.class, member.username.as("name"),
                                    ExpressionUtils.as(
                                            JPAExpressions.select(memberSub.age.max()).from(memberSub),"age")
                                    )
                                )
                                .from(member)
                                .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                                .select(new QMemberDto(member.username, member.age))
                                .from(member)
                                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void dynamicQueryByBooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
        System.out.println("result = " + result);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQueryByWhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
               .selectFrom(member)
               .where(usernameEq(usernameCond), ageEq(ageCond))
               .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }


    @Test
    public void searchByBuilderTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(10);
        condition.setAgeLoe(20);
        condition.setTeamName("teamA");

        List<MemberTeamDto> result = memberRepository.search(condition);
        System.out.println("result = " + result);
        assertThat(result).extracting("username").containsExactly("member1","member2");
    }

    @Test
    public void findMemberTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(10);
        condition.setAgeLoe(20);
        condition.setTeamName("teamA");

        List<Member> result = memberRepository.findMember(condition);
        assertThat(result).extracting("username").containsExactly("member1","member2");
    }

    @Test
    public void searchPageSimpleTest() {
//        Team teamA = new Team("teamA");
//        Team teamB = new Team("teamB");
//        em.persist(teamA);
//        em.persist(teamB);
//
//        Member member1 = new Member("member1", 10, teamA);
//        Member member2 = new Member("member2", 20, teamA);
//        Member member3 = new Member("member3", 30, teamB);
//        Member member4 = new Member("member4", 40, teamB);
//        em.persist(member1);
//        em.persist(member2);
//        em.persist(member3);
//        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);
        System.out.println("result = " + result.getContent());
        /*
        result = [
            MemberTeamDto(memberId=41, username=member1, age=10, teamId=60, teamName=teamA),
            MemberTeamDto(memberId=42, username=member2, age=11, teamId=60, teamName=teamA),
            MemberTeamDto(memberId=43, username=member3, age=12, teamId=null, teamName=null)]
         */
        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchPageComplexTest() {

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(1, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, pageRequest);
        System.out.println("result.getNumberOfElements = " + result.getNumberOfElements());
        System.out.println("result.getContent = " + result.getContent());
        /*
        result.getNumberOfElements = 3
        result.getContent = [
            MemberTeamDto(memberId=44, username=member4, age=13, teamId=null, teamName=null),
            MemberTeamDto(memberId=45, username=member5, age=14, teamId=null, teamName=null),
            MemberTeamDto(memberId=46, username=member6, age=15, teamId=null, teamName=null)]
        */

        assertThat(result.getNumberOfElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member4","member5","member6");
    }
}
