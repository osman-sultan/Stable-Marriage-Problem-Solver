import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SMPSolver {
    public static void main(String[] args) throws IOException {
        ArrayList<Student> S = new ArrayList<>(); // list of students
        ArrayList<School> H = new ArrayList<>(); // list of schools
        GaleShapley GSS = new GaleShapley(); // Student-opt SMP object
        GaleShapley GSH = new GaleShapley(); // School-opt SMP object
        GSH.setSuitorFirst(true); // identifier for schools as suitors

        String choice;
        int newStudents;
        int newSchools;
        boolean matchingOccurredGSS = false;
        boolean matchingOccurredGSH = false;

        // set of copies for student-opt
        ArrayList<Student> sSuitor;
        ArrayList<School> hReceiver;

        // set of copies for school-opt
        ArrayList<Student> sReceiver;
        ArrayList<School> hSuitor;

        do{
            displayMenu();
            choice = BasicFunctions.getString("\nEnter choice: ").toUpperCase();
            if(choice.equals("L")){
                newSchools = loadSchools(H); // load the receivers
                if(newSchools > 0){ // old students must be deleted as their preset ranking will become invalid due to incorrect size
                    S.clear();
                }
                newStudents = loadStudents(S, H); // load the suitors
                if(newStudents > 0 || newSchools > 0){ // clear old data and replace with copies of new data
                    for(Participant student: S){
                        student.clearMatches();
                    }

                    for(Participant school: H){
                        school.clearMatches();
                    }
                    sSuitor = copyStudents(S); //
                    hReceiver = copySchools(H);
                    GSS.clearMatches();
                    GSS.setParticipants(sSuitor, hReceiver);

                    sReceiver = copyStudents(S);
                    hSuitor = copySchools(H);
                    GSH.clearMatches();
                    GSH.setParticipants(hSuitor, sReceiver);
                }
            }
            else if(choice.equals("E")){
                System.out.println();

                // takes user to sub-menu
                editData(S, H);

                // transfer edits made to data to the copies; transfer any previous matches as well
                sSuitor = copyStudents(S);
                hReceiver = copySchools(H);
                copyMatches(sSuitor, hReceiver, GSS);
                GSS.setParticipants(sSuitor, hReceiver);

                sReceiver = copyStudents(S);
                hSuitor = copySchools(H);
                copyMatches(hSuitor, sReceiver, GSH);
                GSH.setParticipants(hSuitor, sReceiver);
            }
            else if(choice.equals("P")){
                System.out.println(); // various empty print statements and "\n" throughout program to produce appropriate spacing in output

                // students/schools need to exist in order to print them, if they don't print error
                if(S.size() == 0){
                    System.out.println("ERROR: No students are loaded!\n");
                }
                else{
                    System.out.println("STUDENTS:\n");
                    printStudents(S, H);
                    System.out.println();
                }
                if(H.size() == 0){
                    System.out.println("ERROR: No schools are loaded!\n");
                }
                else{
                    System.out.println("SCHOOLS:\n");
                    printSchools(S, H);
                    System.out.println();
                }
            }
            else if(choice.equals("M")){

                System.out.println("\nSTUDENT-OPTIMAL MATCHING\n");
                matchingOccurredGSS = GSS.match(); // determine if matching occurred or not
                copyMatches(S, H, GSS); // copy the student-opt matches into the main data sets to use for printing

                System.out.println("SCHOOL-OPTIMAL MATCHING\n");
                matchingOccurredGSH = GSH.match();
            }
            else if(choice.equals("D")){

                System.out.println("\nSTUDENT-OPTIMAL SOLUTION");
                if(!matchingOccurredGSS){ // check if matching happened, if it didn't print error otherwise print matching info
                    System.out.println("\nERROR: No matches exist!\n");
                }
                else{
                    System.out.println();
                    GSS.print();
                }

                System.out.println("SCHOOL-OPTIMAL SOLUTION");
                if(!matchingOccurredGSH){
                    System.out.println("\nERROR: No matches exist!\n");
                }
                else{
                    System.out.println();
                    GSH.print();
                }
            }
            else if(choice.equals("X")){
                if(matchingOccurredGSS && matchingOccurredGSH){
                    printComparison(GSS, GSH); // compare the 2 Gale-Shapley processes and determine the winners
                }
                else{
                    System.out.println("\nERROR: No matches exist!\n");
                }
            }
            else if(choice.equals("R")){
                // reinitialize everything to how it was at start of program
                S.clear();
                H.clear();
                GSS.reset();
                GSH.reset();
                matchingOccurredGSS = false;
                matchingOccurredGSH = false;
                System.out.println("\nDatabase cleared!\n");
            }
            else if(!choice.equals("Q")){
                System.out.println("\nERROR: Invalid menu choice!\n");
            }
        }while(!choice.equals("Q"));

        System.out.println("\nHasta luego!"); // See ya' later!
    }

    // display the menu
    public static void displayMenu(){

        // menu selection to display each iteration
        System.out.println("""
             JAVA STABLE MARRIAGE PROBLEM 
                
             L - Load students and schools from file
             E - Edit students and schools
             P - Print students and schools
             M - Match students and schools using Gale-Shapley algorithm
             D - Display matches
             X - Compare student-optimal and school-optimal matches
             R - Reset database
             Q - Quit""");
    }

    // load school info from file; return number of new schools added
    public static int loadSchools(ArrayList<School> H) throws IOException {
        boolean valid = false; // boolean to check if school info is valid
        String filename;
        String lineN;
        int added = 0; // amount of new schools added to the list
        int counter = 0; // amount of total potential schools that could be added
        BufferedReader fin; // initialize the file reader

        do{
            System.out.println();
            filename = BasicFunctions.getString("Enter school file name (0 to cancel): "); // get filename

            if(filename.equals("0")){
                System.out.println("\nFile loading process canceled.");
                return 0;
            }
            else if((new File(filename)).exists()){ // create a new file object to see if it exists
                valid = true;
            }
            else{
                System.out.println("\nERROR: File not found!"); // keep looping until user enters a valid file or quits
            }
        }while(!valid);

        fin = new BufferedReader(new FileReader(filename)); // set the file reader via the user inputted file
        do{
            lineN = fin.readLine(); // read the individual line
            if(lineN != null){ // only proceed if it contains information
                counter++; // add a potential school
                String[] schoolInfo = lineN.split(","); // split the info by the commas; data is formatted similar to a csv file
                String name = schoolInfo[0]; // get name
                double alpha = Double.parseDouble(schoolInfo[1]); // get GPA weight
                int maxMatches = Integer.parseInt(schoolInfo[2]); // get maximum amount of matches
                School newSchool = new School(name, alpha, maxMatches, 0); // create an object to for the potential school
                if(newSchool.isValid()){ // check if the schools' info is valid. if it is, add it to the list
                    H.add(newSchool);
                    added++; // increase added school counter by 1
                }
            }

        }while(lineN != null); // stop reading when the line is null aka empty

        fin.close(); // close the file

        System.out.println("\n" + added + " of " + counter + " schools loaded!"); // print how many schools were added out of the potential schools then return this value
        return added;
    }

    // load student info from file; return number of new students added   *process very similar to that of LoadSchools
    public static int loadStudents(ArrayList<Student> S, ArrayList<School> H) throws IOException{
        boolean valid = false;
        String fileName;
        String lineN;
        int added = 0;
        int counter = 0;
        BufferedReader fin;

        do{
            System.out.println();
            fileName = BasicFunctions.getString("Enter student file name (0 to cancel): ");
            if(fileName.equals("0")){
                System.out.println("\nFile loading process canceled.\n");
                return 0;
            }

            else if((new File(fileName)).exists()){
                valid = true;
            }
            else{
                System.out.println("\nERROR: File not found!");
            }
        }while(!valid);

        fin = new BufferedReader(new FileReader(fileName));
        do {
            lineN = fin.readLine();
            if(lineN != null){
                valid = true;
                counter++;
                String[] studentInfo = lineN.split(",");
                if(studentInfo.length - 3 == H.size()){ // for student's rankings need to be validated; first # of rankings are checked, if it is the same as amount of schools the check is passed.
                    String name = studentInfo[0];
                    double GPA = Double.parseDouble(studentInfo[1]);
                    int ES = Integer.parseInt(studentInfo[2]);
                    Student newStudent = new Student(name, GPA, ES, H.size());
                    for(int i = 3; i < studentInfo.length; i++){ // 2nd, each individual ranking must be checked. If a ranking is greater than amount of schools, then
                        int ind = Integer.parseInt(studentInfo[i]);
                        if(ind > H.size() || ind <= 0){
                            valid = false;
                            break;
                        }
                        newStudent.setRanking(i - 3, ind - 1);
                    }
                    if(newStudent.isValid() && valid){
                        S.add(newStudent);
                        added++;
                    }
                }
            }
        }while(lineN != null);
        fin.close();
        System.out.println("\n" + added + " of " + counter + " students loaded!\n");

        for(School school: H){ // when students are loaded calculate the schools ranking
            school.setNParticipants(S.size());
            school.calcRankings(S);
        }

        return added;
    }

    // sub-menu to edit existing student/school data
    public static void editData(ArrayList<Student> S, ArrayList<School> H){
        String choice;
        do{
            // menu to print with each iteration until user quits
            System.out.println("""
                 Edit data
                 ---------
                 S - Edit students
                 H - Edit high schools
                 Q - Quit
                 """);
            choice = BasicFunctions.getString("Enter choice: ").toUpperCase();
            System.out.println();

            // perform appropriate task based on user choice; error if invalid choice
            if(choice.equals("S")){

                // if condition not met print error and ask again
                if(S.size() == 0){
                    System.out.println("ERROR: No students are loaded!\n");
                    continue;
                }
                editStudents(S, H); // take user to student editing subsection
            }
            else if(choice.equals("H")){
                if(H.size() == 0){
                    System.out.println("ERROR: No schools are loaded!\n");
                    continue;
                }
                editSchools(S, H); // take user to school editing subsection
            }
            else if(!(choice.equals("Q"))){
                System.out.println("ERROR: Invalid menu choice!\n");
            }
        }while(!(choice.equals("Q")));
    }

    // sub-area to edit existing students; recalculates relevant values for schools if needed
    public static void editStudents(ArrayList<Student> S, ArrayList<School> H){
        int choice;
        do{
            // print selection of students in table form
            printStudents(S, H);
            choice = BasicFunctions.getInteger("Enter student (0 to quit): ", 0, S.size());

            // edit info of particular student
            if(choice != 0){
                System.out.println();
                S.get(choice - 1).editInfo(H, true); // based on if rankings have been set, user will be given additional option to edit rankings

                // must recalculate school rankings as well in the event student's composite score changes
                for (School school : H) {
                    school.calcRankings(S);
                }
            }
            System.out.println();
        }while(choice != 0);
    }

    // sub-area to edit existing schools
    public static void editSchools(ArrayList<Student> S, ArrayList<School> H){
        int choice;
        do{
            printSchools(S, H);
            choice = BasicFunctions.getInteger("Enter school (0 to quit): ", 0, H.size());
            if(choice != 0){
                System.out.println();
                H.get(choice - 1).editSchoolInfo(S, true); // only recalculate school rankings as student ranking is not dependent on school info
            }
            System.out.println();
        }while(choice != 0);
    }

    // print students with corresponding data in tabular format
    public static void printStudents(ArrayList<Student> S, ArrayList<School> H){
        System.out.format("%-5s%-40s%8s%4s  %-40s%-22s\n", " #", "Name", "GPA", "ES", "Assigned school", "Preferred school order"); // formatting for tabular form
        StringBuilder dashLine = new StringBuilder();
        dashLine.append("-".repeat(123)); // dash line for formatting
        System.out.println(dashLine);

        // print each student
        for(int i = 0; i < S.size(); i++) {
            System.out.format("%3d. ", (i + 1));
            S.get(i).print(H); // method to print each students' individual data
            System.out.println();
        }
        System.out.println(dashLine);
    }

    // print schools with corresponding data in tabular format
    public static void printSchools(ArrayList<Student> S, ArrayList<School> H){
        System.out.format("%-5s%-40s%8s%8s  %-40s%-23s\n", " #", "Name", "# spots", "Weight", "Assigned students", "Preferred student order");
        StringBuilder dashLine = new StringBuilder();
        dashLine.append("-".repeat(126));
        System.out.println(dashLine);

        for(int i = 0; i < H.size(); i++) {
            System.out.format("%3d. ", i + 1);
            H.get(i).print(S);
            System.out.println();
        }
        System.out.println(dashLine);
    }

    public static void printComparison(GaleShapley GSS, GaleShapley GSH){

        // determine winners of each category
        String stabilityW = "Tie";
        if(GSS.isStable() != GSH.isStable()){
            stabilityW = GSS.isStable() ? "Student-opt" : "School-opt";
        }

        String sRegretW = "Tie";
        if(GSS.getAvgSuitorRegret() != GSH.getAvgReceiverRegret()){
            sRegretW = GSS.getAvgSuitorRegret() < GSH.getAvgReceiverRegret() ? "Student-opt" : "School-opt";
        }

        String hRegretW = "Tie";
        if(GSS.getAvgReceiverRegret() != GSH.getAvgSuitorRegret()){
            hRegretW = GSS.getAvgReceiverRegret() < GSH.getAvgSuitorRegret() ? "Student-opt" : "School-opt";
        }

        String avgRegretW = "Tie";
        if(GSS.getAvgTotalRegret() != GSH.getAvgTotalRegret()){
            avgRegretW = GSS.getAvgTotalRegret() < GSH.getAvgTotalRegret() ? "Student-opt" : "School-opt";
        }

        String compTimeW = "Tie";
        if(GSS.getTime() != GSH.getTime()){
            compTimeW = GSS.getTime() < GSH.getTime() ? "Student-opt" : "School-opt";
        }

        // print stats and winners in tabular format
        System.out.format("\n%-17s%11s%21s%21s%21s%21s", "Solution", "Stable", "Avg school regret", "Avg student regret", "Avg total regret", "Comp time (ms)");
        StringBuilder dashLine = new StringBuilder();
        dashLine.append("-".repeat(112));
        System.out.println("\n" + dashLine);
        GSS.printStatsRow("Student optimal");
        System.out.println();
        GSH.printStatsRow("School optimal");
        System.out.println();
        System.out.println(dashLine);
        System.out.format("%-17s%11s%21s%21s%21s%21s", "WINNER", stabilityW, hRegretW, sRegretW, avgRegretW, compTimeW);
        System.out.println("\n" + dashLine + "\n");

    }

    // create independent copy of School ArrayList
    public static ArrayList<School> copySchools (ArrayList<School> P){
        ArrayList<School> newList = new ArrayList<>() ;
        for (School school : P) {
            School temp = new School();
            temp.setName(school.getName());
            temp.setAlpha(school.getAlpha());
            temp.setNParticipants(school.getNParticipants());
            temp.setMaxMatches(school.getMaxMatches());
            for (int j = 0; j < temp.getNParticipants(); j++) {
                temp.setRanking(j, school.getRanking(j));
            }
            newList.add(temp);
        }
        return newList;
    }

    // create independent copy of Student ArrayList
    public static ArrayList<Student> copyStudents (ArrayList<Student> P){
        ArrayList<Student> newList = new ArrayList<>() ;
        for (Student student : P) {
            Student temp = new Student();
            temp.setName(student.getName());
            temp.setGPA(student.getGPA());
            temp.setES(student.getES());
            temp.setNParticipants(student.getNParticipants());
            temp.setMaxMatches(student.getMaxMatches());
            for (int j = 0; j < temp.getNParticipants(); j++) {
                temp.setRanking(j, student.getRanking(j));
            }
            newList.add(temp);
        }
        return newList;
    }

    // transfer matches from SMP object to the 2 specified lists
    public static void copyMatches(ArrayList<? extends Participant> S, ArrayList<? extends Participant> R, GaleShapley SMP){
        for(int i = 0; i < SMP.getSuitors().size(); i++){
            S.get(i).clearMatches();
            for(int j = 0; j < SMP.getSuitors().get(i).getNMatches(); j++){
                S.get(i).setMatch(SMP.getSuitors().get(i).getMatch(j));
            }
        }

        for(int i = 0; i < SMP.getReceivers().size(); i++){
            R.get(i).clearMatches();
            for(int j = 0; j < SMP.getReceivers().get(i).getNMatches(); j++){
                R.get(i).setMatch(SMP.getReceivers().get(i).getMatch(j));
            }
        }
    }
}
