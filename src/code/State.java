package code;

import java.util.ArrayList;
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


    public State(int guardX, int guardY, HashMap<String,Ship> ships, int spotsAvailable, int peopleToRescue,
                 int boxesToRetrieve, int peopleRescued, int boxesRetrieved ){
        this.guardX = guardX;
        this.guardY = guardY;
        this.ships = ships;
        this.spotsAvailable = spotsAvailable;
        this.peopleToRescue = peopleToRescue;
        this. boxesToRetrieve = boxesToRetrieve;
        this.peopleRescued = peopleRescued;
        this.boxesRetrieved = boxesRetrieved;




    }

}
