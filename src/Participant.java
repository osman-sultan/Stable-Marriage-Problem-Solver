import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Participant {
    private String name; // name
    private int[] rankings; // rankings of participants
    private final ArrayList<Integer> matches = new ArrayList<>(); // match indices
    private int regret; // total regret
    private int maxMatches; // max # of allowed matches / openings

    // constructors
    public Participant(){
        setName("");
        setRegret(-1);
        setNParticipants(0);
    }

    public Participant(String name, int maxMatches, int nParticipants){
        setName(name);
        setMaxMatches(maxMatches);
        setNParticipants(nParticipants);
        setRegret(-1);
    }

    // getters
    public String getName(){
        return this.name;
    }

    public int getRanking(int i){
        return this.rankings[i];
    }

    public int getMatch(int i){
        return this.matches.get(i);
    }

    public int getRegret(){
        return this.regret;
    }

    public int getMaxMatches(){
        return this.maxMatches;
    }

    public int getNMatches(){
        return this.matches.size();
    }

    public int getNParticipants(){
        return this.rankings.length;
    }

    public boolean isFull(){
        return getMaxMatches() == getNMatches();
    }

    // setters
    public void setName(String name){
        this.name = name;
    }

    public void setRanking(int i, int r){
        this.rankings[i] = r;
    }

    public void setMatch(int m){
        this.matches.add(m);
        calcRegret();
    }

    public void setRegret(int r){
        this.regret = r;
    }

    // set rankings array size
    public void setNParticipants(int n){
        this.rankings = new int[n];
    }

    public void setMaxMatches(int n){
        this.maxMatches = n;
    }

    // methods to handle matches
    public void clearMatches(){ // clear all matches
        this.matches.clear();
        setRegret(-1);
    }

    // find school ranking based on school ID
    public int findRankingByID(int ind){
        int ranking = 0;
        for(int i = 0; i < this.rankings.length; i++){
            if(this.rankings[i] == ind - 1){
                ranking = i;
            }
        }
        return ranking;
    }

    // find the worst-matched participant
    public int getWorstMatch(){
        int regret = -1;
        int worstMatch = -1;
        for(int i = 0; i < getNMatches(); i++){
            if(regret < findRankingByID(getMatch(i)+ 1)){
                regret = findRankingByID(getMatch(i) + 1);
                worstMatch = getMatch(i);
            }
        }
        return worstMatch;
    }

    // remove the match with participant k
    public void unmatch(int k){
        for(int i = 0; i < getNMatches(); i++){
            if(getMatch(i) == k){
                this.matches.remove(i);
                break;
            }
        }
    }

    // check if match to participant k exists
    public boolean matchExists(int k){
        return this.matches.contains(k);
    }

    // get regret from match with k
    public int getSingleMatchedRegret(int k){
        return findRankingByID(k + 1);
    }

    // calculate total regret over all matches
    public void calcRegret(){
        int participantRegret = 0;
        for(int i = 0; i < getNMatches(); i++){
            participantRegret += getSingleMatchedRegret(getMatch(i));
        }
        setRegret(participantRegret);
    }

    // methods to edit data from the user
    public void editInfo(ArrayList <? extends Participant> P){
        // get student attributes
        String name = BasicFunctions.getString("Name: ");
        int maxMatches = BasicFunctions.getInteger("Maximum number of matches: ", 1, Integer.MAX_VALUE);
        setName(name);
        setMaxMatches(maxMatches);
        String choice;

        // if user wants to edit rankings, loop until correct input
        boolean canEditRankings;
        do{
            canEditRankings = true;
            choice = BasicFunctions.getString("Edit rankings (y/n): ").toUpperCase();
            if(choice.equals("Y")){
                setNParticipants(P.size());
                editRankings(P); //ask for rankings of student again
            }
            else if(!(choice.equals("N"))){
                System.out.println("ERROR: Choice must be 'y' or 'n'!");
                canEditRankings = false;
            }
        }while(!canEditRankings);
    }

    public void editRankings(ArrayList <? extends Participant> P){
        int r;
        Arrays.fill(this.rankings, -1); // fill array with arbitrary values to determine if certain school has been ranked yet

        System.out.println("\nParticipant " + this.name + "'s rankings:");

        // ask for rankings of school for particular student
        for(int i = 0; i < this.rankings.length; i++){
            boolean bool = true;
            do{
                r = BasicFunctions.getInteger("School " + P.get(i).getName() + ": ", 1, this.rankings.length) - 1;

                // checks if the ranking was already made; if it wasn't set it
                if(this.rankings[r] == -1){
                    this.setRanking(r, i);
                    bool = false;
                }
                else{
                    System.out.println("ERROR: Rank " + (r + 1) + " already used!\n");
                }
            }while(bool);
        }
        System.out.println();
    }

    // print methods
    public void print(ArrayList <? extends Participant> P){
        String assignedStudents = getNMatches() == 0 ? "-" : getMatchNames(P); // display match if it has been made
        System.out.format("%-40s%8d  %-40s", getName(), getMaxMatches(), assignedStudents); // display schools info
        if(getNParticipants() == 0){
            System.out.format("-");
        }
        else{
            printRankings(P); // display rankings if they have been made
        }
    }

    public void printRankings(ArrayList <? extends Participant> P){
        // checks if ranking of school is equivalent to current index; if so print it
        for(int i = 0; i < this.rankings.length; i++){
            if(i == 0){
                for(int j = 1; j <= this.rankings.length; j++){
                    if(findRankingByID(j) == i){
                        System.out.format("%s", P.get(j - 1).getName()); // slightly different formatting for 1st rank
                        break;
                    }
                }
            }
            else{
                for (int j = 1; j <= this.rankings.length; j++) {
                    if (this.findRankingByID(j) == i) {
                        System.out.format(", %s", P.get(j - 1).getName());
                        break;
                    }
                }
            }
        }

    }

    // return all the names of the matches for every participant
    public String getMatchNames (ArrayList <? extends Participant> P){
        StringBuilder matchNames = new StringBuilder();
        for(int match: this.matches){
            if(matchNames.toString().equals("")){
                matchNames.append(P.get(match).getName());
            }
            else{
                matchNames.append(", ").append(P.get(match).getName());
            }
        }
        return matchNames.toString();
    }

    // check if this participant has valid info
    public boolean isValid(){
        List<Integer> arr = Arrays.stream(this.rankings).boxed().toList();
        if(getMaxMatches() <= 0){
            return false;
        }
        else return !BasicFunctions.checkDuplicate(arr);
    }
}


