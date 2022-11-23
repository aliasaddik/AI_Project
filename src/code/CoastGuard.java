package code;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class CoastGuard extends SearchProblem{

    public static String genGrid(){
        Random rand = new Random();
        StringBuilder grid = new StringBuilder();
        int m = rand.nextInt(11)+5;
        int n = rand.nextInt(11)+5;
        grid.append(m+","+n+";");
        boolean[][] matrix = new boolean[m][n];
        int x, y;
        grid.append((rand.nextInt(71)+30)+";");
        x= rand.nextInt(m);
        y = rand.nextInt(n);
        grid.append(x+","+y+";");
        matrix[x][y]= true;
        int shipNo = rand.nextInt((m*n-2))+1;
        int statNo = rand.nextInt((m*n)-shipNo-1)+1;
        for (int i =0; i<statNo;i++){
         while(true){
             x= rand.nextInt(m);
             y= rand.nextInt(n);
             if ( !matrix[x][y])
                 break;
         }
            matrix[x][y]=true;
            grid.append(x+","+y+",");

        }
        grid.replace(grid.length()-1, grid.length(),";");
        for (int i =0; i<shipNo;i++){
            while(true){
                x= rand.nextInt(m);
                y= rand.nextInt(n);
                if ( !matrix[x][y])
                    break;
            }
            matrix[x][y]=true;
            grid.append(x+","+y+",");
            grid.append(rand.nextInt(100)+1+",");

        }
        grid.replace(grid.length()-1, grid.length(),";");
        return grid.toString();

    }
    public static String Solve(String grid,String strategy,boolean visualize){
        State initState= new State();

        String [] gridSeparated = grid.split(";");
        // matrix ( 0-> empty // 1-> ship // 2-> station)
        String[] bounds = gridSeparated[0].split(",");
        int[][] matrix = new int[Integer.parseInt(bounds[0])][Integer.parseInt(bounds[1])];
        initState.spotsAvailable = Integer.parseInt(gridSeparated[1]);
        bounds = gridSeparated[2].split(",");
        initState.guardX =  Integer.parseInt(bounds[0]);
        initState.guardY =  Integer.parseInt(bounds[1]);
        bounds = gridSeparated[3].split(",");
        for(int i = 0 ; i<bounds.length; i++) {
            int x = Integer.parseInt(bounds[i]);
            int y = Integer.parseInt(bounds[++i]);
            matrix[x][y]=2;
        }
        bounds = gridSeparated[4].split(",");
        HashMap<String,Ship> ships = new HashMap<>();
        int peopleToRescue = 0;
        for(int i = 0 ; i<bounds.length; i++) {
            int x = Integer.parseInt(bounds[i]);
            int y = Integer.parseInt(bounds[++i]);
            matrix[x][y]=1;
            int capacity = Integer.parseInt(bounds[++i]);
            Ship ship = new Ship(capacity);
            ships.put(x+","+y,ship);
            peopleToRescue+=capacity;
        }
        initState.ships = ships;
        initState.peopleToRescue = peopleToRescue;

        switch(strategy) {
            case "BF":
              return BFS(matrix,initState);

            case "DF":
                return DFS(matrix,initState);

            case "ID":
                return IDS(matrix,initState);

            case "GR1":
                return GR1(matrix,initState);

            case "GR2":
                return GR2(matrix,initState);

            case "AS1":
                return AS1(matrix,initState);

            case "AS2":
                return AS2(matrix,initState);

            default:
               return "";
        }
    }
    private static String BFS(int[][] matrix, State initState){
        Queue<Node> queue = new LinkedList<>();
        Node initNode = new Node (initState,null,null);
        queue.offer(initNode);
        while (!queue.isEmpty()) {
            Node nodeToExpand = queue.poll();
            if (!goalTest(nodeToExpand.state)){
                if(nodeToExpand.state.guardY>0){
                    //HashMap<String, Ship> myships = nodeToExpand.state.ships;
                //State expandUpState = new State();
                //Node expandUpState = new Node()
                }
            }
            //apply 6 operators to create 6 nodes
            //add to queue


        }
        return "";
    }
    private static String DFS(int[][] matrix, State initState){

        return "";
    }
    private static String IDS(int[][] matrix, State initState){

        return "";
    }
    private static String GR1(int[][] matrix, State initState){

        return "";
    }
    private static String GR2(int[][] matrix, State initState){

        return "";
    }
    private static String AS1(int[][] matrix, State initState){

        return "";
    }
    private static String AS2(int[][] matrix, State initState){

        return "";
    }
    public static void main(String[] args) {
        CoastGuard cs = new CoastGuard();
        System.out.println(cs.genGrid());
    }


    public static boolean goalTest(Object State) {
        State state = (State)State;
        if(state.peopleToRescue==0 && state.boxesToRetrieve==0)
        return true;
        else{
            return false;
        }
    }


    public static int pathCost(Object State) {
        return 0;
    }
}
