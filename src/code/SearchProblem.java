package code;
//parent class of search problems
public class SearchProblem {
    Enum operators;
    Object initState;


    public  static boolean goalTest(Object State){
        return false;
    }
    public static int pathCost(Object State){
        return 0;
    }

}
