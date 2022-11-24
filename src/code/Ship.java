package code;

public class Ship {
    int aliveOnBoard;
    int dead;
    int counter;

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
    public void Update(){
        if (aliveOnBoard==0 && counter>0){
            counter --;
        }
        else{
            dead++;
            aliveOnBoard--;
        }
    }

}
