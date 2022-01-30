import java.io.*;
import java.util.*;

public class BasicFunctions {
    public static BufferedReader cin = new BufferedReader(new InputStreamReader(System.in)); //object used to get user input

    public static int getInteger(String prompt, int LB, int UB){
        int choice = 0; //input variable
        boolean bool;

        // get integer within bounds from user
        do{
            bool = true;
            System.out.print(prompt);

            try{
                choice = Integer.parseInt(cin.readLine());
            }
            catch (Exception e){
                bool = false;
            }

            if(bool && (choice < LB || choice > UB)){
                bool = false;
            }

            if (!bool) {
                if (LB == -(Integer.MAX_VALUE) && UB == Integer.MAX_VALUE) {
                    System.out.format("\nERROR: Input must be an integer in [-infinity, infinity]!\n\n");
                }
                else if (LB == -(Integer.MAX_VALUE)) {
                    System.out.format("\nERROR: Input must be an integer in [-infinity, %d]!\n\n", UB);
                }
                else if (UB == Integer.MAX_VALUE) {
                    System.out.format("\nERROR: Input must be an integer in [%d, infinity]!\n\n", LB);
                }
                else {
                    System.out.format("\nERROR: Input must be an integer in [%d, %d]!\n\n", LB, UB);
                }
            }

        }while(!bool);

        return choice; //return choice once a correct option is inputted
    }

    public static double getDouble(String prompt, double LB, double UB) {
        double choice = 0.0; //input variable
        boolean bool;

        // get double from user within bounds
        do {
            bool = true;
            System.out.print(prompt);

            try {
                choice = Double.parseDouble(cin.readLine());
            }
            catch(Exception e) {
                bool = false;
            }

            if(bool && (choice < LB || choice > UB)) {
                bool = false;
            }

            if(!bool) {
                if (LB == -(Double.MAX_VALUE) && UB == Double.MAX_VALUE) {
                    System.out.format("\nERROR: Input must be a real number in [-infinity, infinity]!\n\n");
                } else if (LB == -(Double.MAX_VALUE)) {
                    System.out.format("\nERROR: Input must be a real number in [-infinity, %.2f]!\n\n", UB);
                } else if (UB == Double.MAX_VALUE) {
                    System.out.format("\nERROR: Input must be a real number in [%.2f, infinity]!\n\n", LB);
                } else {
                    System.out.format("\nERROR: Input must be a real number in [%.2f, %.2f]!\n\n", LB, UB);
                }
            }

        }while(!bool);

        return choice; //return choice once a correct option is inputted
    }

    public static String getString(String prompt){
        String choice = "";
        System.out.print(prompt);

        // get string input from user
        try{
            choice = cin.readLine();
        }
        catch(Exception ignored){
        }

        return choice;
    }

    // checks if a value is already within an array
    public static <T> boolean checkDuplicate(List<T> arr){
        boolean duplicate = false;

        Set<T> set = new HashSet<>(arr);
        if(arr.size() > set.size()){
            duplicate = true;
        }

        return duplicate;
    }
}
