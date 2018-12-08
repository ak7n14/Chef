import genius.core.Domain;
import genius.core.utility.UtilitySpace;
import genius.core.BidIterator;
import genius.core.Bid;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;


public class CalcNash {

    private Domain domain;
    private UtilitySpace agentSpace;
    private Opponent opponent;
    private List<SingularBid> space;
    private List<SingularBid> pareto;
    private SingularBid nashBid;
    private int iterations ;//
    private boolean spaceUpdated;



    public CalcNash(Domain domain, UtilitySpace agentSpace, Opponent opponent) {
        this.domain = domain;
        this.agentSpace = agentSpace;
        this.opponent = opponent;
        pareto = new ArrayList<SingularBid>();
        iterations = 25000;
    }


    private void createBidSpace() {
        space = new ArrayList<SingularBid>();
        BidIterator it = new BidIterator(domain);
        int iter = 0;

        while (it.hasNext() && iter < iterations) {
            Bid bid = it.next();
            space.add(new SingularBid(bid, agentSpace.getUtility(bid), opponent.getEstimatedUtility(bid)));
            iterations++;
        }
        spaceUpdated = true;
    }

    public void updateBidSpace(Opponent opponent) {
        this.opponent = opponent;
        if (space == null) {
            createBidSpace();
        } else {
            for (SingularBid bp : space) {
                bp.setOpponent(opponent.getEstimatedUtility(bp.getBid()));
            }
        }
        spaceUpdated = true;
    }


    private void addFrontier(SingularBid b) {
        for (SingularBid front : pareto) {

            if (b.isGreater(front)) {
                return;
            }

            if (front.isGreater(b)) {
                removeDominance(b, front);
                return;
            }
        }

        pareto.add(b);
    }

    private void removeDominance(SingularBid bidToAdd, SingularBid bidToRemove) {

        List<SingularBid> frontierToRemove = new ArrayList<SingularBid>();
        pareto.remove(bidToRemove);

        for (SingularBid front : pareto) {
            if (front.isGreater(bidToAdd)) {
                frontierToRemove.add(front);
            }
        }
        pareto.removeAll(frontierToRemove);
        pareto.add(bidToAdd);
    }

    public Bid getNashPoint(){

        if (spaceUpdated || nashBid == null) {
            spaceUpdated = false;


            for (SingularBid b : space) {
                addFrontier(b);
            }
            double maxProduct = -1;
            double currentProduct = 0;
            // Loop through pareto frontier and find Nash point
            for (SingularBid b : pareto) {
                currentProduct = b.getProduct();
                if (currentProduct > maxProduct) {
                    nashBid = b;
                    maxProduct = currentProduct;
                }
            }
        }

        if (nashBid == null) {
            System.out.println("Nash is null");
        }

        return nashBid == null ? null : nashBid.getBid();
    }



    public double nashDist(Bid bid) {
        double dist=-1;
        if (nashBid != null) {
            double agentDiff = nashBid.getAgent() - agentSpace.getUtility(bid);
            double opponentDiff = nashBid.getOpponent() - opponent.getEstimatedUtility(bid);

            dist = Math.sqrt(((Math.pow(agentDiff, 2)) + (Math.pow(opponentDiff, 2))));
        }
        return dist;
    }

    public double getNashUtility() {
        return nashBid.getAgent();
    }

}
