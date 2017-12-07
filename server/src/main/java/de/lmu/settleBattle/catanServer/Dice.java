package de.lmu.settleBattle.catanServer;import java.util.Random;

public class Dice {

    private Random rand = new Random();
    private int[] dice = new int[2];


    /**
     *<method name:randomInt
     *description: generates a random int between two integers loVal and hiVal>
     *<preconditions: none>
     *<postconditions: none>
     */
    private int randomInt(int loVal, int hiVal){
        int randomNum = loVal+ rand.nextInt(hiVal);
        return randomNum;
    }

    /**
     *<method name: rollDice
     *description : sets the value of the two dice to a random number between 1 and 6 and returns the total value>
     *<preconditions: none>
     *<postconditions: die1SideUp and die2SideUp set to a random number>
     */
    public int[] roll() {
        dice[0] = randomInt(1,6);
        dice[1] = randomInt(1,6);
        return dice;
    }
}