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
        System.out.println("my bounds are x= "+bounds[0]+" y = "+ bounds[1]);
        int spotsAvailable = Integer.parseInt(gridSeparated[1]);
        System.out.println("spots available on coast guard = "+ spotsAvailable);
        bounds = gridSeparated[2].split(",");
        int guardX =  Integer.parseInt(bounds[0]);
        int guardY =  Integer.parseInt(bounds[1]);
        System.out.println("my agent is at x= "+bounds[0]+" y = "+ bounds[1]);
        bounds = gridSeparated[3].split(",");
        for(int i = 0 ; i<bounds.length; i++) {
            int x = Integer.parseInt(bounds[i]);
            int y = Integer.parseInt(bounds[++i]);
            matrix[x][y]=2;
            System.out.println("station is at x = "+ x+" y = "+y);
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
            System.out.println("ship is at x = "+ x+" y = "+y+" with capacity "+ capacity);
        }
        System.out.println("initially people to rescue in solve "+peopleToRescue);
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
    private static State updateMoving(State nodeToExpandState,Operator operator){
        int shipsWithPeopleAlive = 0;
        int boxesLost = 0;
        HashMap<String, Ship> prevShips = nodeToExpandState.ships;

        HashMap<String, Ship> myShips = new HashMap<>();
        for (String key : prevShips.keySet()) {

            Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                    prevShips.get(key).counter);

            if(newShip.aliveOnBoard>0)
                shipsWithPeopleAlive++;

            newShip.Update();
            if (newShip.counter>0)
                myShips.put(key,newShip);
            else{
                boxesLost++;

            }

            System.out.println("alive on board koll marra :"+ newShip.aliveOnBoard);
        }
        int guardX = nodeToExpandState.guardX;
        int guardY = nodeToExpandState.guardY;
        System.out.println("ANA delwa2ty ha move we ana aslun fe x: "+ guardX +" y: "+ guardY);
        switch (operator){
            case UP: guardX--;break;
            case DOWN: guardX++; break;
            case RIGHT: guardY++; break;
            case LEFT: guardY--; break;

        }
        State newState = new State(guardX, guardY, myShips ,
                nodeToExpandState.spotsAvailable,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive,
                nodeToExpandState.boxesToRetrieve-boxesLost, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved);
        return newState;
    }
    private static State DropOff(State nodeToExpandState , State initState){
        int peopleOnBoard = initState.spotsAvailable - nodeToExpandState.spotsAvailable;
        HashMap<String, Ship> myships = new HashMap<>();
        int shipsWithPeopleAlive =0;
        int boxesLost= 0;
        HashMap<String, Ship> prevShips = nodeToExpandState.ships;
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

            System.out.println("alive on board koll marra :"+ newShip.aliveOnBoard);
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                initState.spotsAvailable,nodeToExpandState.peopleToRescue-(shipsWithPeopleAlive+peopleOnBoard),
                nodeToExpandState.boxesToRetrieve-boxesLost,
                nodeToExpandState.peopleRescued+peopleOnBoard,
                nodeToExpandState.boxesRetrieved);
        return newState;
    }
    private static State retrieve ( State nodeToExpandState){
        HashMap<String, Ship> myships = new HashMap<>();
        int shipsWithPeopleAlive = 0;
        int boxesLost = 0;
        HashMap<String, Ship> prevShips = nodeToExpandState.ships;
        for (String key : prevShips.keySet()) {
            String[] indices = key.split(",");
            Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                    prevShips.get(key).counter);

            if (nodeToExpandState.guardX == Integer.parseInt(indices[0]) &&
                    nodeToExpandState.guardY == Integer.parseInt(indices[1]) && newShip.aliveOnBoard<=0){
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
            System.out.println("alive on board koll marra :"+ newShip.aliveOnBoard);
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                nodeToExpandState.spotsAvailable,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive,
                nodeToExpandState.boxesToRetrieve-boxesLost-1, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved+1);
        return newState;
    }
    public static State pickUp(State nodeToExpandState){
        HashMap<String, Ship> myships = new HashMap<>();
        int shipsWithPeopleAlive =0;
        int boxesLost= 0;
        int peopleRetrieved=0;
        HashMap<String, Ship> prevShips = nodeToExpandState.ships;
        for (String key : prevShips.keySet()) {
            String[] indices = key.split(",");
            Ship newShip = new Ship(prevShips.get(key).aliveOnBoard, prevShips.get(key).dead,
                    prevShips.get(key).counter);

            if (nodeToExpandState.guardX == Integer.parseInt(indices[0])&&
                    nodeToExpandState.guardY == Integer.parseInt(indices[1])&& newShip.aliveOnBoard>0) {

                newShip.aliveOnBoard-= nodeToExpandState.spotsAvailable;
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
            System.out.println("alive on board koll marra :"+ newShip.aliveOnBoard);
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                nodeToExpandState.spotsAvailable-peopleRetrieved,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive ,
                nodeToExpandState.boxesToRetrieve-boxesLost, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved);
        return newState;


    }

    private static String BFS(int[][] matrix, State initState){
        Queue<Node> queue = new LinkedList<>();
        System.out.println("gaurdX: "+ initState.guardX+ " gaurdY: "+ initState.guardY+ " people to rescue: "+initState.peopleToRescue+
                "spots available on coast guard: "+initState.spotsAvailable+ " no of ships: "+initState.ships.size()+
                " people rescued: "+ initState.peopleRescued+ " boxes retreived: "+ initState.boxesRetrieved+
                " boxes to retrieve: "+ initState.boxesToRetrieve);

        Node initNode = new Node (initState,null,null);
        queue.offer(initNode);
        while (!queue.isEmpty()) {
            Node nodeToExpand = queue.poll();
            System.out.println("people to rescue koll marra: "+nodeToExpand.state.peopleToRescue);
            System.out.println("initstate spots Available: "+ initState.spotsAvailable);
            System.out.println("current spots avialable: "+ nodeToExpand.state.spotsAvailable);
            if (!goalTest(nodeToExpand.state)){
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP);
                    queue.offer(newNode);
                    System.out.println("up");
                }

                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    queue.offer(newNode);
                    System.out.println("down");
                }

                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    queue.offer(newNode);
                    System.out.println("right");
                }
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    queue.offer(newNode);
                    System.out.println("left");

                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP);
                    queue.offer(newNode);

                    System.out.println("drop off");
                    System.out.println("parent Node Info :");
                    System.out.println("gaurdX: "+ nodeToExpand.state.guardX+ " gaurdY: "+ nodeToExpand.state.guardY+ " people to rescue: "
                            +nodeToExpand.state.peopleToRescue+ "spots available on coast guard: "+nodeToExpand.state.spotsAvailable+
                            " no of ships: "+nodeToExpand.state.ships.size()+
                            " people rescued: "+ nodeToExpand.state.peopleRescued+ " boxes retreived: "+ nodeToExpand.state.boxesRetrieved+
                            " boxes to retrieve: "+ nodeToExpand.state.boxesToRetrieve);
                    System.out.println("new Node Info :");
                    System.out.println("gaurdX: "+ newState.guardX+ " gaurdY: "+ newState.guardY+ " people to rescue: "+newState.peopleToRescue+
                            "spots available on coast guard: "+newState.spotsAvailable+ " no of ships: "+newState.ships.size()+
                            " people rescued: "+ newState.peopleRescued+ " boxes retreived: "+ newState.boxesRetrieved+
                            " boxes to retrieve: "+ newState.boxesToRetrieve);
                }

                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    queue.offer(newNode);
                    System.out.println("retreived");
                }
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICK_UP);
                    queue.offer(newNode);
                    System.out.println("PickedUp");
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
