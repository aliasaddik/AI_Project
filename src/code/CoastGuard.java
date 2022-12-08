package code;
import java.util.*;


public class CoastGuard extends SearchProblem{
  // a function that generate a random grid.
    public static String genGrid(){
        Random rand = new Random();
        StringBuilder grid = new StringBuilder();
        int m = rand.nextInt(11)+5;
        int n = rand.nextInt(11)+5;
        grid.append(m+","+n+";");
        boolean[][] matrix = new boolean[n][m];
        int x, y;
        grid.append((rand.nextInt(71)+30)+";");
        x= rand.nextInt(n);
        y = rand.nextInt(m);
        grid.append(x+","+y+";");
        matrix[x][y]= true;
        int shipNo = rand.nextInt((m*n-2))+1;
        int statNo = rand.nextInt((m*n)-shipNo-1)+1;
        for (int i =0; i<statNo;i++){
         while(true){
             x= rand.nextInt(n);
             y= rand.nextInt(m);
             if ( !matrix[x][y])
                 break;
         }
            matrix[x][y]=true;
            grid.append(x+","+y+",");

        }
        grid.replace(grid.length()-1, grid.length(),";");
        for (int i =0; i<shipNo;i++){
            while(true){
                x= rand.nextInt(n);
                y= rand.nextInt(m);
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
    //solve function :  first reads the grid then inputs it's data into a matrix that indicates
    // where are the stations and ships and initiate the state of the initial state
    // then it calls the designated function for the search strategy indicated in the function
    public static String solve(String grid,String strategy,boolean visualize){
        String [] gridSeparated = grid.split(";");
        String[] bounds = gridSeparated[0].split(",");
        int[][] matrix = new int[Integer.parseInt(bounds[1])][Integer.parseInt(bounds[0])];
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

         State initState= new State(guardX,guardY,ships,spotsAvailable,peopleToRescue, ships.size(),0,0, ships.size());

        switch(strategy) {
            case "BF":
              return BFS(matrix,initState, visualize);

            case "DF":
                return DFS(matrix,initState, Integer.MAX_VALUE, visualize);

            case "ID":
                return IDS(matrix,initState, visualize);

            case "GR1":
                return GR1(matrix,initState, visualize);

            case "GR2":
                return GR2(matrix,initState, visualize);

            case "AS1":
                return AS1(matrix,initState, visualize);

            case "AS2":
                return AS2(matrix,initState, visualize);

            default:
               return "";
        }
    }

    // a function that is used as a helper method to the heuristic function to calculate the minimum distance
    // between the coast guard and the closest ship that is still not wrecked
    // and also returns the number of people on the closest ship
    private static int[] minDistGuardShip ( HashMap<String,Ship> ships,int guardx, int guardy ){
        int min = Integer.MAX_VALUE;
        int max_people =Integer.MIN_VALUE;
        for (String key: ships.keySet()) {
            String[] loc = key.split(",");
            int xShip = Integer.parseInt(loc[0]);
            int yShip = Integer.parseInt(loc[1]);
            int distance = Math.abs(xShip - guardx) + Math.abs(yShip - guardy);
            if (distance < min){
                min = distance;
                max_people = ships.get(key).aliveOnBoard;
            }
            else if(distance == min){
                int people =ships.get(key).aliveOnBoard;

                max_people = (people>max_people)? people:max_people;
            }
        }
        return new int []{min,max_people};
    }
    private static int[] maxPeopleNDistGuardShip (HashMap<String,Ship> ships,int guardx, int guardy){
        int max_people = Integer.MIN_VALUE;
        int dist =0;
        for (String key: ships.keySet()) {
            String[] loc = key.split(",");
            int people= ships.get(key).aliveOnBoard;

            if (people> max_people){
                int xShip = Integer.parseInt(loc[0]);
                int yShip = Integer.parseInt(loc[1]);
                dist = Math.abs(xShip - guardx) + Math.abs(yShip - guardy);
                max_people= people;
            }
            else if(people==max_people){
                int xShip = Integer.parseInt(loc[0]);
                int yShip = Integer.parseInt(loc[1]);
                int dist2 = Math.abs(xShip - guardx) + Math.abs(yShip - guardy);
                if(dist>dist2){
                    dist=dist2;
                }
            }


        }
        return new int []{max_people,dist};
 }

 private static int calcHeuristic1(int peopleToRescue,int minDistGuardShipsToMax, int maxPeople){
        int deadCount= 0;
        if(maxPeople<= minDistGuardShipsToMax)   {
            deadCount+= maxPeople;
        }
        else{
            deadCount+= minDistGuardShipsToMax;
        }
        if (peopleToRescue-maxPeople<=minDistGuardShipsToMax){
            deadCount+= peopleToRescue-maxPeople;
        }
        else{
            deadCount+= minDistGuardShipsToMax;
        }

        return deadCount;


    }
    //this heuristic approximates the number of deaths by calculating the min number odf steps to the closest ship and
    //the closest ship .. this heuristic handles the two cases if all the people alive in the situation is less than
    // what is calculated to die in this way to the minimum distance and the other case if the other ships has less than
    // what it calculates the deaths to be
    private static int calcHeuristic2(int peopleToRescue,int minDistGuardShips, int peopleOnMin){
        int peopleDead = 0;

        if(peopleOnMin < minDistGuardShips)   {
            peopleDead+=peopleOnMin;
        }
        else{
            peopleDead+=minDistGuardShips;
        }
        if (peopleToRescue-peopleOnMin< minDistGuardShips){
            peopleDead+=(peopleToRescue-peopleOnMin);
        }
        else{
            peopleDead+=minDistGuardShips;
        }

        return peopleDead;

    }
//this is a helper function that updates the state if the agent moved in any direction
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

        }
        int guardX = nodeToExpandState.guardX;
        int guardY = nodeToExpandState.guardY;
        switch (operator){
            case UP: guardX--;break;
            case DOWN: guardX++; break;
            case RIGHT: guardY++; break;
            case LEFT: guardY--; break;
        }
        State newState = new State(guardX, guardY, myShips ,
                nodeToExpandState.spotsAvailable,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive,
                nodeToExpandState.boxesToRetrieve-boxesLost, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved, shipsWithPeopleAlive);
        return newState;
    }
    //this is a helper function that updates the state if the agent  dropped of passengers at a station
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
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                initState.spotsAvailable,nodeToExpandState.peopleToRescue-(shipsWithPeopleAlive+peopleOnBoard),
                nodeToExpandState.boxesToRetrieve-boxesLost,
                nodeToExpandState.peopleRescued+peopleOnBoard,
                nodeToExpandState.boxesRetrieved, shipsWithPeopleAlive);
        return newState;
    }
    //this is a helper function that updates the state if the agent retrieved a box
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
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                nodeToExpandState.spotsAvailable,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive,
                nodeToExpandState.boxesToRetrieve-boxesLost-1, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved+1, shipsWithPeopleAlive);
        return newState;
    }
    //this is a helper function that updates the state if the agent picked up passengers at a ship
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
        }

        State newState = new State(nodeToExpandState.guardX, nodeToExpandState.guardY, myships,
                nodeToExpandState.spotsAvailable-peopleRetrieved,nodeToExpandState.peopleToRescue-shipsWithPeopleAlive ,
                nodeToExpandState.boxesToRetrieve-boxesLost, nodeToExpandState.peopleRescued,
                nodeToExpandState.boxesRetrieved, shipsWithPeopleAlive);
        return newState;


    }
