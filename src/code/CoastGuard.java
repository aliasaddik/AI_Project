package code;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
    public static String solve(String grid,String strategy,boolean visualize){

        String [] gridSeparated = grid.split(";");
        // matrix ( 0-> empty // 1-> ship // 2-> station)
        String[] bounds = gridSeparated[0].split(",");
        int[][] matrix = new int[Integer.parseInt(bounds[0])][Integer.parseInt(bounds[1])];
        int spotsAvailable = Integer.parseInt(gridSeparated[1]);
        bounds = gridSeparated[2].split(",");
        int guardX =  Integer.parseInt(bounds[0]);
        int guardY =  Integer.parseInt(bounds[1]);
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
        System.out.println("initially peopleto rescue in solve "+peopleToRescue);
        State initState= new State(guardX,guardY,ships,spotsAvailable,peopleToRescue, ships.size(),0,0);
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
                if(nodeToExpand.state.guardX>0){
                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {

                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);
                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX-1, nodeToExpand.state.guardY, myships,
                            nodeToExpand.state.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued, nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP);
                    queue.offer(newNode);

                }
                if(nodeToExpand.state.guardX<matrix.length-1){
                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {

                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);
                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX+1, nodeToExpand.state.guardY, myships,
                            nodeToExpand.state.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued, nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    queue.offer(newNode);
                }
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {

                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);
                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX, nodeToExpand.state.guardY+1, myships,
                            nodeToExpand.state.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued, nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    queue.offer(newNode);
                }
                if(nodeToExpand.state.guardY>0){
                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {

                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);
                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX, nodeToExpand.state.guardY-1, myships,
                            nodeToExpand.state.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued, nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    queue.offer(newNode);

                }
                // drop_off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2&& nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    int peopleOnBoard = initState.spotsAvailable - nodeToExpand.state.spotsAvailable;
                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {

                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);
                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX, nodeToExpand.state.guardY, myships,
                             initState.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive-peopleOnBoard,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued+peopleOnBoard,
                            nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP);
                    queue.offer(newNode);
                }
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1){

                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {
                        String[] indices = key.split(",");
                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);

                        if (nodeToExpand.state.guardX == Integer.parseInt(indices[0])&&
                                nodeToExpand.state.guardX == Integer.parseInt(indices[1])&& newShip.aliveOnBoard==0 ){
                            newShip.counter=0;
                        }
                        else{

                        if(newShip.aliveOnBoard>0)
                            shipsWithPeopleAlive++;
                        newShip.Update();
                        if (newShip.counter>0)
                            myships.put(key,newShip);
                        else{
                            boxesLost++;
                        }
                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX, nodeToExpand.state.guardY, myships,
                            nodeToExpand.state.spotsAvailable,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive,
                            nodeToExpand.state.boxesToRetrieve-boxesLost-1, nodeToExpand.state.peopleRescued,
                            nodeToExpand.state.boxesRetrieved+1);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    queue.offer(newNode);
                }

                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1){

                    HashMap<String, Ship> myships = new HashMap<>();
                    int shipsWithPeopleAlive =0;
                    int boxesLost= 0;
                    int peopleRetrieved=0;
                    HashMap<String, Ship> prevShips = nodeToExpand.state.ships;
                    for (String key : prevShips.keySet()) {
                        String[] indices = key.split(",");
                        Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                                prevShips.get(key).counter);

                        if (nodeToExpand.state.guardX == Integer.parseInt(indices[0])&&
                                nodeToExpand.state.guardX == Integer.parseInt(indices[1])&& newShip.aliveOnBoard>0) {

                            newShip.aliveOnBoard-= nodeToExpand.state.spotsAvailable;
                            if (newShip.aliveOnBoard<0){
                                newShip.aliveOnBoard=0;
                            }
                            peopleRetrieved = prevShips.get(key).aliveOnBoard-newShip.aliveOnBoard;

                        }

                            if(newShip.aliveOnBoard>0)
                                shipsWithPeopleAlive++;
                            newShip.Update();
                            if (newShip.counter>0)
                                myships.put(key,newShip);
                            else{
                                boxesLost++;

                        }
                    }

                    State newState = new State(nodeToExpand.state.guardX, nodeToExpand.state.guardY, myships,
                            nodeToExpand.state.spotsAvailable-peopleRetrieved,nodeToExpand.state.peopleToRescue-shipsWithPeopleAlive ,
                            nodeToExpand.state.boxesToRetrieve-boxesLost, nodeToExpand.state.peopleRescued,
                            nodeToExpand.state.boxesRetrieved);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICK_UP);
                    queue.offer(newNode);
                }

            }
            else{
                //plan;deaths;retrieved;nodes
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                int nodes = 0;
                System.out.println("deaths = " +deaths+ "initialState people to rescue ="+ initState.peopleToRescue+ "final rescued ="+ nodeToExpand.state.peopleRescued);
                while(nodeToExpand.parent!=null){
                    solution.insert(0,nodeToExpand.operator+",");
                    nodes++;
                    nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+nodes);
                return String.valueOf(solution);

            }



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
        System.out.println( solve("5,6;50;0,1;0,4,3,3;1,1,90;","BF",false));
    }


    public static boolean goalTest(Object State) {
        State state = (State)State;
        if(state.peopleToRescue<=0 && state.boxesToRetrieve<=0)
        return true;
        else{
            return false;
        }
    }


    public static int pathCost(Object State) {
        return 0;
    }
}
