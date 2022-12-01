package code;

import java.util.HashMap;

public class Node implements Comparable<Node> {

    State state;
    Node parent;
    Operator operator;
    int depth;
    int pathCost;
    int h1;
    int h2;

    public Node(State currState, Node parent, Operator operator){
        this.state= currState;
        this.parent = parent;
        this.operator = operator;
        if (parent!= null)
            this.depth = parent.depth+1;
        else{
            this.depth = 0;
        }

    }

    @Override
    public int compareTo(Node node) {

        return 0;
    }
}
