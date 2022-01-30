import java.util.ArrayList;

public class GaleShapley {
    public ArrayList <Participant> S = new ArrayList <>(); // suitors
    public ArrayList <Participant> R = new ArrayList <>(); // receivers
    private double avgSuitorRegret; // average suitor regret
    private double avgReceiverRegret; // average receiver regret
    private double avgTotalRegret; // average total regret
    private boolean matchesExist = false; // whether or not matches exist
    private boolean stable = false; // whether or not matching is stable
    private boolean suitorFirst = false; // whether to print suitor stats first
    private long compTime; // computation time
    private int receiverWorstRegret;

    public GaleShapley(){ // constructor
        this.avgSuitorRegret = 0.0D;
        this.avgReceiverRegret = 0.0D;
        this.avgTotalRegret = 0.0D;
    }

    // getters
    public double getAvgSuitorRegret(){
        return this.avgSuitorRegret;
    }

    public double getAvgReceiverRegret(){
        return this.avgReceiverRegret;
    }

    public double getAvgTotalRegret(){
        return this.avgTotalRegret;
    }

    public boolean matchesExist(){
        return this.matchesExist;
    }

    public boolean isStable(){
        return this.stable;
    }

    public long getTime(){
        return this.compTime;
    }

    // determine amount of matches still possible within suitors
    public int getNSuitorOpenings(){
        int suitorOpenings = 0;
        for(Participant suitor: this.S){
            suitorOpenings += suitor.getMaxMatches() - suitor.getNMatches();
        }
        return suitorOpenings;
    }

    public int getNReceiverOpenings(){
        int receiverOpenings = 0;
        for(Participant receiver: this.R){
            receiverOpenings += receiver.getMaxMatches() - receiver.getNMatches();
        }
        return receiverOpenings;
    }

    public ArrayList<? extends Participant> getSuitors(){
        return this.S;
    }

    public ArrayList<? extends Participant> getReceivers(){
        return this.R;
    }

    // new getter created for quiz; returns the regret of the receiver with the highest regret
    public int getWorstReceiverRegret(){
        return this.receiverWorstRegret;
    }

    // setters
    public void setMatchesExist(boolean b){
        this.matchesExist = b;
    }

    public void setSuitorFirst(boolean b){
        this.suitorFirst = b;
    }

    // fill in new suitor and receiver lists; clear previous actions
    public void setParticipants(ArrayList <? extends Participant> S, ArrayList <? extends Participant> R){
        this.S.clear();
        this.S.addAll(S);

        this.R.clear();
        this.R.addAll(R);

        if(matchesExist()){
            determineStability();
            calcRegrets();
        }
    }

    // methods for matching
    // clear out existing matches
    public void clearMatches(){
        setMatchesExist(false);
        this.stable = false;

        for(Participant suitor: this.S){
            suitor.clearMatches();
        }

        for(Participant receiver: this.R){
            receiver.clearMatches();
        }
    }

    // check that matching rules are satisfied
    public boolean matchingCanProceed(){
        if(this.S.size() == 0){
            System.out.println("\nERROR: No suitors are loaded!\n");
            return false;
        }
        else if(this.R.size() == 0){
            System.out.println("ERROR: No receivers are loaded!\n");
            return false;
        }
        else if(getNSuitorOpenings() != getNReceiverOpenings()){
            System.out.println("ERROR: The number of suitor and receiver openings must be equal!\n");
            return false;
        }

        // if all conditions pass, matching can proceed
        return true;
    }

    // Gale-Shapely algorithm to match
    public boolean match(){
        int receiver;

        if(matchingCanProceed()){
            clearMatches();
            int totalMatches = getNReceiverOpenings();
            long startTime = System.currentTimeMillis();
            while (getNSuitorOpenings() > 0){
                int i;
                for (i = 0; i < this.S.size(); i++){
                    if(!this.S.get(i).isFull())
                        break;
                }
                for(int j = 0; j < this.S.get(i).getNParticipants() && (!this.S.get(i).isFull()); j++){
                    receiver = this.S.get(i).getRanking(j);
                    if(makeProposal(i, receiver)){
                        makeEngagement(i, receiver, this.R.get(receiver).getWorstMatch());
                    }
                }
            }
            this.compTime = System.currentTimeMillis() - startTime;
            setMatchesExist(true);
            this.stable = determineStability();
            printStats();
            System.out.format("%d matches made in %dms!\n\n", totalMatches, getTime());
            determineWorstReceiverRegret();
        }
        else{
            setMatchesExist(false);
        }
        return matchesExist();
    }

    // suitor proposes; check if proposal is valid

