package org.eleventhlabs.ncomplo.web.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eleventhlabs.ncomplo.business.entities.BetNew;
import org.eleventhlabs.ncomplo.business.entities.BetDefinition;
import org.eleventhlabs.ncomplo.business.entities.BetType;
import org.eleventhlabs.ncomplo.business.entities.MatchNew;
import org.eleventhlabs.ncomplo.business.entities.MatchWinner;
import org.eleventhlabs.ncomplo.business.entities.RoundNew;
import org.eleventhlabs.ncomplo.business.entities.TeamNew;
import org.eleventhlabs.ncomplo.business.services.BetNewService;
import org.eleventhlabs.ncomplo.business.util.DateUtils;
import org.eleventhlabs.ncomplo.web.application.NComploApplication;
import org.eleventhlabs.ncomplo.web.utils.JavascriptEventConfirmation;

public class BetsForOwnerAdminPage extends BaseAdminPage {

    private static final long serialVersionUID = 5123309121L;

    
    public BetsForOwnerAdminPage(final String ownerName) {
        super();
        add(new BookmarkablePageLink<Object>("linkMatchAdmin", MatchAdminPage.class));
        add(new BookmarkablePageLink<Object>("linkBetAdmin", BetAdminPage.class));
        add(new FeedbackPanel("feedbackPanel"));
        add(new BetForm("betForm", ownerName));
    }
    
    
    
    
    
    private static class BetForm extends Form<Object> {

        private static final long serialVersionUID = -76438631157065039L;

        protected final String ownerName;
        
        private final TextField<String> ownerAccountId;
        
        private final LinkedHashMap<Integer,BetType> betTypeByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamAByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamBByMatchId;
        private final LinkedHashMap<Integer,TextField<Integer>> scoreAByMatchId;
        private final LinkedHashMap<Integer,TextField<Integer>> scoreBByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<MatchWinner>> matchWinnerByMatchId;
        
        
        public BetForm(String id, final String ownerName) {
            
            super(id);
            
            this.ownerName = ownerName;


            final MatchBetModel matchBetModel = new MatchBetModel(this.ownerName);
            
            final List<Map.Entry<MatchNew, BetNew>> currentBets = matchBetModel.getObject();
            
            add(new Label("ownerName", new Model<String>(this.ownerName)));
            
            this.ownerAccountId = new TextField<String>("ownerAccountId", new Model<String>());
            add(this.ownerAccountId);
            
            if (!currentBets.isEmpty()) {
                this.ownerAccountId.setModelObject(currentBets.get(0).getValue().getOwnerAccountId());
            }
            
            this.betTypeByMatchId = new LinkedHashMap<Integer, BetType>();
            this.teamAByMatchId = new LinkedHashMap<Integer, DropDownChoice<TeamNew>>();
            this.teamBByMatchId = new LinkedHashMap<Integer, DropDownChoice<TeamNew>>();
            this.scoreAByMatchId = new LinkedHashMap<Integer, TextField<Integer>>();
            this.scoreBByMatchId = new LinkedHashMap<Integer, TextField<Integer>>();
            this.matchWinnerByMatchId = new LinkedHashMap<Integer, DropDownChoice<MatchWinner>>();
            
            add(new MatchesForBetsListView("match", this.betTypeByMatchId,
                    this.teamAByMatchId, this.teamBByMatchId,
                    this.scoreAByMatchId, this.scoreBByMatchId,
                    this.matchWinnerByMatchId, matchBetModel));
            
            
            final Link<Object> removeBetsLink =
                new Link<Object>("removeBets") {
    
                    private static final long serialVersionUID = 1212355532L;
                    
                    @Override
                    public void onClick() {
                        
                        NComploApplication.get().getBetService().
                                deleteAllBetsForOwner(BetForm.this.ownerName);
                        setResponsePage(BetAdminPage.class);
                    }
                 
                };
            removeBetsLink.add(new JavascriptEventConfirmation("onclick", "\u00BFSeguro que desea borrar todas las apuestas de esta persona?"));
            add(removeBetsLink);
            
        }


        
        @Override
        protected void onSubmit() {
            
            super.onSubmit();
            
            final List<BetDefinition> betDefinitions = new ArrayList<BetDefinition>();
            
            for (final Map.Entry<Integer, BetType> matchEntry : this.betTypeByMatchId.entrySet()) {
                
                final Integer matchId = matchEntry.getKey();
                final BetType betType = matchEntry.getValue();
                
                final DropDownChoice<TeamNew> teamAInput = this.teamAByMatchId.get(matchId);
                final DropDownChoice<TeamNew> teamBInput = this.teamBByMatchId.get(matchId);
                final TextField<Integer> scoreAInput = this.scoreAByMatchId.get(matchId);
                final TextField<Integer> scoreBInput = this.scoreBByMatchId.get(matchId);
                final DropDownChoice<MatchWinner> matchWinner = this.matchWinnerByMatchId.get(matchId);

//                switch (betType) {
//                
//                    case RESULT_AND_SCORE:
//                        betDefinitions.add(
//                            new BetDefinition(matchId, scoreAInput.getModelObject(), scoreBInput.getModelObject()));
//                        break;
//                    case PRESENCE_IN_ROUND:
//                        betDefinitions.add(
//                                new BetDefinition(matchId, teamAInput.getModelObject(), teamBInput.getModelObject()));
//                        break;
//                    case FINAL:
//                        betDefinitions.add(
//                                new BetDefinition(matchId, teamAInput.getModelObject(), teamBInput.getModelObject(), matchWinner.getModelObject()));
//                        break;
//                }
                
            }

            final BetNewService betService = NComploApplication.get().getBetService();
            betService.setBets(this.ownerName, this.ownerAccountId.getModelObject(), betDefinitions);
            
            getSession().info("Las apuestas han sido correctamente almacenadas");
            
            setResponsePage(BetAdminPage.class);
            
        }

        
        
    }
    
    
    
