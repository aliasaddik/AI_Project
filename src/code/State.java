package code;

import java.util.HashMap;

public class State {
    int peopleRescued;
    int peopleToRescue;
    int boxesToRetrieve;
    int guardX;
    int guardY;
    HashMap<String,Ship> ships;
    int spotsAvailable;
    int boxesRetrieved;

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
