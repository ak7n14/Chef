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
            List<Issue> issueList = bid.getIssues();
            for(Issue issue:issueList){
                int issueNumber = issue.getNumber();
                ValueDiscrete val = (ValueDiscrete) bid.getValue(issueNumber);
                HashMap<ValueDiscrete, List<Integer>> valuePos = valsOfIssues.computeIfAbsent(issue, k -> new HashMap<>());
                List<Integer> positionList = valuePos.computeIfAbsent(val, k -> new ArrayList<>());
                positionList.add(p);
            }

            HashMap<Issue, HashMap<ValueDiscrete, Double>> means = new HashMap<>();
            HashMap<Issue, List<Double>> stddeviations = new HashMap<>();
            for(Issue issue: valsOfIssues.keySet()){
                HashMap<ValueDiscrete, List<Integer>> valuePos = valsOfIssues.get(issue);
                ArrayList<Double> stddevlist = new ArrayList<>();
                for(ValueDiscrete value : valuePos.keySet()){
                    List<Integer> positions  = valuePos.get(value);
                    double mean = positions.stream().mapToInt(e -> e).average().orElseThrow(() -> new RuntimeException("Value exists with no positions for it"));
                    double stddev = 0d;
                    for(int v : positions){
                        stddev += (v - mean) * (v - mean);
                    }
                    stddev /= (double) positions.size();


                    means.computeIfAbsent(issue, k -> new HashMap<>()).put(value, mean);

                    stddevlist.add(stddev);
                    this.setUtility(issue, value, mean);

                }
                Double maxStddev = stddevlist.stream().mapToDouble(e -> e).max().getAsDouble();
                getUtilitySpace().setWeight(issue, 1d / maxStddev);
            }
            this.normalizeWeightsByMaxValues();

        }

    }






}



