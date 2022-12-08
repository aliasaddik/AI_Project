package code;
//The ship object encapsulates info about a single ship and is kept in individual state object
//Each ship object represents info about a ship for a particular state
//In other words, a ship object is never updated or reused across nodes
public class Ship {
    int aliveOnBoard; //people alive on the ship
    int dead;//people dead
    int counter; // counter before box expires

    public Ship(int aliveOnBoard){
        this.aliveOnBoard = aliveOnBoard;
        this.dead = 0;
        this.counter = 20;

    }
    public Ship(int aliveOnBoard, int dead, int counter){
        this.aliveOnBoard = aliveOnBoard;
        this.dead = dead;
        this.counter = counter;

    }
    //This update is called after ship objects from previous states are cloned then updates the ship
    //update corresponds to the status of the ship after the CG took a step (applied an operator)
    public void Update(){
    if(aliveOnBoard==1){
       dead++;
       aliveOnBoard--;
    }
        if (aliveOnBoard ==0 && counter>0){
            counter --;
        }
        else{
            dead++;
            aliveOnBoard--;

        }
    }

}
