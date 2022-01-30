import java.util.ArrayList;

public class School extends Participant{
    private double alpha; // GPA weight

    // constructors
    public School(){
        super();
        setMaxMatches(1);
    }

    public School(String name, double alpha, int maxMatches, int nStudents){
        super(name, maxMatches, nStudents);
        setAlpha(alpha);
    }

    // getters and setters
    public double getAlpha(){
        return this.alpha;
    }

    public void setAlpha(double alpha){
        this.alpha = alpha;
    }

    // get new info from the user; cannot be inherited or overridden from parent
    public void editSchoolInfo(ArrayList <Student> S, boolean canEditRankings){
        String name = BasicFunctions.getString("Name: ");
        double alpha = BasicFunctions.getDouble("GPA weight: ", 0.00, 1.00);
        int maxMatches = BasicFunctions.getInteger("Maximum number of matches: ", 1, Integer.MAX_VALUE);
        setName(name);
        setAlpha(alpha);
        setMaxMatches(maxMatches);

        if(canEditRankings && S.size() != 0){
            this.calcRankings(S);
        }
    }

    // calculate rankings based on weight alpha
    public void calcRankings(ArrayList<Student> S){
        double[][] calcScore = new double[getNParticipants()][2]; // 2d array to store composite score with corresponding index of student
        double[] temp; // temp variable for comparison

        // calculate and store scores of each student
        for(int i = 0; i < getNParticipants(); i++){
            calcScore[i][0] = i;
            calcScore[i][1] = (this.getAlpha() * S.get(i).getGPA()) + ((1 - this.getAlpha()) * (double)S.get(i).getES());
        }

        // sort the array in descending order
        for(int i = 0; i < getNParticipants(); i++){
            for(int j = 0; j < getNParticipants(); j++){
                // first sort by composite score
                if(calcScore[i][1] < calcScore[j][1] && i < j){
                    temp = new double[] {calcScore[i][0], calcScore[i][1]};
                    calcScore[i][0] = calcScore[j][0];
                    calcScore[i][1] = calcScore[j][1];
                    calcScore[j][0] = temp[0];
                    calcScore[j][1] = temp[1];
                }
                // then sort by index (lower index ranked higher)
                else if(calcScore[i][1] == calcScore[j][1] && calcScore[i][0] > calcScore[j][0] && i < j){
                    temp = new double[] {calcScore[i][0], calcScore[i][1]};
                    calcScore[i][0] = calcScore[j][0];
                    calcScore[i][1] = calcScore[j][1];
                    calcScore[j][0] = temp[0];
                    calcScore[j][1] = temp[1];
                }
            }
        }

        // assign rankings based on sorted array
        for(int i = 0; i < getNParticipants(); i++){
            this.setRanking(i, (int)(calcScore[i][0]));
        }
    }

    public void print(ArrayList <? extends Participant> S){ // print school row
        String assignedStudents = getNMatches() == 0 ? "-" : getMatchNames(S); // display match if it has been made
        System.out.format("%-40s%8d%8.2f  %-40s", getName(), getMaxMatches(), getAlpha(), assignedStudents); // display schools info
        if(getNParticipants() == 0){
            System.out.format("-");
        }
        else{
            printRankings(S); // display rankings if they have been made
        }
    }

    public boolean isValid(){ // check if this school has valid info
        if(!super.isValid()){
            return false;
        }
        else{
            return ((this.alpha >= 0.0D) && (this.alpha <= 1.0D));
        }
    }
}