// function that implements bfs by enqueuing every expansion after checking for redundancy in the queue then upon
// popping checking if it's the goal state
    private static String BFS(int[][] matrix, State initState,boolean visualize){
        int expanded =0;
        HashSet<String> visitedStates = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        Node initNode = new Node (initState,null,null);
        queue.offer(initNode);
        while (!queue.isEmpty()) {
            Node nodeToExpand = queue.poll();
            expanded+=1;
            if (!goalTest(nodeToExpand.state)){
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

                    }
                }
                // retrieved
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0&&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);

                    }}
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                    }
                }
                //up
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP);
                   String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                    }

                }
                //down
                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                    }

                }
                //right
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                    }
                }
                //left
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        queue.offer(newNode);
                    }
                }
            }
            else{
                if(visualize){
                    visualize(matrix,nodeToExpand);
                }
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;

                 while(nodeToExpand.parent!=null){
                     solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                     nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+expanded);
                return String.valueOf(solution);

            }
        }
        return "";
    }
    // function that implements bfs by adding to stack every expansion after checking for redundancy in the stack
    // then upon popping checking if it's the goal state.. This function also takes max depth as a parameter
    // to be used a sa helper method for the iterative Iterative depth search in which the depth is the maximum depth for the iteration..
    // for normal DFS the Depth is an Integer.MAX
    private static String DFS(int[][] matrix, State initState, int depth, boolean visualize){
        int expanded = 0;
        HashSet<String> visitedStates = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        Node initNode = new Node (initState,null,null);
        stack.push(initNode);
        while (!stack.isEmpty()) {
            Node nodeToExpand = stack.pop();
            expanded+=1;
            if ((!goalTest(nodeToExpand.state))&& nodeToExpand.depth <= depth ){
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
                    }

                }
                //retrieve
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0&&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                    State newState = retrieve(nodeToExpand.state);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
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
                    }

                }
                //up
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                    }

                }
                //down
                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                    }

                }
                //right
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        stack.push(newNode);
                    }

                }
                // left
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT);
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                       stack.push(newNode);
                    }


                }
            }
            else if (goalTest(nodeToExpand.state)){
                if(visualize){
                    visualize(matrix,nodeToExpand);
                }
                //plan;deaths;retrieved;nodes
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;

                 while(nodeToExpand.parent!=null){
                     solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                     nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+expanded);
                return String.valueOf(solution);

            }



        }
        return "";

    }
