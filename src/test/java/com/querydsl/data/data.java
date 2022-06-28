package com.querydsl.data;

import com.querydsl.entity.Member;
import com.querydsl.entity.Team;
import com.querydsl.repository.MemberRepository;
import com.querydsl.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class data {
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;


    @Test
    @Rollback(false)
    public void createMember(){
        for(int i=1;i<=10;i++){
            String username ="member"+i;
            int age =9+i;
            Member member = new Member(username,age);
            Member save = memberRepository.save(member);
            System.out.println("save = " + save);
        }
    }

    @Test
    @Rollback(false)
    public void createTeam(){
        Team teamA = new Team("teamA");
        Team teamAsave = teamRepository.save(teamA);

        Team teamB = new Team("teamB");
        Team teamBsave = teamRepository.save(teamB);

        em.flush();
        em.clear();

        Member member1 = memberRepository.findByUsername("member1");
        member1.setTeam(teamAsave);

        Member member2 = memberRepository.findByUsername("member2");
        member2.setTeam(teamAsave);
    }

}
