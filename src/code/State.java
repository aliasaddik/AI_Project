package code;

import java.util.HashMap;
//The state object holds info about the state of the world in a particular node in the search tree
public class State {
    int peopleRescued;
    //the sum of people alive on ships and the ones the coast guard it holding
    int peopleToRescue;
    //boxes not yet retrieved and not yet expired
    int boxesToRetrieve;
    //the X location of the guard
    int guardX;
    //the Y location of the guard
    int guardY;
    //A hashmap list of ship objects, each holding info about
    HashMap<String,Ship> ships;
    //the maximum number of people the CG can pickup in this state
    int spotsAvailable;
    //boxes retrieved be CG
    int boxesRetrieved;
    // The number of ships in the world with people alive on board
    int shipsWithPpl;


    public State(int guardX, int guardY, HashMap<String,Ship> ships, int spotsAvailable, int peopleToRescue,
                 int boxesToRetrieve, int peopleRescued, int boxesRetrieved , int shipsWithPpl ){
        this.guardX = guardX;
        this.guardY = guardY;
        this.ships = ships;
        this.spotsAvailable = spotsAvailable;
        this.peopleToRescue = peopleToRescue;
        this. boxesToRetrieve = boxesToRetrieve;
        this.peopleRescued = peopleRescued;
        this.boxesRetrieved = boxesRetrieved;
        this.shipsWithPpl=shipsWithPpl;


    }


}
