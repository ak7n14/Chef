import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

public class Opponent {
    private String id;
    private ArrayList<Bid> history;
    private ArrayList<Bid> acceptedBids;
    private HashMap<Issue, HashMap<Value, Integer>> frequencyTable;
    private HashMap<Issue, HashMap<Value, Double>> estimatedValue;
    private HashMap<Issue, Double> estimatedWeights;


    public Opponent(String id, Domain domain){
        this.id = id;
        history = new ArrayList<>();
        frequencyTable = new HashMap<>();
        acceptedBids = new ArrayList<>();
        estimatedValue = new HashMap<>();
        estimatedWeights = new HashMap<>();
    }


    public void addBidToHistory(Bid bid){
        history.add(bid);
        for(Issue i : bid.getIssues()) {
            Value v = bid.getValue(i.getNumber());
            if (frequencyTable.keySet().contains(i)) {
                if(frequencyTable.get(i).keySet().contains(v)) {
                    frequencyTable.get(i).put(v, frequencyTable.get(i).get(v) + 1);
                } else {
                    frequencyTable.get(i).put(v, 1);
                }
            } else {
                frequencyTable.put(i, new HashMap<>());
                frequencyTable.get(i).put(v, 1);
            }
        }

        updateModel();
    }

    public void  addToAcceptedHistory(Bid bid){
        acceptedBids.add(bid);
    }

    public double getEstimatedUtility(Bid bid) {
        double utility = 0;

        for(Issue i : bid.getIssues()) {
            double weight = (estimatedWeights.containsKey(i) ? estimatedWeights.get(i) : 0);

            double value = estimatedValue.get(i).containsKey(bid.getValue(i.getNumber()))
                    ? estimatedValue.get(i).get(bid.getValue(i.getNumber())) : 0;

            utility += weight * value;
        }

        return utility;
    }


    public void updateEstimatedValue(){
        for(Issue i : frequencyTable.keySet()) {
            HashMap<Value, Integer> sortedValues = sortValues(frequencyTable.get(i));
            HashMap<Value, Double> tempValueMap = new HashMap<>();
            int count = 0;
            for(Value v : sortedValues.keySet()) {
                tempValueMap.put(v, (sortedValues.size() - count) / (double) sortedValues.size());
            }
            estimatedValue.put(i, tempValueMap);
        }
    }

    public void updateEstimatedWeights(){
        for(Issue i : frequencyTable.keySet()) {
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

        for(Issue i : estimatedWeights.keySet()) {
            estimatedWeights.put(i, estimatedWeights.get(i) / total);
        }
    }

    public void updateModel() {
        updateEstimatedValue();
        updateEstimatedWeights();
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
