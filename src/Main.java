import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static ArrayList<Property> board = new ArrayList<>();
    public static final int MAX_HOUSES = 32;
    public static final int MAX_HOTELS = 12;
    public static int housesPlaced = 0;
    public static int hotelsPlaces = 0;
    public static int freeParking = 0;
    public static final int MAX_TURNS = 1000;
    public static final int ITERATIONS = 1; //Set to 1 or 10 million for running monte carlo sims
    public static final boolean DEBUG = true; //Only recommended turning on for single games

    public static void main(String[] args) {
        loadBoard();
        //monteCarloSuite();
        monteCarlo(Player.STRATEGY.ALL, Player.STRATEGY.ALL);
    }

    public static void monteCarloSuite(){
        System.out.println("P1-LOW P2-LOW : " + monteCarlo(Player.STRATEGY.LOW, Player.STRATEGY.LOW));
        System.out.println("P1-LOW P2-MED : " + monteCarlo(Player.STRATEGY.LOW, Player.STRATEGY.MEDIUM));
        System.out.println("P1-LOW P2-HI  : " + monteCarlo(Player.STRATEGY.LOW, Player.STRATEGY.HIGH));
        System.out.println("P1-LOW P2-ALL : " + monteCarlo(Player.STRATEGY.LOW, Player.STRATEGY.ALL));

        System.out.println("P1-MED P2-LOW : " + monteCarlo(Player.STRATEGY.MEDIUM, Player.STRATEGY.LOW));
        System.out.println("P1-MED P2-MED : " + monteCarlo(Player.STRATEGY.MEDIUM, Player.STRATEGY.MEDIUM));
        System.out.println("P1-MED P2-HI  : " + monteCarlo(Player.STRATEGY.MEDIUM, Player.STRATEGY.HIGH));
        System.out.println("P1-MED P2-ALL : " + monteCarlo(Player.STRATEGY.MEDIUM, Player.STRATEGY.ALL));

        System.out.println("P1-HI  P2-LOW : " + monteCarlo(Player.STRATEGY.HIGH, Player.STRATEGY.LOW));
        System.out.println("P1-HI  P2-MED : " + monteCarlo(Player.STRATEGY.HIGH, Player.STRATEGY.MEDIUM));
        System.out.println("P1-HI  P2-HI  : " + monteCarlo(Player.STRATEGY.HIGH, Player.STRATEGY.HIGH));
        System.out.println("P1-HI  P2-ALL : " + monteCarlo(Player.STRATEGY.HIGH, Player.STRATEGY.ALL));

        System.out.println("P1-All P2-LOW : " + monteCarlo(Player.STRATEGY.ALL, Player.STRATEGY.LOW));
        System.out.println("P1-ALL P2-MED : " + monteCarlo(Player.STRATEGY.ALL, Player.STRATEGY.MEDIUM));
        System.out.println("P1-ALL P2-HI  : " + monteCarlo(Player.STRATEGY.ALL, Player.STRATEGY.HIGH));
        System.out.println("P1-ALL P2-ALL : " + monteCarlo(Player.STRATEGY.ALL, Player.STRATEGY.ALL));
    }

    public static int monteCarlo(Player.STRATEGY s1, Player.STRATEGY s2){
        ArrayList<Integer> results = new ArrayList<>();

        for(int i = 0; i < ITERATIONS; i++){
            Player p1 = new Player("1", board, s1);
            Player p2 = new Player("2", board, s2);
            p1.setOther(p2);
            p2.setOther(p1);
            results.add(runGame(p1, p2));
            if(DEBUG){System.out.println("Game " + i + " complete");}
        }

        int sum = 0;
        for (Integer result : results) {
            sum += result;
        }
        return (sum/results.size());
    }

    public static int runGame(Player p1, Player p2){
        for(int i = 0; i < MAX_TURNS; i++){
            if(DEBUG){System.out.println("{Turn " + i + "}");}
            playTurn(p1);
            playTurn(p2);
            if(p1.isBankrupt()){
                attemptMortgage(p1);
                if(p1.isBankrupt()){
                    if(DEBUG){System.out.println(p1.displayName() + " loses on turn " + i);}
                    break;
                }
            }
            if(p2.isBankrupt()){
                attemptMortgage(p2);
                if(p2.isBankrupt()){
                    if(DEBUG){System.out.println(p2.displayName() + " loses on turn " + i);}
                    break;
                }
            }
            if(DEBUG){
                System.out.println("Houses: " + housesPlaced + "/" + MAX_HOUSES);
                System.out.println("Hotels: " + hotelsPlaces + "/" + MAX_HOTELS);
                System.out.println("-------------");
            }
        }
        if(DEBUG){
            System.out.println("-----------------------------");
            System.out.println(p1.displayName() + " final balance: " + p1.getBalance());
            System.out.println(p2.displayName() + " final balance: " + p2.getBalance());
            System.out.println("Difference (p1 view): " + (p1.getBalance() - p2.getBalance()));
            System.out.println("-----------------------------");
            p1.propertyReport();
            p2.propertyReport();
        }
        resetBoard();
        if(!p1.isBankrupt() && !p2.isBankrupt()){ //Went to max turns. Can happen since trading doesn't exist
            return 0;
        }
        else{
            return (p1.getBalance() - p2.getBalance());
        }

    }

    public static void attemptMortgage(Player player){
        if(DEBUG){System.out.println(player.displayName() + " mortgaging at balance: " + player.getBalance());}
        while(player.isBankrupt()){
            if(player.ownedProperties.size() <= 0){
                break;
            }
            else{
                //select a random owned property to mortgage
                int idx = (int) (Math.random()*player.ownedProperties.size());
                Property toMortgage = player.ownedProperties.get(idx);
                player.ownedProperties.remove(idx);
                if(DEBUG){System.out.print(">mortgaging " + toMortgage.getName() + ". Balance: " + player.getBalance() + " -> ");}
                player.changeBalance(toMortgage.price / 2);
                if(player.ownsSuite(toMortgage.color)){
                    player.removeSuite(toMortgage.color);
                }
                if(toMortgage.name.startsWith("*util")){
                    player.utilsOwned--;
                }
                toMortgage.owned = false;
                toMortgage.owner = null;
                player.changeBalance(toMortgage.numHouses * (toMortgage.houseCost/2));
                if(toMortgage.numHouses == 5){
                    hotelsPlaces--;
                }
                else{
                    housesPlaced -= toMortgage.numHouses;
                }
                toMortgage.numHouses = 0;
                if(DEBUG){System.out.println(player.getBalance());}
            }
        }
        if(DEBUG){System.out.println(">Now owns " + player.ownedProperties.size() + " properties");}
    }

    public static void playTurn(Player player){
        int roll = rollDice() + rollDice();
        player.advance(roll);
        Property landed = board.get(player.getPosition());
        if(DEBUG){System.out.println(player.displayName() + " Rolled: " + roll + " now on {" + player.getPosition() + "} " + landed.getName());}

        if(player.ownedProperties.contains(landed)){
            if(DEBUG){System.out.println(player.displayName() + " already owns: " + landed.getName() + ". Balance: " + player.getBalance());}
        }
        else if(landed.isOwned() && !landed.getName().startsWith("RR") && !landed.getName().startsWith("util")){
            if(DEBUG){System.out.print(player + " landed on with " + landed.numHouses + " houses. Owned by other. Balance: " + player.getBalance());}
            player.changeBalance(-1 * landed.visitingCosts[landed.numHouses]);
            player.giveOther(landed.visitingCosts[landed.numHouses]);
            if(DEBUG){System.out.println(" -> " + player.getBalance());}
        }
        else if(landed.isOwned() && landed.getName().startsWith("RR")){
            if(DEBUG){System.out.print(player + " landed on with " + player.other.numRROwned() + " RRs owned by other. Balance: " + player.getBalance());}
            player.changeBalance(-1 * landed.visitingCosts[player.other.numRROwned() + 1]);
            player.giveOther(landed.visitingCosts[landed.numHouses]);
            if(DEBUG){System.out.println(" -> " + player.getBalance());}
        }
        else if(landed.isPurchasable() && player.getBalance() >= landed.price){
            switch (player.strat){
                case ALL:
                    landed.boughtBy(player);
                    player.addProperty(landed);
                    if(DEBUG){System.out.println(player + " bought. Balance: " + player.getBalance());}
                    break;
                case LOW:
                    if(landed.value == Property.Level.LOW){
                        landed.boughtBy(player);
                        player.addProperty(landed);
                        if(DEBUG){System.out.println(player + " bought. Balance: " + player.getBalance());}
                    }
                    break;
                case MEDIUM:
                    if(landed.value == Property.Level.MEDIUM){
                        landed.boughtBy(player);
                        player.addProperty(landed);
                        if(DEBUG){System.out.println(player + " bought. Balance: " + player.getBalance());}
                    }
                    break;
                case HIGH:
                    if(landed.value == Property.Level.HIGH){
                        landed.boughtBy(player);
                        player.addProperty(landed);
                        if(DEBUG){System.out.println(player + " bought. Balance: " + player.getBalance());}
                    }
                    break;
            }
        }
        else if(landed.name.equals("*chance")){
            chance(player);
        }
        else if(landed.name.equals("*taxIncome")){
            player.changeBalance(-200);
            if(DEBUG){System.out.println(player.displayName() + " (INCOME TAX) lost 200");}
            freeParking += 200;
        }
        else if(landed.name.equals("*taxLuxury")){
            player.changeBalance(-100);
            if(DEBUG){System.out.println(player.displayName() + " (LUXURY TAX) lost 100");}
            freeParking += 100;
        }
        else if(landed.name.equals("*freeParking")){
            player.changeBalance(freeParking);
            if(DEBUG){System.out.println(player.displayName() + " (FREE PARKING) gained " + freeParking);}
            freeParking = 0;
        }
        else if(landed.name.startsWith("util")){
            if(DEBUG){System.out.print(player + " landed on. " + player.other.getUtilsOwned() + " utilities owned by other. Balance: " + player.getBalance());}
            if(player.other.getUtilsOwned() == 1){
                player.changeBalance(-4 * roll);
                player.giveOther(-4 * roll);
            }
            if(player.other.getUtilsOwned() == 2){
                player.changeBalance(-10 * roll);
                player.giveOther(-10 * roll);
            }
            if(DEBUG){System.out.println(" -> " + player.getBalance());}
        }

        //Build houses/hotels
        while(player.getBalance() > 200 && player.hasSuiteNeedingHouses()){
            boolean bought = false;
            for(Property r : player.ownedProperties){
                if(!r.getName().startsWith("RR") && player.ownsSuite(r.color)){
                    if(player.getBalance() > r.houseCost && !r.getName().startsWith("RR") && housesPlaced < MAX_HOUSES && !othersWithoutEvenHouses(r)){
                        if(r.numHouses == 4 && hotelsPlaces < MAX_HOTELS){
                            r.addHouse();
                            hotelsPlaces++;
                            housesPlaced -= 4;
                            if(DEBUG){System.out.print(player.displayName() + " buys hotel on " + r.getName() + ". Balance: " + player.getBalance());}
                            player.changeBalance(-1 * r.houseCost);
                            if(DEBUG){System.out.println(" -> " + player.getBalance());}
                            bought = true;
                        }
                        else if(r.numHouses < 4){
                            r.addHouse();
                            housesPlaced++;
                            if(DEBUG){System.out.print(player.displayName() + " buys house on " + r.getName() + ". Now has " + r.numHouses + " houses. Balance: " + player.getBalance());}
                            player.changeBalance(-1 * r.houseCost);
                            if(DEBUG){System.out.println(" -> " + player.getBalance());}
                            bought = true;
                        }
                    }
                }
            }
            if(!bought){
                break;
            }

        }

    }

    public static boolean othersWithoutEvenHouses(Property check){
        boolean result = false;
        for(Property p : board){
            if (p.color.equals(check.color) && p.numHouses < check.numHouses) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void chance(Player player){
        double event = Math.random();
        if(event < .05){
            player.changeBalance(25);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) gained 25. Balance: " + player.getBalance());}
        }
        else if(event < .1){
            player.changeBalance(-25);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) lost 25. Balance: " + player.getBalance());}
        }
        else if(event < .15){
            player.changeBalance(50);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) gained 50. Balance: " + player.getBalance());}
        }
        else if(event < .2){
            player.changeBalance(-50);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) lost 50. Balance: " + player.getBalance());}
        }
        else if(event < .25){
            player.changeBalance(100);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) gained 100. Balance: " + player.getBalance());}
        }
        else if(event < .3){
            player.changeBalance(-100);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) lost 100. Balance: " + player.getBalance());}
        }
        else if(event < .35){
            player.changeBalance(200);
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) gained 200. Balance: " + player.getBalance());}
        }
        else if(event < .4){
            if(DEBUG){System.out.println(player.displayName() + " (CHANCE) advance to GO");}
            player.advance(40 - player.getPosition()); //force to go
        }
        else if(event < .45){
            if(DEBUG){System.out.print(player.displayName() + " (CHANCE) street repairs! Balance: " + player.getBalance());}
            player.changeBalance(player.numHouses() * -40);
            player.changeBalance(player.numHotels() * -115);
            if(DEBUG){System.out.println(" -> " + player.getBalance());}
        }
        else{
            //
            //System.out.println(player.displayName() + " (CHANCE) nothing");
        }
    }

    public static void resetBoard(){
        for(Property p : board){
            p.owner = null;
            p.owned = false;
            p.numHouses = 0;
        }
        housesPlaced = 0;
        hotelsPlaces = 0;
        freeParking = 0;
    }

    public static void loadBoard(){
        board.add(new Property("GO", 0, null, 0, new int[]{0,0,0,0,0,0}, "n/a"));
        try(Scanner s = new Scanner(new File("layout.txt"))){
            while(s.hasNext()){
                String[] data = s.nextLine().split(",");
                if(!data[0].startsWith("*") && !data[0].startsWith("RR") && !data[0].startsWith("util")){
                    String name = data[0];
                    int price = Integer.parseInt(data[1]);
                    Property.Level value = Property.Level.NONE;
                    switch (data[2]){
                        case "LOW":
                            value = Property.Level.LOW;
                            break;
                        case "MED":
                            value = Property.Level.MEDIUM;
                            break;
                        case "HIGH":
                            value = Property.Level.HIGH;
                            break;
                    }
                    int houseCost = Integer.parseInt(data[3]);
                    int[] visitingCosts = {Integer.parseInt(data[4]), Integer.parseInt(data[5]),
                            Integer.parseInt(data[6]), Integer.parseInt(data[7]), Integer.parseInt(data[8]),
                            Integer.parseInt(data[9])};
                    String color = data[10];
                    board.add(new Property(name, price, value, houseCost, visitingCosts, color));
                }
                else if(data[0].startsWith("RR")){
                    board.add(new Property(data[0], 200, Property.Level.MEDIUM, 0, new int[]{0,0,25,50,100,200}, "n/a"));
                }
                else if(data[0].startsWith("util")){
                    board.add(new Property(data[0], 150, Property.Level.LOW, 0, new int[]{0,0,0,0,0,0}, "n/a"));
                }
                else{
                    board.add(new Property(data[0], 0, Property.Level.NONE, 0, new int[]{0,0,0,0,0,0}, "n/a"));
                }
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static int rollDice(){
        return (int)(Math.random()*6)+1;
    }

}
