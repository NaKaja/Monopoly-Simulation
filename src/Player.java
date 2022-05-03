import java.util.ArrayList;
import java.util.HashSet;

public class Player {

    final String name;
    int balance, position, utilsOwned;
    ArrayList<Property> ownedProperties;
    ArrayList<Property> board;
    HashSet<String> suitesOwned;
    STRATEGY strat;
    Player other;

    public Player(String name, ArrayList<Property> board, STRATEGY strat){
        this.name = name;
        this.board = board;
        this.strat = strat;
        balance = 1500;
        position = 0;
        utilsOwned = 0;
        ownedProperties = new ArrayList<>();
        suitesOwned = new HashSet<>();
    }

    public enum STRATEGY {
        HIGH,
        MEDIUM,
        LOW,
        ALL
    }

    public boolean hasSuiteNeedingHouses(){
        boolean result = false;

        for(String s : suitesOwned){
            int avail = 0;
            for(Property p : board){
                if(p.color.equals(s) && p.owner == this){
                    avail++;
                }
            }
            if(s.equals("BROWN") || s.equals("DARKBLUE")){
                if(avail == 2){
                    result = true;
                    break;
                }
            }
            else{
                if(avail == 3){
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public void addSuite(String color){
        suitesOwned.add(color);
    }

    public void removeSuite(String color){
        suitesOwned.remove(color);
    }

    public boolean nowOwnsSuite(String color){
        for(Property p : board){
            if (p.color.equals(color) && p.owner != this) {
                return false;
            }
        }
        return true;
    }

    public boolean ownsSuite(String color){
        return suitesOwned.contains(color);
    }

    public boolean isBankrupt(){
        return balance <= 0;
    }

    public void setOther(Player other){
        this.other = other;
    }

    public void giveOther(int amount){
        other.changeBalance(amount);
    }

    public void addProperty(Property p){
        ownedProperties.add(p);
        if(p.name.startsWith("util")){utilsOwned++;}
        if(nowOwnsSuite(p.color)){
            addSuite(p.color);
            if(Main.DEBUG){System.out.println(displayName() + " now owns suite: " + p.color);}
        }
    }

    public void propertyReport(){
        System.out.println("----------------------------------------------------");
        for(Property p : ownedProperties){
            System.out.println(p.getName() + " with " + p.numHouses + " houses");
        }
        System.out.println(displayName() + " owns " + ownedProperties.size() + " properties (" + suitesOwned.size() + " suites)");

        System.out.println("----------------------------------------------------");

    }

    public int numRROwned(){
        int result = 0;
        for(Property p : ownedProperties){
            if(p.getName().startsWith("RR")){
                result++;
            }
        }
        return result;
    }

    public void changeBalance(int change){
        balance += change;
    }

    public int getPosition(){
        return position;
    }

    public int getBalance(){
        return balance;
    }

    public int getUtilsOwned(){ return utilsOwned; }

    public void advance(int spaces){
        position += spaces;
        if(position >= 40){
            balance += 200;
            if(Main.DEBUG){System.out.println(displayName() + " passed GO. Balance: " + balance);}
        }
        position = position % 40;
    }

    public int numHouses(){
        int result = 0;
        for(Property p : ownedProperties){
            if(p.numHouses <= 4){
                result += p.numHouses;
            }
        }
        return result;
    }

    public int numHotels(){
        int result = 0;
        for(Property p : ownedProperties){
            if(p.numHouses == 5){
                result++;
            }
        }
        return result;
    }

    public String displayName(){
        return "[Player " + name + "]";
    }

    public String toString(){
        return "[Player " + name + "] on space {" + position + "}: " + board.get(position).getName();
    }

}
