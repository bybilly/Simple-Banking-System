package banking;

import java.util.Random;

class Card {
    private final String number;
    private final String pin;
    private int balance;

    public Card(String number, String pin) {
        this.number = number;
        this.pin = pin;
    }

    public String getNumber() {
        return number;
    }

    public String getPin() {
        return pin;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public static int generatePin(int length) {
        Random ran = new Random();
        return ran.nextInt((int) Math.pow(10, length - 1));
    }

    public static String generateNumber(String branch, int numberAccounts) {
        String tempCardNo = branch + String.format("%09d", numberAccounts);
        int sum = luhnRemainder(tempCardNo);
        int additionalDigit = (sum % 10 == 0) ? 0 : 10 - (sum % 10);
        return tempCardNo + additionalDigit;
    }

    public static int luhnRemainder(String input) {
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            int currentChar = Character.getNumericValue(input.charAt(i));
            if (i % 2 == 0) currentChar *= 2;
            if (currentChar > 9) currentChar -= 9;
            sum += currentChar;
        }
        return sum % 10;
    }
}