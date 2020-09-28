package banking;

public class CreditCard {
    private String numberCard;
    private int balance;
    private String pinCode;

    public CreditCard(String numberCard, String pinCode, int balance) {
        this.numberCard = numberCard;
        this.pinCode = pinCode;
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }
    public  String getNumberCard() {
        return  numberCard;
    }
    public String getPinCode() {
        return pinCode;
    }
    public static boolean isValid(String numberCard){
        char[] tempArr = numberCard.toCharArray();
        int[] myArr = new int[16];
        int chekSum = 0;
        for(int i = 0; i< myArr.length; i++) {
            myArr[i] = Character.getNumericValue(tempArr[i]);
        }
        for (int i = 0; i < myArr.length - 1; i++) {
            if ((i + 1) % 2 == 1) {
                myArr[i] *= 2;
            }
            if (myArr[i] > 9) {
                myArr[i] -= 9;
            }
            chekSum += myArr[i];

        }
        if ((chekSum + myArr[myArr.length - 1])% 10 == 0) {
            return true;
        } else {
            return false;
        }
    }

}