//This the function that implements the iterative depth search it loops with an increment of depth by 7
// if it didn't reach the goal state in the past iteration and pass the depth parameter to the DFS function
    private static String IDS(int[][] matrix, State initState, boolean visualize){

        for( int limit= 0; limit< Integer.MAX_VALUE; limit+=7){
            String answer = DFS(matrix,initState,limit, visualize);
            if (answer.length()!=0){
                return answer;
            }
        }

        return "";
    }
    //A function that implements the greedy heuristic no.1 it uses a priority queue for Nodes and it takes the minimum
    // distance between a ship and a station as a parameter because it's used in the heuristic
    private static String GR1(int[][] matrix, State initState,boolean visualize){

        int expanded =0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        HashSet<String> visitedStates = new HashSet<>();
        int [] maxShip= maxPeopleNDistGuardShip (initState.ships,initState.guardX, initState.guardY );
        Node initNode = new Node (initState,null,null,calcHeuristic1(initState.peopleToRescue,maxShip[1], maxShip[0]));
        priorityQueue.offer(initNode);
        while (!priorityQueue.isEmpty()) {
            Node nodeToExpand = priorityQueue.poll();
            expanded+=1;
            if (!goalTest(nodeToExpand.state)){
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }

                //retrieve
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0
                        &&nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                    State newState = retrieve(nodeToExpand.state);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //up
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
                 //down
                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }

                }
                //right
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //left
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }
                }
            }
            else{
                if(visualize){
                    visualize(matrix,nodeToExpand);
                }
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;

                 while(nodeToExpand.parent!=null){
                     solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                     nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+expanded);
                return String.valueOf(solution);

            }
        }
        return "";
    }
    //A function that implements the greedy heuristic no.2 it uses a priority queue for Nodes and it takes the minimum
    // distance between a ship and a station as a parameter because it's used in the heuristic
    private static String GR2(int[][] matrix, State initState, boolean visualize){
       int expanded = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        HashSet<String> visitedStates = new HashSet<>();
        int [] minDist = minDistGuardShip(initState.ships, initState.guardX,initState.guardY);
        Node initNode = new Node (initState,null,null,calcHeuristic2(initState.peopleToRescue, minDist[0],minDist[1]));
        priorityQueue.offer(initNode);
        while (!priorityQueue.isEmpty()) {
            Node nodeToExpand = priorityQueue.poll();
            expanded+=1;
            if (!goalTest(nodeToExpand.state)){
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0],minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //retrieve
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0&&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                    State newState = retrieve(nodeToExpand.state);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0],minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0],minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //up
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
                //down
                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0],minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }

                }
                //right
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0],minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //left
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }
                }
            }
            else{
                if(visualize){
                  visualize(matrix,nodeToExpand);
                }
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                 while(nodeToExpand.parent!=null){
                    solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                    nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+expanded);
                return String.valueOf(solution);

            }
        }
        return "";

    }



