import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;


public class Agent28 extends AbstractNegotiationParty {
    private final String description = "Chef Agent";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;
    private AgentID lastAgent;

    private HashMap<AgentID, Opponent> opponents;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        opponents = new HashMap<>();
        utilitySpace = estimateUtilitySpace();
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        // According to Stacked Alternating Offers Protocol list includes
        // Accept, Offer and EndNegotiation actions only.
        double time = getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
        // The time is normalized, so agents need not be
        // concerned with the actual internal clock.


        double maxUtility = this.utilitySpace.getUtility(this.getMaxUtilityBid());
        double minUtility = this.utilitySpace.getUtility(this.getMinUtilityBid());

        double targetUtility = maxUtility - getDiscount(time, 0.1, 0.3) * (maxUtility - minUtility);



        //System.out.println(targetUtility);
        System.out.println(this.utilitySpace.getUtility(getMaxUtilityBid()));

        if(lastReceivedOffer != null && myLastOffer != null && this.utilitySpace.getUtility(lastReceivedOffer) >= targetUtility) {

            return new Accept(this.getPartyId(), lastReceivedOffer);
        } else {
            Bid[] bids = generateNBids(10, targetUtility);
            Bid bid = null;
            if(lastAgent != null)
                bid = opponents.get(lastAgent).getBestAcceptableBid(bids);


            if (bid != null) {
                System.out.println("Offering bid that opponent should accept");
                myLastOffer = bid;
            } else {
                myLastOffer = bids[0];
            }


            /*System.out.println("Predicted Utility"+this.utilitySpace.getUtility(myLastOffer));
            ExperimentalUserModel e = (ExperimentalUserModel) userModel;
            UncertainAdditiveUtilitySpace realUSpace = e.getRealUtilitySpace();
            System.out.println("Actual Utility"+realUSpace.getUtility(myLastOffer));*/

            return new Offer(this.getPartyId(), myLastOffer);
        }

    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();
            lastAgent = sender;

            if(opponents.containsKey(sender)) {
                opponents.get(sender).addBidToHistory(offer.getBid());
            } else {
                Opponent newOpp = new Opponent(sender.getName(), this.getDomain());
                newOpp.addBidToHistory(offer.getBid());
                opponents.put(sender, newOpp);
            }


        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bid getMinUtilityBid() {
        try {
            return this.utilitySpace.getMinUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bid generateBitWithMinUtility(double bidUtility) {
        Bid bid;
        double utility;

        do {
            bid = generateRandomBid();
            try {
                utility = utilitySpace.getUtility(bid);
            } catch (Exception e) {
                utility = 0.0;
            }
        }
        while(utility < bidUtility);
        return bid;
    }

    public Bid[] generateNBids(int n, double bidUtility) {
        Bid[] bids = new Bid[n];

        for(int i = 0; i < n; i++) {
            bids[i] = generateBitWithMinUtility(bidUtility);
        }

        return bids;
    }

    @Override
    public AbstractUtilitySpace estimateUtilitySpace(){
        Domain d = this.getDomain();
        UtilityEstimator est = new UtilityEstimator(d);
        BidRanking bid = userModel.getBidRanking();
        est.estimateUtility(bid);
        return est.getUtilitySpace();
    }

    private static double getDiscount(double t, double k, double beta) {
        return k + (1-k) * Math.pow(t, (1/beta));
    }

    private Bid getBidWithMostProduct(Bid[] bids) {
        double max = Double.MIN_VALUE;
        Bid maxBid = null;

        for(Bid bid : bids) {
            double product = 0;

            product = this.utilitySpace.getUtility(bid);

            for(Opponent opp : opponents.values()) {
                product *= opp.getEstimatedUtility(bid);
            }

            if(product > max) {
                max = product;
                maxBid = bid;
            }
        }

        return maxBid;
    }



}