    private boolean makeProposal(int suitor, int receiver){
        if(this.R.get(receiver).matchExists(suitor)){
            return false;
        }
        else if(!this.R.get(receiver).isFull()){
            return true;

        }
        else return this.R.get(receiver).findRankingByID(this.R.get(receiver).getWorstMatch() + 1) > this.R.get(receiver).findRankingByID(suitor + 1);
    }

    // make suitor-receiver engagement, break receiver-oldSuitor engagement if it exists
    private void makeEngagement(int suitor, int receiver, int oldSuitor){
        if(this.R.get(receiver).isFull()){
            this.S.get(oldSuitor).unmatch(receiver);
            this.R.get(receiver).unmatch(oldSuitor);
        }
        this.S.get(suitor).setMatch(receiver);
        this.R.get(receiver).setMatch(suitor);
    }

    // calculate regrets for entire dataset
    public void calcRegrets(){
        double totalSuitorRegret = 0;
        double totalReceiverRegret = 0;

        for (Participant suitor: this.S) {
            suitor.calcRegret();
            totalSuitorRegret += suitor.getRegret();
        }

        for (Participant receiver : this.R) {
            receiver.calcRegret();
            totalReceiverRegret += receiver.getRegret();
        }
        this.avgSuitorRegret = totalSuitorRegret / this.S.size();
        this.avgReceiverRegret = totalReceiverRegret / this.R.size();
        this.avgTotalRegret = (totalSuitorRegret + totalReceiverRegret) / (this.S.size() + this.R.size()); // divide by total amount of participants as suitors and receivers are not necessarily equal
    }

    // calculate if matching is stable
    public boolean determineStability(){
        for(int i = 0; i < this.S.size(); i++){
            for(int j = 0; j < this.S.get(i).getNMatches(); j++){
                int rankOfMatch = this.S.get(i).findRankingByID(this.S.get(i).getMatch(j) + 1);
                for(int k = 0; k < rankOfMatch; k++){
                    int receiver = this.S.get(i).getRanking(k);
                    if(!this.S.get(i).matchExists(receiver)){
                        int regretOfMatch = this.R.get(receiver).findRankingByID(this.R.get(receiver).getWorstMatch() + 1);
                        if(regretOfMatch > this.R.get(receiver).findRankingByID(i + 1)){
                            this.stable = false;
                            return false;
                        }
                    }
                }
            }
        }
        this.stable = true;
        return true;
    }

    // print methods
    // print the matching results and statistics
    public void print(){
        if(matchesExist())
            printMatches();

        printStats();
    }

    // print matches
    public void printMatches(){
        System.out.println("Matches:\n--------"); // display matches
        if(this.suitorFirst){
            for (Participant school : this.S) {
                System.out.format("%s: %s\n", school.getName(), school.getMatchNames(this.R));
            }
        }
        else{
            for (Participant school : this.R) {
                System.out.format("%s: %s\n", school.getName(), school.getMatchNames(this.S));
            }
        }
        System.out.println();
    }

    // print matching statistics
    public void printStats(){

        // display data after checking stability
        boolean stable = determineStability();
        if(stable){
            System.out.println("Stable matching? Yes");
        }
        else{
            System.out.println("Stable matching? No");
        }
        calcRegrets();
        System.out.format("Average suitor regret: %.2f\n", getAvgSuitorRegret());
        System.out.format("Average receiver regret: %.2f\n", getAvgReceiverRegret());
        System.out.format("Average total regret: %.2f\n", getAvgTotalRegret());
        System.out.println();
    }

    // print matching statistics in tabular format
    public void printStatsRow(String rowHeading){ // print stats as row
        String stability = isStable() ? "Yes" : "No";
        if(this.suitorFirst){
            System.out.format("%-17s%11s%21.2f%21.2f%21.2f%21d", rowHeading, stability, getAvgSuitorRegret(), getAvgReceiverRegret(), getAvgTotalRegret(), getTime());
        }
        else{
            System.out.format("%-17s%11s%21.2f%21.2f%21.2f%21d", rowHeading, stability, getAvgReceiverRegret(), getAvgSuitorRegret(), getAvgTotalRegret(), getTime());
        }
    }

    // reset everything
    public void reset(){
        this.S.clear();
        this.R.clear();
        this.avgSuitorRegret = 0.0D;
        this.avgReceiverRegret = 0.0D;
        this.avgTotalRegret = 0.0D;
        setMatchesExist(false);
        this.stable = false;
    }

    // new function for quiz; determines the worst regret out of all the receivers after matching has occurred.
    public void determineWorstReceiverRegret(){
        if(!this.matchesExist) return;
        int worstRegret = -1;
        for(Participant receiver: this.R){
            if(receiver.getRegret() > worstRegret){
                worstRegret = receiver.getRegret();
            }
        }
        this.receiverWorstRegret = worstRegret;
    }
}
