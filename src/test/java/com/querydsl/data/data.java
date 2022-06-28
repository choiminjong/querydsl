package com.querydsl.data;

import com.querydsl.entity.Member;
import com.querydsl.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class data {
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Rollback(false)
    public void createMember(){
        for(int i=1;i<=10;i++){
            String username ="member"+i;
            int age =10+i;
            Member member = new Member(username,age);
            Member save = memberRepository.save(member);
            System.out.println("save = " + save);
        }
    }
}
