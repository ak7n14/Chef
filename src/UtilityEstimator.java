import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UtilityEstimator extends AdditiveUtilitySpaceFactory {

    private Domain domain;
    HashMap<Issue, HashMap<ValueDiscrete, List<Integer>>> valsOfIssues;
    public UtilityEstimator(Domain d) {
        super(d);
        domain = d;
        valsOfIssues = new HashMap<>();
    }

    public void populateValsIssues(BidRanking bidsRankingList){

        int p=0;
        for(Bid bid : bidsRankingList.getBidOrder()) {

            p+=1;
            List<Issue> isssuList = bid.getIssues();
            for(Issue issue:isssuList){
                int issueNumber = issue.getNumber();
                ValueDiscrete val = (ValueDiscrete) bid.getValue(issueNumber);
                HashMap<ValueDiscrete, List<Integer>> valuePos = valsOfIssues.computeIfAbsent(issue, k -> new HashMap<>());
                List<Integer> positionList = valuePos.computeIfAbsent(val, k -> new ArrayList<>());
                positionList.add(p);
            }
        }

    }


    public void estimateUtility(BidRanking bids){

        populateValsIssues(bids);
        HashMap<Issue, HashMap<ValueDiscrete, Double>> means = new HashMap<>();
        HashMap<Issue, List<Double>> stddevs = new HashMap<>();


    }





}



