package code;

import java.util.HashMap;

public class Node {

    State state;
    Node parent;
    Operator operator;
    int depth;
    int pathCost;

    public Node(State currState, Node parent, Operator operator){
        this.state= currState;
        this.parent = parent;
        this.operator = operator;
        if (parent!= null)
            this.depth = parent.depth+1;
    }
}
