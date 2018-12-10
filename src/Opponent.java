import java.util.*;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;

public class Opponent {
    private String id;
    private ArrayList<Bid> history;
    private ArrayList<Bid> acceptedBids;
    private HashMap<Integer, HashMap<Value, Integer>> frequencyTable;
    private HashMap<Integer, HashMap<Value, Double>> estimatedValue;
    private HashMap<Integer, Double> estimatedWeights;
    private HashMap<Integer, Issue> issueList;


    public Opponent(String id, Domain domain){
        this.id = id;
        history = new ArrayList<>();
        frequencyTable = new HashMap<>();
        acceptedBids = new ArrayList<>();
        estimatedValue = new HashMap<>();
        estimatedWeights = new HashMap<>();
        issueList = new HashMap<>();
    }



    public void addBidToHistory(Bid bid){
        history.add(bid);
        for(Issue i : bid.getIssues()) {
            Integer id = i.getNumber();
            issueList.putIfAbsent(id, i);
            Value v = bid.getValue(id);

            frequencyTable.putIfAbsent(id, new HashMap<>());

            frequencyTable.get(id).putIfAbsent(v, 1);

            frequencyTable.get(id).put(v, frequencyTable.get(id).get(v) + 1);
        }

        updateModel();
    }

    public void  addToAcceptedHistory(Bid bid){
        acceptedBids.add(bid);
    }

    public double getEstimatedUtility(Bid bid) {
        double utility = 0;

        for(Issue i : bid.getIssues()) {

            double weight = 0;
            double value = 0;
            double k;

            Integer id = i.getNumber();

            if(estimatedWeights.containsKey(id)){
                k = ((IssueDiscrete) i).getNumberOfValues();
                weight = estimatedWeights.get(id);
                if(estimatedValue.get(id).containsKey(bid.getValue(id))) {
                    value = estimatedValue.get(id).get(bid.getValue(id));
                } else {
                    value = 1 / k;
                }
            }



            utility += weight * value;
        }

        return utility;
    }


    private void updateEstimatedValue(){
        for(Integer i : frequencyTable.keySet()) {
            HashMap<Value, Integer> sortedValues = sortValues(frequencyTable.get(i));
            HashMap<Value, Double> tempValueMap = new HashMap<>();
            int count = 0;
            double k = ((IssueDiscrete) issueList.get(i)).getNumberOfValues();


            for(Value v : sortedValues.keySet()) {
                tempValueMap.put(v, (sortedValues.size() - count) / k);
                count++;
            }
            estimatedValue.put(i, tempValueMap);
        }
    }

    private void updateEstimatedWeights(){
        for(Integer i : frequencyTable.keySet()) {
            double weight = 0;
            for(Integer val : frequencyTable.get(i).values()) {
                weight += (val*val) / (double) (history.size() * history.size());
            }

            estimatedWeights.put(i, weight);
        }

        normaliseWeights();
    }

    private void normaliseWeights() {
        double total = 0;

        for(double weight : estimatedWeights.values()) {
            total += weight;
        }

        for(Integer i : estimatedWeights.keySet()) {
            estimatedWeights.put(i, estimatedWeights.get(i) / total);
        }
    }

    private void updateModel() {
        updateEstimatedValue();
        updateEstimatedWeights();
    }

    public Bid getBestAcceptableBid(Bid[] bids) {
        //REPLACE
        double threshold = 0.5;

        Bid best = null;
        double bestUtility = Double.MAX_VALUE;
        for(Bid bid : bids) {
            double currentUtil = this.getEstimatedUtility(bid);
            if(currentUtil > threshold && currentUtil < bestUtility) {
                best = bid;
                bestUtility = currentUtil;
            }
        }

        return best;
    }

    private static HashMap<Value, Integer> sortValues(HashMap<Value, Integer> map) {
        List<Map.Entry<Value, Integer>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        HashMap<Value, Integer> temp = new LinkedHashMap<>();
        for(Map.Entry<Value, Integer> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }


}