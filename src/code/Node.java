package code;

import java.util.HashMap;
//The node object represents a node in the search tree
public class Node implements Comparable<Node> {

    State state;
    //reference to parent node
    Node parent;
    //operator used from previous node to reach this node
    Operator operator;
    //depth of this node in the search tree
    int depth;
    //path cost tuple <people died, boxes lost>
    int[] pathCost;
    //the value given by the heuristic function for this node
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
        if (parent != null)
            this.depth = parent.depth + 1;
        else {
            this.depth = 0;
        }
    }
  //compareTo function to compare two nodes (used by greedy and A* search)
    @Override
    public int compareTo(Node node) {
        if ((this.heuristic+this.pathCost[0]) == (node.heuristic+node.pathCost[0])) {
            return (this.pathCost[1])-(node.pathCost[1]) ;
        }

        return (this.heuristic+this.pathCost[0])-(node.heuristic+node.pathCost[0]) ;

    }
}