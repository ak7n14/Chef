import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
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
    private HashMap<Issue, HashMap<Value, Integer>> estimaedValue;
    private HashMap<Issue, HashMap<Value, Integer>> estimatedWeights;


    public Opponent(String id){
        this.id = id;
        history =new ArrayList<>();
        acceptedBids = new ArrayList<>();
        estimaedValue = new HashMap<>();
        estimatedWeights = new HashMap<>();
    }


    public void addBidToHistory(Bid bid){
        history.add(bid);
    }

    public void  addToAcceptedHistory(Bid bid){
        acceptedBids.add(bid);
    }


    public void updateEstimatedValue(){

    }

    public void updateEstimatedWeights(){

    }



}
