package com.teambB.koting.repository;

import com.teambB.koting.domain.Meeting;
import com.teambB.koting.domain.Member;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional
public class MemberRepository {

  private final EntityManager em;

  public void save(Member member) {
    if (member.getId() == null) {
      em.persist(member);
    } else {
      em.merge(member);
    }
  }

  public Member findById(Long id) {
    return em.find(Member.class, id);
  }

  public Member findByEmail(String email) {
    return em.createQuery("select m from Member m where m.email = :email", Member.class)
        .setParameter("email", email)
        .getSingleResult();
  }

  public List<Member> findByNumber(String number) {
    return em.createQuery("select m from Member m where m.number = :number", Member.class)
        .setParameter("number", number)
        .getResultList();
  }

  public List<Member> findAll() {
    return em.createQuery("select m from Member m", Member.class)
        .getResultList();
  }

  public List<Member> findByNumberList(String number) {
    return em.createQuery("select m from Member m where m.number = :number", Member.class)
        .setParameter("number", number)
        .getResultList();
  }
}