//    function that implements A* search using the first heuristic and the path cost
    private static String AS1(int[][] matrix,  State initState,  boolean visualize){
       int expanded = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        HashSet<String> visitedStates = new HashSet<>();
        int [] maxShip= maxPeopleNDistGuardShip (initState.ships,initState.guardX, initState.guardY );
        Node initNode = new Node (initState,null,null,calcHeuristic1(initState.peopleToRescue,maxShip[1], maxShip[0]));
        priorityQueue.offer(initNode);
        while (!priorityQueue.isEmpty()) {
            Node nodeToExpand = priorityQueue.poll();
            expanded+=1;
            if (!goalTest(nodeToExpand.state)){
                //pickup
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                        && nodeToExpand.state.spotsAvailable>0 ){
                    State newState = pickUp(nodeToExpand.state);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]),  pathCost(nodeToExpand,newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }

                //retrieve
                if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                        nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                        nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0
                        && nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                    State newState = retrieve(nodeToExpand.state);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]),pathCost(nodeToExpand, newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
                // drop off
                if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                        nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                    State newState =DropOff(nodeToExpand.state, initState);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.DROP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]),pathCost(nodeToExpand, newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }
                //up
                if(nodeToExpand.state.guardX>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.UP);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.UP,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]), pathCost(nodeToExpand,newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }

                }
//down
                if(nodeToExpand.state.guardX<matrix.length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.DOWN,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]), pathCost(nodeToExpand, newState,initState) );
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }

                }
//right
                if(nodeToExpand.state.guardY<matrix[0].length-1){
                    State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]),pathCost(nodeToExpand, newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);

                    }
                }
                //left
                if(nodeToExpand.state.guardY>0){
                    State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                    maxShip= maxPeopleNDistGuardShip (newState.ships,newState.guardX, newState.guardY );
                    Node newNode = new Node(newState, nodeToExpand, Operator.LEFT,calcHeuristic1((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),maxShip[1], maxShip[0]),pathCost(nodeToExpand, newState,initState));
                    String stringifiedState = stringify(newState);
                    if (!visitedStates.contains(stringifiedState)){
                        visitedStates.add(stringifiedState);
                        priorityQueue.offer(newNode);
                    }
                }


            }
            else{
                if(visualize){
                    visualize(matrix,nodeToExpand);
                }
                StringBuilder solution = new StringBuilder();
                int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
                int retrieved = nodeToExpand.state.boxesRetrieved;
                while(nodeToExpand.parent!=null){
                    solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                    nodeToExpand= nodeToExpand.parent;
                }
                solution.replace(solution.length()-1,solution.length(),";");
                solution.append(deaths+";"+retrieved+";"+expanded);
                return String.valueOf(solution);

            }
        }
        return "";
}


 private static String AS2(int[][] matrix, State initState, boolean visualize){
        int expanded  = 0;
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
     HashSet<String> visitedStates = new HashSet<>();
     int [] minDist = minDistGuardShip( initState.ships, initState.guardX,initState.guardY);
     Node initNode = new Node (initState,null,null,calcHeuristic2(initState.peopleToRescue,minDist[0], minDist[1]));
     priorityQueue.offer(initNode);
     while (!priorityQueue.isEmpty()) {
         Node nodeToExpand = priorityQueue.poll();
         expanded+=1;
         if (!goalTest(nodeToExpand.state)){
             //pickup
             if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                     nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                     nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard>0
                     && nodeToExpand.state.spotsAvailable>0 ){
                 State newState = pickUp(nodeToExpand.state);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);

                 Node newNode = new Node(newState, nodeToExpand, Operator.PICKUP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                 }
             }
             //retrieve
             if (matrix[nodeToExpand.state.guardX][nodeToExpand.state.guardY]==1 &&
                     nodeToExpand.state.ships.containsKey(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY) &&
                     nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).aliveOnBoard<=0&&
                     nodeToExpand.state.ships.get(nodeToExpand.state.guardX+","+nodeToExpand.state.guardY).counter>0){
                 State newState = retrieve(nodeToExpand.state);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.RETRIEVE,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                 }

             }
             // drop off
             if (matrix[nodeToExpand.state.guardX] [nodeToExpand.state.guardY]==2 &&
                     nodeToExpand.state.spotsAvailable<initState.spotsAvailable){
                 State newState =DropOff(nodeToExpand.state, initState);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.DROP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                 }
             }
             //up
             if(nodeToExpand.state.guardX>0){
                 State newState = updateMoving(nodeToExpand.state,Operator.UP);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.UP,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                  }

             }
