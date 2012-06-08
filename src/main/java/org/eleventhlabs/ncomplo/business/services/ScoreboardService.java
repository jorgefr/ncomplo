package org.eleventhlabs.ncomplo.business.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eleventhlabs.ncomplo.business.entities.Bet;
import org.eleventhlabs.ncomplo.business.entities.League;
import org.eleventhlabs.ncomplo.business.entities.User;
import org.eleventhlabs.ncomplo.business.entities.repositories.BetRepository;
import org.eleventhlabs.ncomplo.business.entities.repositories.GameRepository;
import org.eleventhlabs.ncomplo.business.entities.repositories.GameSideRepository;
import org.eleventhlabs.ncomplo.business.entities.repositories.LeagueRepository;
import org.eleventhlabs.ncomplo.business.entities.repositories.UserRepository;
import org.eleventhlabs.ncomplo.business.views.ScoreboardEntry;
import org.eleventhlabs.ncomplo.business.views.ScoreboardEntry.ScoreboardEntryComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class ScoreboardService {
    
    
    @Autowired
    private LeagueRepository leagueRepository;
    
    @Autowired
    private BetRepository betRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private GameSideRepository gameSideRepository;
    
    @Autowired
    private UserRepository userRepository;
 
    
    
    
    public ScoreboardService() {
        super();
    }
    
    
    

    @Transactional
    public List<ScoreboardEntry> computeScoreboard(
            final Integer leagueId, final Integer roundId, final Locale locale) {

        
        final List<ScoreboardEntry> scoreboard = new ArrayList<ScoreboardEntry>();
        
        final League league = this.leagueRepository.findOne(leagueId);
        final Set<User> participants = league.getParticipants();
        
        for (final User participant : participants) {
            
            int totalPoints = 0;
            
            final List<Bet> bets = 
                    this.betRepository.findByLeagueIdAndUserLogin(leagueId, participant.getLogin());
            
            for (final Bet bet : bets) {
                if (bet.getPointsEarned() != null) {
                    if (roundId != null) {
                        if (!bet.getGame().getRound().getId().equals(roundId)) {
                            continue;
                        }
                    }
                    totalPoints += bet.getPointsEarned().intValue();
                }
            }
            
            
            
        }
        
        
        Collections.sort(scoreboard, new ScoreboardEntryComparator(locale));
        
        return scoreboard;
        
    }
    
}