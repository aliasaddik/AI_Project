package code;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CoastGuard extends SearchProblem{
    public static int capacity;
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
        String[] bounds = gridSeparated[0].split(",");
        int[][] matrix = new int[Integer.parseInt(bounds[1])][Integer.parseInt(bounds[0])];
        System.out.println("my bounds are x= "+bounds[1]+" y = "+ bounds[0]);
        int spotsAvailable = Integer.parseInt(gridSeparated[1]);
        System.out.println("spots available on coast guard = "+ spotsAvailable);
        bounds = gridSeparated[2].split(",");
        int guardX =  Integer.parseInt(bounds[0]);
        int guardY =  Integer.parseInt(bounds[1]);
        System.out.println("my agent is at x= "+bounds[0]+" y = "+ bounds[1]);
        bounds = gridSeparated[3].split(",");
        ArrayList<Integer> stations = new ArrayList<>();
        for(int i = 0 ; i<bounds.length; i++) {
            int x = Integer.parseInt(bounds[i]);
            int y = Integer.parseInt(bounds[++i]);
            matrix[x][y]=2;
            stations.add(x);
            stations.add(y);
            System.out.println("station is at x = "+ x+" y = "+y);
        }
        bounds = gridSeparated[4].split(",");
        HashMap<String,Ship> ships = new HashMap<>();

        int peopleToRescue = 0;
        ArrayList<Integer> shipsLocations = new ArrayList<>();
        for(int i = 0 ; i<bounds.length; i++) {
            int x = Integer.parseInt(bounds[i]);
            int y = Integer.parseInt(bounds[++i]);
            matrix[x][y]=1;
            int capacity = Integer.parseInt(bounds[++i]);
            Ship ship = new Ship(capacity);
            ships.put(x+","+y,ship);
            shipsLocations.add(x);
            shipsLocations.add(y);
            peopleToRescue+=capacity;
            System.out.println("ship is at x = "+ x+" y = "+y+" with capacity "+ capacity);
        }

        System.out.println("initially people to rescue in solve "+peopleToRescue);
        State initState= new State(guardX,guardY,ships,spotsAvailable,peopleToRescue, ships.size(),0,0);


        switch(strategy) {
            case "BF":
              return BFS(matrix,initState,visualize);

            case "DF":
                return DFS(matrix,initState, Integer.MAX_VALUE);

            case "ID":
                return IDS(matrix,initState);

            case "GR1":
                return GR1(matrix,initState,calcMinDist(shipsLocations,stations),visualize);

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
    private static int calcMinDist(ArrayList<Integer> ships, ArrayList<Integer> stations){
        int min = Integer.MAX_VALUE;
        for(int i =0;i< ships.size();i+=2){
            for(int j=0;j<stations.size();j+=2){
                int xShip = ships.get(i);
                int yShip = ships.get(i+1);
                int xStation = stations.get(j);
                int yStation = stations.get(j+1);
                int distance = Math.abs(xShip-xStation)+Math.abs(yShip-yStation);
                min = distance<min?distance:min;
            }
        }
    return min;
    }
    private static int calcHeuristic1(int peopleToRescue, int capacity, int minDistance){
        return (int) (Math.ceil((double)peopleToRescue/(double)capacity)*(minDistance*2+2));
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

    private static String BFS(int[][] matrix, State initState,boolean visualize){
        HashSet<String> visitedStates = new HashSet<>();
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
                   String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("up");
                    }

                }

                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("down");
                    }

                }

                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("right");
                    }
                }
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("left");
                    }
                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("drop off");
                    }
                }

                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("retreived");
                    }

                }
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                        System.out.println("pickedUp");
                    }
                }
            }
            else{
                visualize(matrix,nodeToExpand);
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                int nodes = 0;
                System.out.println("deaths = " +deaths+ "initialState people to rescue ="+ initState.peopleToRescue+ "final rescued ="+ nodeToExpand.state.peopleRescued);
                while(nodeToExpand.parent!=null){

                    solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
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
    private static String DFS(int[][] matrix, State initState, int depth){
        HashSet<String> visitedStates = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        System.out.println("gaurdX: "+ initState.guardX+ " gaurdY: "+ initState.guardY+ " people to rescue: "+initState.peopleToRescue+
                "spots available on coast guard: "+initState.spotsAvailable+ " no of ships: "+initState.ships.size()+
                " people rescued: "+ initState.peopleRescued+ " boxes retreived: "+ initState.boxesRetrieved+
                " boxes to retrieve: "+ initState.boxesToRetrieve);

        Node initNode = new Node (initState,null,null);
        stack.push(initNode);
        while (!stack.isEmpty()) {
            Node nodeToExpand = stack.pop();
            System.out.println("people to rescue koll marra: "+nodeToExpand.state.peopleToRescue);
            System.out.println("initstate spots Available: "+ initState.spotsAvailable);
            System.out.println("current spots avialable: "+ nodeToExpand.state.spotsAvailable);
            if ((!goalTest(nodeToExpand.state))&& nodeToExpand.depth <= depth ){
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                        System.out.println("up");
                    }

                }

                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                        System.out.println("down");
                    }

                }

                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                        System.out.println("right");
                    }

                }
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                       stack.push(newNode);
                        System.out.println("left");
                    }


                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                        System.out.println("drop off");
                    }

                }

                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                       stack.push(newNode);
                        System.out.println("retreived");
                    }

                }
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                        System.out.println("pickedUp");
                    }

                }


            }
            else if (goalTest(nodeToExpand.state)){
                //plan;deaths;retrieved;nodes
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                int nodes = 0;
                System.out.println("deaths = " +deaths+ "initialState people to rescue ="+ initState.peopleToRescue+ "final rescued ="+ nodeToExpand.state.peopleRescued);
                while(nodeToExpand.parent!=null){

                    solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
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

    private static String IDS(int[][] matrix, State initState){

        for( int limit= 0; limit< Integer.MAX_VALUE; limit+=7){
            String answer = DFS(matrix,initState,limit);
            if (answer.length()!=0){
                System.out.println("ehna dakhlna el if elly fel for loop elly fe IDS");
                return answer;
            }
        }

        return "";
    }
    private static String GR1(int[][] matrix, State initState,int minDist,boolean visualize){
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        HashSet<String> visitedStates = new HashSet<>();
        System.out.println("gaurdX: "+ initState.guardX+ " gaurdY: "+ initState.guardY+ " people to rescue: "+initState.peopleToRescue+
                "spots available on coast guard: "+initState.spotsAvailable+ " no of ships: "+initState.ships.size()+
                " people rescued: "+ initState.peopleRescued+ " boxes retreived: "+ initState.boxesRetrieved+
                " boxes to retrieve: "+ initState.boxesToRetrieve);
        Node initNode = new Node (initState,null,null,calcHeuristic1(initState.peopleToRescue,initState.spotsAvailable,minDist));
        priorityQueue.offer(initNode);
        while (!priorityQueue.isEmpty()) {
            Node nodeToExpand = priorityQueue.poll();
            System.out.println("people to rescue koll marra: "+nodeToExpand.state.peopleToRescue);
            System.out.println("initstate spots Available: "+ initState.spotsAvailable);
            System.out.println("current spots avialable: "+ nodeToExpand.state.spotsAvailable);
            if (!goalTest(nodeToExpand.state)){
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("up");
                    }

                }

                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("down");
                    }

                }

                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("right");
                    }
                }
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("left");
                    }
                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("drop off");
                    }
                }

                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("retreived");
                    }

                }
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP,calcHeuristic1(newState.peopleToRescue, newState.spotsAvailable,minDist));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                        System.out.println("pickedUp");
                    }
                }
            }
            else{
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                int nodes = 0;
                System.out.println("deaths = " +deaths+ "initialState people to rescue ="+ initState.peopleToRescue+ "final rescued ="+ nodeToExpand.state.peopleRescued);
                while(nodeToExpand.parent!=null){

                    solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
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
        //test 2222
       String sol = solve("8,5;60;4,6;2,7;3,4,37,3,5,93,4,0,40;","BF", false);
       // System.out.println(sol);
       // visualize(new int[5][5]);

    }
    public static String  stringify (State myState){
        StringBuilder result =new StringBuilder(myState.peopleRescued+";"+ myState.peopleToRescue+";"+myState.boxesToRetrieve+";"+ myState.guardX+";"+
                myState.guardY+";"+ myState.spotsAvailable+";"+ myState.boxesRetrieved+";");
        HashMap<String, Ship> ships = myState.ships;
        for (String key : ships.keySet()) {
            result.append(key+";"+ships.get(key).aliveOnBoard+";"+ships.get(key).counter+";"+ships.get(key).dead+";");
        }

        return result.toString();

    }
    public static void visualize(int [][] grid, Node node){
        if(node.parent==null){
            printGrid(grid, node.state);
            System.out.println("");
            System.out.println("SPOTS AVAILABLE ON COST GUARD: " +node.state.spotsAvailable);
            return;
        }
        visualize(grid,node.parent);
        System.out.println("");
        printGrid(grid,node.state);
        System.out.println("");
        System.out.println("SPOTS AVAILABLE ON COST GUARD: " +node.state.spotsAvailable);

    }
    public static void  printGrid(int [][] grid, State state){
        for(int i=0;i<grid.length;i++){

            for(int j=0;j<grid[i].length;j++){ //prints row separation

                System.out.print("------");
            }
            System.out.println(" ");

            for(int j=0;j<grid[i].length;j++){ //prints what is present in this location if the grid
                if(j==0){
                    System.out.print("|");
                }
                if(state.guardY==j && state.guardX==i){ System.out.print("C");}
                else{System.out.print(" ");}

                if(grid[i][j]==0){
                    System.out.print("    |");
                }
                if(grid[i][j]==1){
                    Ship ship =state.ships.get(i+","+j);
                    if(ship!=null) {
                        int alive = ship.aliveOnBoard;
                        if (alive < 10) {
                            System.out.print(" S" + alive + " |");
                        } else {
                            System.out.print(" S" + alive + "|");
                        }
                    }else{
                        System.out.print(" S" + "X" + " |"); //this is a ship with the box retrieved
                    }
                }
                if(grid[i][j]==2){
                    System.out.print(" #  |");
                }
            }
            System.out.println("");
        }
        for(int j=0;j<grid[0].length;j++){ //prints last row outline
            System.out.print("------");
        }

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