    private static class MatchBetModel extends LoadableDetachableModel<List<Map.Entry<MatchNew,BetNew>>> {

        private static final long serialVersionUID = 7226824L;

        private final String ownerName;
        
        public MatchBetModel(final String ownerName) {
            super();
            this.ownerName = ownerName;
        }
        
        @Override
        protected List<Map.Entry<MatchNew,BetNew>> load() {
            // Should return an entry for every match, with a null bet
            // if there currently is no established bet for that user in that match.

            final List<MatchNew> allMatches =
                    NComploApplication.get().getMatchService().findAllMatchesOrderByDate();
            
            final Map<MatchNew,BetNew> betsByMatchForUser = 
                    NComploApplication.get().getBetService().findAllBetsForOwner(this.ownerName);
            
            final Map<MatchNew,BetNew> betsByAllMatches = new LinkedHashMap<MatchNew, BetNew>();
            for (final MatchNew match : allMatches) {
                final BetNew betForMatch = betsByMatchForUser.get(match);
                betsByAllMatches.put(
                        match, (betForMatch == null? new BetNew() : betForMatch));
            }
            
            return new ArrayList<Map.Entry<MatchNew,BetNew>>(betsByAllMatches.entrySet());
            
        }
        
    }
    
    
    
    private static class MatchesForBetsListView extends ListView<Map.Entry<MatchNew, BetNew>> {

        private static final long serialVersionUID = 579938465348690L;

        private final LinkedHashMap<Integer,BetType> betTypeByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamAByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamBByMatchId;
        private final LinkedHashMap<Integer,TextField<Integer>> scoreAByMatchId;
        private final LinkedHashMap<Integer,TextField<Integer>> scoreBByMatchId;
        private final LinkedHashMap<Integer,DropDownChoice<MatchWinner>> matchWinnerByMatchId;
        

