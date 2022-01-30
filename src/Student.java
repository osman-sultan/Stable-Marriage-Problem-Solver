import java.util.ArrayList;

public class Student extends Participant{
    private double GPA;
    private int ES;

    // constructors
    public Student(){
        super();
        setMaxMatches(1);
    }

    public Student(String name, double GPA, int ES, int nSchools){
        super(name, 1, nSchools);
        setGPA(GPA);
        setES(ES);
    }

    // getters and setters
    public double getGPA(){
        return this.GPA;
    }

    public int getES(){
        return this.ES;
    }

    public void setGPA(double GPA){
        this.GPA = GPA;
    }

    public void setES(int ES){
        this.ES = ES;
    }

    public void editInfo (ArrayList <School> H, boolean canEditRankings){ // user info
        // get student attributes
        String name = BasicFunctions.getString("Name: ");
        double GPA = BasicFunctions.getDouble("GPA: ", 0.00, 4.00);
        int ES = BasicFunctions.getInteger("Extracurricular score: ", 0, 5);
        int maxMatches = BasicFunctions.getInteger("Maximum number of matches: ", 1, Integer.MAX_VALUE);
        setName(name);
        setGPA(GPA);
        setES(ES);
        setMaxMatches(maxMatches);

        String choice;
        // if user wants to edit rankings, loop until correct input
        do{
            choice = BasicFunctions.getString("Edit rankings (y/n): ").toUpperCase();
            if(choice.equals("Y")){
                setNParticipants(H.size());
                editRankings(H); //ask for rankings of student again
                canEditRankings = true;
            }
            else if(!(choice.equals("N"))){
                System.out.println("ERROR: Choice must be 'y' or 'n'!");
                canEditRankings = false;
            }
        }while(!canEditRankings);
    }

    public void print(ArrayList<? extends Participant> H){ // print student row
        String assignedSchools = getNMatches() == 0 ? "-" : getMatchNames(H); // checks if match has been made; display match if it has
        System.out.format("%-40s%8.2f%4d  %-40s", getName(), getGPA(), getES(), assignedSchools); // display students info with appropriate formatting

        // display students rankings if they have been made
        if(getNParticipants() == 0){
            System.out.format("-");
        }
        else{
            printRankings(H); // function to display rankings
        }
    }

    public boolean isValid(){ // check if the student has valid info
        if(this.GPA > 4.0D || this.GPA < 0.0D) return false;
        if(this.ES > 5 || this.ES < 0) return false;
        return super.isValid();
    }
}
