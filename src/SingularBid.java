import genius.core.Bid;

public class SingularBid {

    private Bid b;
    private double agent;
    private double opponent;


    public SingularBid(Bid bid, double agentUtility, double opponentUtility) {
        this.b = bid;
        this.agent = agentUtility;
        this.opponent = opponentUtility;
    }


    public boolean isGreater(SingularBid other) {
        if (other != this) {
            if ((other.getAgent() < getAgent()) || (other.getOpponent() < getOpponent())) {

                return false;
            } else if ((other.getAgent() > getAgent()) || (other.getOpponent() > getOpponent())) {
                return true;
            }
        }
        return false;
    }

    public double getProduct() {
        return opponent * agent;
    }

    public Bid getBid() {
        return b;
    }

    public double getAgent() {
        return agent;
    }

    public double getOpponent() {
        return opponent;
    }

    public void setOpponent(double utility) {
        this.opponent = utility;
    }
}