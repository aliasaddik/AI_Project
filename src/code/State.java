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
    public boolean isEqual(State otherState){

        if(this.peopleRescued!= otherState.peopleRescued || this.peopleToRescue!= otherState.peopleToRescue || this.boxesRetrieved != otherState.boxesRetrieved || this.boxesToRetrieve!= otherState.boxesToRetrieve
        || this.spotsAvailable!= otherState.spotsAvailable || this.ships.size()!=otherState.ships.size()){
           return false;
        }
        HashMap<String,Ship> otherShips= otherState.ships;
        for (String key : ships.keySet()) {
            Ship otherShip = otherShips.get(key);
            if(otherShip==null){
                return false;
            }
            Ship currentShip= ships.get(key);
            if(currentShip.aliveOnBoard!=otherShip.aliveOnBoard || currentShip.counter!=otherShip.counter || currentShip.dead!=otherShip.dead){
                return false;
            }
        }
        return true;

    }

}
