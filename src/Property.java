public class Property {

    String name, color;
    int price, houseCost, numHouses;
    int[] visitingCosts;
    Level value;
    boolean owned;
    Player owner;

    public Property(String name, int price, Level value, int houseCost, int[] visitingCosts, String color){
        this.name = name;
        this.price = price;
        this.houseCost = houseCost;
        this.visitingCosts = visitingCosts;
        this.color = color;
        this.value = value;
        numHouses = 0;
        owned = false;
        owner = null;
    }

    public enum Level {
        HIGH,
        MEDIUM,
        LOW,
        NONE
    }

    public boolean isPurchasable(){
        return !name.startsWith("*") && !name.equals("GO") && !owned;
    }

    public boolean isOwned(){
        return owned;
    }

    public void boughtBy(Player player){
        player.changeBalance(-1 * price);
        owned = true;
        owner = player;
    }

    public void addHouse(){
        if(numHouses < 5){
            numHouses++;
        }
    }

    public String getName(){
        return name;
    }

    public String toString(){
        String result = name;
        if(value != Level.NONE){
            result += " - price: " + price + " - visitingcosts: {";
            for(int i = 0; i <= 5; i++){
                result += visitingCosts[i];
                if(i != 5){
                    result += ", ";
                }
            }
            result += "}";
        }
        return result;
    }

}
