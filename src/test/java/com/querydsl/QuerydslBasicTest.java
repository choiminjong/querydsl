package com.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.entity.Member;
import com.querydsl.entity.QMember;
import com.querydsl.entity.QTeam;
import com.querydsl.entity.Team;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    @PersistenceContext
    EntityManager emf;

    @Autowired
    JPAQueryFactory queryFactory;

    QMember member = QMember.member;
    QTeam team = QTeam.team;

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
        Member findMember = queryFactory.selectFrom(member) // select + from
                            .where(member.username.eq("member1"))
                            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        QMember member = QMember.member;
        Member findMember = queryFactory
                            .selectFrom(member)
                            .where(member.username.eq("member1")
                            .and(member.age.eq(10)))
                            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        long fetchCount = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                             .selectFrom(member)
                             .where(member.age.eq(100))
                             .orderBy(member.age.desc(), member.username.asc().nullsLast())
                             .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void join() {
        List<Member> result = queryFactory
                              .selectFrom(member)
                              .join(member.team, team)
                              .where(team.name.eq("teamA"))
                              .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    public void thetaJoin() {
        List<Member> result = queryFactory
                              .select(member)
                              .from(member, team)
                              .where(member.username.eq(team.name))
                              .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @Test
    public void joinOnFiltering() {
        /*
         * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
         * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
         */
        List<Tuple> result = queryFactory
                            .select(member, team)
                            .from(member)
                            .leftJoin(member.team, team).on(team.name.eq("teamA"))
                            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void joinOnNoRelation() {
        /*
         * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
         * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
         */
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                            .select(member, team)
                            .from(member)
                            .leftJoin(team).on(member.username.eq(team.name))
                            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    @Test
    public void fetchJoinNonUse() {
        em.flush();
        em.clear();


        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);
    }

    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(
                        member.age.eq(
                                JPAExpressions.select(memberSub.age.max()).from(memberSub))
                        )
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(19);
    }

    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(
                        member.age.goe(
                        JPAExpressions.select(memberSub.age.avg()).from(memberSub))
                      )
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(15,16,17,18,19);
    }

    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions.select(memberSub.age)
                                       .from(memberSub)
                                        .where(memberSub.age.gt(10))
                       ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(11,12,13,14,15,16,17,18,19);
    }

    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(
                        member.username,
                        JPAExpressions.select(memberSub.age.avg()).from(memberSub)
                        ).from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(JPAExpressions.select(memberSub.age.avg()).from(memberSub)));
        }
    }

    @Test
    public void caseSimple() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void caseComplicate() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 11)).then("0~11살")
                        .when(member.age.between(12, 20)).then("12~20살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void caseWithOrderBy() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                                            .when(member.age.between(0, 12)).then(2)
                                            .when(member.age.between(13, 17)).then(1)
                                            .otherwise(3);

        List<Tuple> result = queryFactory
                            .select(member.username, member.age, rankPath)
                            .from(member)
                            .orderBy(rankPath.desc())
                            .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " + rank);
        }
    }

    @Test
    public void constant() {
        Tuple result = queryFactory
                      .select(member.username, Expressions.constant("A"))
                      .from(member)
                      .fetchFirst();

        System.out.println("result = " + result);
    }

    @Test
    public void concat() {
        String result = queryFactory
                        .select(member.username.concat("_").concat(member.age.stringValue()))
                        .from(member)
                        .where(member.username.eq("member1"))
                        .fetchOne();

        System.out.println("result = " + result);
    }

}
