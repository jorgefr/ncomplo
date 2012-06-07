package org.eleventhlabs.ncomplo.business.entities.repositories;

import org.eleventhlabs.ncomplo.business.entities.Bet;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface BetRepository 
        extends PagingAndSortingRepository<Bet,Integer> {
    
    // No methods to add
    
}
    