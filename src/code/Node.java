package code;

import java.util.HashMap;

public class Node implements Comparable<Node> {

    State state;
    Node parent;
    Operator operator;
    int depth;
    int[] pathCost;
    int boxesLost;
    int heuristic;

    public Node(State currState, Node parent, Operator operator) {
        this.state = currState;
        this.parent = parent;
        this.operator = operator;
        this.pathCost = new int[]{0, 0};
        if (parent != null)
            this.depth = parent.depth + 1;
        else {
            this.depth = 0;
        }

    }

    public Node(State currState, Node parent, Operator operator, int heuristic) {
        this.state = currState;
        this.parent = parent;
        this.operator = operator;
        this.heuristic = heuristic;
        this.pathCost = new int[]{0, 0};
        if (parent != null)
            this.depth = parent.depth + 1;
        else {
            this.depth = 0;
        }
    }

    public Node(State currState, Node parent, Operator operator, int heuristic, int[] pathCost) {
        this.state = currState;
        this.parent = parent;
        this.operator = operator;
        this.heuristic = heuristic;
        this.pathCost = pathCost;
        this.boxesLost = boxesLost;
        if (parent != null)
            this.depth = parent.depth + 1;
        else {
            this.depth = 0;
        }
    }

    @Override
    public int compareTo(Node node) {
        if ((this.heuristic+this.pathCost[0]) == (node.heuristic+node.pathCost[0])) {
            return (this.pathCost[1])-(node.pathCost[1]) ;
        }

        return (this.heuristic+this.pathCost[0])-(node.heuristic+node.pathCost[0]) ;

    }
}