        public MatchesForBetsListView(
                final String id,
                final LinkedHashMap<Integer,BetType> betTypeByMatchId,
                final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamAByMatchId,
                final LinkedHashMap<Integer,DropDownChoice<TeamNew>> teamBByMatchId,
                final LinkedHashMap<Integer,TextField<Integer>> scoreAByMatchId,
                final LinkedHashMap<Integer,TextField<Integer>> scoreBByMatchId,
                final LinkedHashMap<Integer,DropDownChoice<MatchWinner>> matchWinnerByMatchId,
                final MatchBetModel matchBetModel) {
            
            super(id, matchBetModel);
            this.betTypeByMatchId = betTypeByMatchId;
            this.teamAByMatchId = teamAByMatchId;
            this.teamBByMatchId = teamBByMatchId;
            this.scoreAByMatchId = scoreAByMatchId;
            this.scoreBByMatchId = scoreBByMatchId;
            this.matchWinnerByMatchId = matchWinnerByMatchId;
            
        }
        

        @Override
        protected void populateItem(final ListItem<Map.Entry<MatchNew,BetNew>> item) {

            final Map.Entry<MatchNew,BetNew> entry = item.getModelObject();
            final MatchNew match = entry.getKey();
            final BetNew bet = entry.getValue();
            
            final Integer matchId = match.getId();
            this.betTypeByMatchId.put(matchId, match.getBetType());
            
            item.add(new Label("round", new Model<RoundNew>(match.getRound())));
            item.add(new Label("date", DateUtils.toString(match.getDate())));
            item.add(new Label("name", match.getName()));

            
            final Label teamA = new Label("teamA", new Model<TeamNew>(match.getTeamA()));
            item.add(teamA);
            
            final DropDownChoice<TeamNew> teamAInput = 
                new DropDownChoice<TeamNew>("teamAInput", new Model<TeamNew>(bet.getTeamA()), Arrays.asList(TeamNew.ALL_TEAMS));
            item.add(teamAInput);
            this.teamAByMatchId.put(matchId, teamAInput);


            
            final Label teamB = new Label("teamB", new Model<TeamNew>(match.getTeamB()));
            item.add(teamB);
            
            final DropDownChoice<TeamNew> teamBInput = 
                new DropDownChoice<TeamNew>("teamBInput", new Model<TeamNew>(bet.getTeamB()), Arrays.asList(TeamNew.ALL_TEAMS));
            item.add(teamBInput);
            this.teamBByMatchId.put(matchId, teamBInput);

            
            
            final TextField<Integer> scoreAInput = 
                new TextField<Integer>("scoreAInput", new Model<Integer>(bet.getScoreA()));
            scoreAInput.setType(Integer.class);
            item.add(scoreAInput);
            this.scoreAByMatchId.put(matchId, scoreAInput);
            
            final TextField<Integer> scoreBInput = 
                new TextField<Integer>("scoreBInput", new Model<Integer>(bet.getScoreB()));
            scoreBInput.setType(Integer.class);
            item.add(scoreBInput);
            this.scoreBByMatchId.put(matchId, scoreBInput);

            
            
            final DropDownChoice<MatchWinner> matchWinnerInput = 
                new DropDownChoice<MatchWinner>("matchWinnerInput", new Model<MatchWinner>(bet.getMatchWinner()), Arrays.asList(MatchWinner.FINAL_MATCH_WINNERS));
            item.add(matchWinnerInput);
            this.matchWinnerByMatchId.put(matchId, matchWinnerInput);
            
            
            
//            switch (match.getBetType()) {
//                case RESULT_AND_SCORE :
//                    scoreAInput.setRequired(true);
//                    scoreBInput.setRequired(true);
//                    teamAInput.setVisible(false);
//                    teamBInput.setVisible(false);
//                    matchWinnerInput.setVisible(false);
//                    break;
//                case PRESENCE_IN_ROUND :
//                    teamAInput.setRequired(true);
//                    teamBInput.setRequired(true);
//                    teamA.setVisible(false);
//                    teamB.setVisible(false);
//                    scoreAInput.setVisible(false);
//                    scoreBInput.setVisible(false);
//                    matchWinnerInput.setVisible(false);
//                    break;
//                case FINAL :
//                    teamAInput.setRequired(true);
//                    teamBInput.setRequired(true);
//                    matchWinnerInput.setRequired(true);
//                    teamA.setVisible(false);
//                    teamB.setVisible(false);
//                    scoreAInput.setVisible(false);
//                    scoreBInput.setVisible(false);
//                    break;
//            }
            
        }
        
    }
    
    
}