//down
             if(nodeToExpand.state.guardX<matrix.length-1){
                 State newState = updateMoving(nodeToExpand.state,Operator.DOWN);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.DOWN,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                  }

             }
//right
             if(nodeToExpand.state.guardY<matrix[0].length-1){
                 State newState = updateMoving(nodeToExpand.state,Operator.RIGHT);
                 minDist = minDistGuardShip( newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.RIGHT,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                  }
             }
             //left
             if(nodeToExpand.state.guardY>0){
                 State newState = updateMoving(nodeToExpand.state,Operator.LEFT);
                 minDist = minDistGuardShip(newState.ships ,newState.guardX,newState.guardY);
                 Node newNode = new Node(newState, nodeToExpand, Operator.LEFT,calcHeuristic2((newState.peopleToRescue-(initState.spotsAvailable- newState.spotsAvailable)),minDist[0], minDist[1]),pathCost(nodeToExpand, newState, initState));
                 String stringifiedState = stringify(newState);
                 if (!visitedStates.contains(stringifiedState)){
                     visitedStates.add(stringifiedState);
                     priorityQueue.offer(newNode);
                  }
             }

         }
         else{
             if(visualize){
                 visualize(matrix,nodeToExpand);
             }
             StringBuilder solution = new StringBuilder();
             int deaths = initState.peopleToRescue-nodeToExpand.state.peopleRescued;
             int retrieved = nodeToExpand.state.boxesRetrieved;
             while(nodeToExpand.parent!=null){
                 solution.insert(0,nodeToExpand.operator.toString().toLowerCase()+",");
                 nodeToExpand= nodeToExpand.parent;
             }
             solution.replace(solution.length()-1,solution.length(),";");
             solution.append(deaths+";"+retrieved+";"+expanded);
             return String.valueOf(solution);

         }
     }
     return "";
 }

    public static void main(String[] args) {
        CoastGuard cs = new CoastGuard();
        //test 2222
        String grid0 = "5,6;50;0,1;0,4,3,3;1,1,90;";
        String grid1 = "6,6;52;2,0;2,4,4,0,5,4;2,1,19,4,2,6,5,0,8;";
        String grid2 = "7,5;40;2,3;3,6;1,1,10,4,5,90;";
        String grid3 = "8,5;60;4,6;2,7;3,4,37,3,5,93,4,0,40;";
        String grid4 = "5,7;63;4,2;6,2,6,3;0,0,17,0,2,73,3,0,30;";
        String grid5 = "5,5;69;3,3;0,0,0,1,1,0;0,3,78,1,2,2,1,3,14,4,4,9;";
        String grid6 = "7,5;86;0,0;1,3,1,5,4,2;1,1,42,2,5,99,3,5,89;";
        String grid7= "6,7;82;1,4;2,3;1,1,58,3,0,58,4,2,72;";
        String grid8 = "6,6;74;1,1;0,3,1,0,2,0,2,4,4,0,4,2,5,0;0,0,78,3,3,5,4,3,40;";
        String grid9 = "7,5;100;3,4;2,6,3,5;0,0,4,0,1,8,1,4,77,1,5,1,3,2,94,4,3,46;";
        String grid10= "10,6;59;1,7;0,0,2,2,3,0,5,3;1,3,69,3,4,80,4,7,94,4,9,14,5,2,39;";


//String random =genGrid();
//        System.out.println(random);
        String sol = solve(grid1,"BF" , true);
        System.out.println("BFS : "+sol);
        sol = solve(grid1,"DF" , true);
        System.out.println("DFS: "+sol);
        sol = solve(grid1,"ID" , true);
        System.out.println("ID: "+sol);
        sol = solve(grid1,"GR1" , true);
        System.out.println("GR1: "+sol);
        sol = solve(grid1,"GR2" , true);
        System.out.println("GR2: "+sol);
        sol = solve(grid1,"AS1" , true);
        System.out.println("AS1: "+sol);
        sol = solve(grid1,"AS2" , true);
        System.out.println("AS2: "+sol);



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


    public static int[] pathCost(Node parentNode, State state, State initState) {

        return new int[]{(parentNode.pathCost[0] + state.shipsWithPpl),(initState.boxesToRetrieve-state.boxesToRetrieve-state.boxesRetrieved)};
    }
}
