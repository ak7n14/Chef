import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UtilityEstimator extends AdditiveUtilitySpaceFactory {

    private HashMap<Issue, HashMap<ValueDiscrete, List<Integer>>> valsOfIssues;
    public UtilityEstimator(Domain d) {
        super(d);
        valsOfIssues = new HashMap<>();
    }

    private void populateList(BidRanking bids) {
        int position = 1;

        for(Bid bid : bids.getBidOrder()) {
            for(Issue issue : bid.getIssues()) {
                int id = issue.getNumber();
                ValueDiscrete value = (ValueDiscrete) bid.getValue(id);
                if(!valsOfIssues.containsKey(issue)) {
                    valsOfIssues.put(issue, new HashMap<>());
                }

                if(!valsOfIssues.get(issue).containsKey(value)) {
                    valsOfIssues.get(issue).put(value, new ArrayList<>());
                }

                valsOfIssues.get(issue).get(value).add(position);
            }

            HashMap<Issue, HashMap<ValueDiscrete, Double>> mean = new HashMap<>();

            position++;
        }
    }

    public void estimateUtility(BidRanking bidRanking) {
        populateList(bidRanking);

        for(Issue issue : valsOfIssues.keySet()) {
            ArrayList<Double> stddevList = new ArrayList<>();
            for(ValueDiscrete value : valsOfIssues.get(issue).keySet()) {
                double mean = getMean(valsOfIssues.get(issue).get(value));
                double stddev = 0;

                for(int i : valsOfIssues.get(issue).get(value)) {
                    stddev += Math.pow((mean - i), 2);
                }

                stddev = stddev / (double) valsOfIssues.get(issue).get(value).size();

                stddevList.add(stddev);
                this.setUtility(issue, value, mean);
            }
            double maxDev = getMax(stddevList);
            getUtilitySpace().setWeight(issue, 1 / maxDev);
        }
        this.normalizeWeightsByMaxValues();
    }

    private double getMean(List<Integer> values) {
        double total = 0;
        for(Integer i : values) {
            total += i;
        }

        return total / (double) values.size();
    }

    private double getMax(List<Double> list) {
        double max = Double.MIN_VALUE;

        for(double d : list) {
            if(d > max)
                max = d;
        }

        return max;
    }
}



