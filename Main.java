package banking;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static int numberAccounts = 0;
    static String fileName;

    public static void main(String[] args) {
        fileName = args[1];
        createNewDatabase();
        Scanner scanner = new Scanner(System.in);
        String option;
        boolean terminated = false;
        boolean loggedIn = false;
        Card loggedInAcc = null;

        while (!terminated) {
            if (!loggedIn) {
                do {
                    displayMenu(1);
                    option = scanner.nextLine();
                } while (checkChoice(option) == -1 || checkChoice(option) > 2);

                int choice = checkChoice(option);

                switch (choice) {
                    case 0:
                        System.out.println();
                        System.out.println("Bye!");
                        terminated = true;
                        break;
                    case 1:
                        System.out.println();
                        Card newCard = createCard();
                        addToDatabase(newCard);
                        System.out.println("Your card has been created");
                        System.out.println("Your card number:");
                        System.out.println(newCard.getNumber());
                        System.out.println("Your card pin:");
                        System.out.println(newCard.getPin());
                        System.out.println();
                        break;
                    case 2:
                        System.out.println();
                        System.out.println("Enter your card number:");
                        String cardNumber = scanner.nextLine();
                        System.out.println("Enter your PIN:");
                        String pin = scanner.nextLine();
                        System.out.println();
                        loggedIn = checkDetails(cardNumber, pin);
                        if (loggedIn) {
                            System.out.println("You have successfully logged in!");
                            loggedInAcc = new Card(cardNumber, pin);
                            int bal = getBalance(cardNumber);
                            loggedInAcc.setBalance(bal);
                        } else {
                            System.out.println("Wrong card number or PIN!");
                        }
                        System.out.println();
                        break;
                }
            } else {
                do {
                    displayMenu(2);
                    option = scanner.nextLine();
                } while (checkChoice(option) == -1);

                int choice = checkChoice(option);

                switch (choice) {
                    case 0:
                        System.out.println();
                        System.out.println("Bye!");
                        terminated = true;
                        break;
                    case 1:
                        System.out.println();
                        int balance = loggedInAcc.getBalance();
                        System.out.println("Balance: " + balance);
                        System.out.println();
                        break;
                    case 2:
                        System.out.println();
                        System.out.println("Enter income:");
                        int income = Integer.parseInt(scanner.nextLine());
                        loggedInAcc.setBalance(income + loggedInAcc.getBalance());
                        addIncome(loggedInAcc.getNumber(), income);
                        System.out.println("Income was added!");
                        System.out.println();
                        break;
                    case 3:
                        System.out.println();
                        System.out.println("Transfer");
                        System.out.println("Enter card number:");
                        String cardNumber = scanner.nextLine();
                        if (cardNumber.equals(loggedInAcc.getNumber())) {
                            System.out.println("You can't transfer money to the same account!");
                            System.out.println();
                            break;
                        }
                        if (Card.luhnRemainder(cardNumber) != 0) {
                            System.out.println("Probably you made a mistake in the card number. Please try again!");
                            System.out.println();
                            break;
                        }
                        if (!cardExists(cardNumber)) {
                            System.out.println("Such a card does not exist.");
                            System.out.println();
                            break;
                        }
                        System.out.println("Enter how much money you want to transfer:");
                        int moneyToTransfer = Integer.parseInt(scanner.nextLine());
                        if (moneyToTransfer > loggedInAcc.getBalance()) {
                            System.out.println("Not enough money!");
                            System.out.println();
                            break;
                        }
                        addIncome(cardNumber, moneyToTransfer);
                        addIncome(loggedInAcc.getNumber(), -moneyToTransfer);
                        loggedInAcc.setBalance(loggedInAcc.getBalance() - moneyToTransfer);
                        System.out.println("Success!");
                        System.out.println();
                        break;
                    case 4:
                        System.out.println();
                        deleteAccount(loggedInAcc.getNumber());
                        loggedIn = false;
                        System.out.println("The account has been closed!");
                        System.out.println();
                        break;
                    case 5:
                        System.out.println();
                        loggedIn = false;
                        System.out.println("You have successfully logged out!");
                        System.out.println();
                        break;
                }
            }
        }
    }

    public static void createNewDatabase() {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                try (Statement statement = conn.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS card ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "number VARCHAR(16),"
                    + "pin VARCHAR(4),"
                    + "balance INT DEFAULT 0);");
                    ResultSet rs = statement.executeQuery("SELECT id FROM card ORDER BY id DESC;");
                    if (rs.next()) {
                        numberAccounts = rs.getInt(1);
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addToDatabase(Card card) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String sql = "INSERT INTO card (number, pin) VALUES (?, ?);";
                try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                    String number = card.getNumber();
                    String pin = card.getPin();
                    pstat.setString(1, number);
                    pstat.setString(2, pin);
                    pstat.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteAccount(String card) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "DELETE FROM card WHERE number = ?";
            try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                pstat.setString(1, card);
                pstat.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addIncome(String card, int income) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "UPDATE card SET balance = balance + ? WHERE number = ?;";
            try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                pstat.setInt(1, income);
                pstat.setString(2, card);
                pstat.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Card createCard() {
        int pin = Card.generatePin(4);
        String cardNumber = Card.generateNumber("400000", ++numberAccounts);
        String newPin = String.format("%04d", pin);
        return new Card(cardNumber, newPin);
    }

    public static int checkChoice(String option) {
        switch (option) {
            case "1":
                return 1;
            case "2":
                return 2;
            case "3":
                return 3;
            case "4":
                return 4;
            case "5":
                return 5;
            case "0":
                return 0;
            default:
                return -1;
        }
    }

    public static boolean checkDetails(String cardNumber, String pin) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String sql = "SELECT * FROM card WHERE number = ? AND pin = ?;";
                try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                    pstat.setString(1, cardNumber);
                    pstat.setString(2, pin);
                    return (pstat.executeQuery().next());
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static int getBalance(String cardNumber) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String sql = "SELECT * FROM card WHERE number = ?;";
                try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                    pstat.setString(1, cardNumber);
                    ResultSet rs = pstat.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("balance");
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static boolean cardExists(String cardNumber) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * FROM card WHERE number = ?;";
            try (PreparedStatement pstat = conn.prepareStatement(sql)) {
                pstat.setString(1, cardNumber);
                return (pstat.executeQuery().next());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static void displayMenu(int menu) {
        if (menu == 1) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
        } else {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
        }
    }
}