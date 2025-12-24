package org.example.repository;

import org.example.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    @Query("SELECT c FROM Card c WHERE c.id NOT IN (SELECT lc.id FROM User u JOIN u.learnedCards lc WHERE u.username = :username)")
    Page<Card> findNotLearnedByUsername(String username, Pageable pageable);
}