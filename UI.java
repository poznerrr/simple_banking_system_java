package banking;

import org.sqlite.SQLiteDataSource;
import java.sql.*;
import java.util.*;


public class UI {
    private boolean isWorking = true;
    CreditCard currentCard = null;
    private ArrayList<CreditCard> creditCards = new ArrayList<CreditCard>();
    Scanner scanner = new Scanner(System.in);
    SQLiteDataSource dataSource;

    
    public UI() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(Main.URL);
        try (Connection con = dataSource.getConnection()) {
            con.isValid(5);
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL," +
                        "balance INTEGER DEFAULT 0);");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean showMenu () {
        int chosen;
        System.out.println();
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");

        chosen = scanner.nextInt();
        if (chosen == 1) {
            createAccount();
        } else if (chosen == 2) {
            logAccount();
        } else if (chosen == 0) {
            System.out.println("\nBye!");
            isWorking = false;
        }
        return isWorking;
    }

    void createAccount() {

        byte[] IIN = new byte[]{4,0,0,0,0,0};
        byte[] creatorNumb = new byte[9];
        byte checkSum = 0;
        byte [] creatorPinCode = new byte[4];
        Random random = new Random();

        for (int i = 0; i < creatorNumb.length; i++) {
            creatorNumb[i] = (byte) random.nextInt(10);
        }
        //implementation Luhn algorithm
        byte[] temporaryArr = new byte[15];
        byte temporarySum = 0;

        for (int i = 0; i < IIN.length; i++) {
            temporaryArr[i] = IIN[i];
        }
        for (int i = 0; i < creatorNumb.length; i++) {
            temporaryArr[i + IIN.length] = creatorNumb[i];
        }

        for (int i = 0; i < temporaryArr.length; i++) {
            if ((i + 1) % 2 == 1) {
                temporaryArr[i] *= 2;
            }
        }

        for (int i = 0; i < temporaryArr.length; i++) {
            if (temporaryArr[i] > 9) {
                temporaryArr[i] -= 9;
            }
            temporarySum += temporaryArr[i];
        }

        for (byte i = 0 ; i < 10; i++) {
            if ((temporarySum + i) % 10 == 0) {
                checkSum = i;
            }
        }


        //create Pin Code
        for (int i = 0; i < creatorPinCode.length; i++) {
            creatorPinCode[i] = (byte) random.nextInt(10);
        }

        String numberCard = "";
        for (byte numb: IIN) {
            numberCard += numb;
        }
        for (byte numb : creatorNumb) {
            numberCard = numberCard + numb;
        }
        numberCard += checkSum;

        String pinCode = "";
        for (byte numb : creatorPinCode) {
            pinCode += numb;
        }
        // record to DATABASE
        boolean isExisting = false;
        try (Connection con = dataSource.getConnection()) {

            try (Statement statement = con.createStatement()) {
                String command = "SELECT * FROM Card WHERE number = " + numberCard + ";" ;
                try (ResultSet validateCards = statement.executeQuery(command)) {
                    if (validateCards.next()) {
                    isExisting = true;
                    }
                }
                    catch(Exception ex) {
                    System.out.println(ex.getMessage());
                    }


                if (!isExisting) {
                    command = "INSERT INTO Card (number, pin, balance) VALUES (" +
                            numberCard + ", " + pinCode + ", 0);";
                    statement.executeUpdate(command);
                    System.out.println("\nYour card has been created");
                    System.out.println("Your card number");
                    System.out.println(numberCard);
                    System.out.println("Your card PIN");
                    System.out.println(pinCode);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                isExisting = true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (isExisting) {
            createAccount();
        }




    }

    void logAccount() {
        System.out.println("\nEnter your card number:");
        String currentNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String currentPin = scanner.next();
        // work with DATABASE
        boolean isExisting = false;
        try (Connection con = dataSource.getConnection()) {

            try (Statement statement = con.createStatement()) {
                // Statement execution

                String command = "SELECT * FROM Card WHERE number = '" +
                        currentNumber + "' AND pin ='" + currentPin + "';";
                try (ResultSet validateCards = statement.executeQuery(command)) {
                    if (validateCards.next()) {
                        // Retrieve column values
                        String pin = validateCards.getString("pin");
                        String name = validateCards.getString("number");
                        int balance = validateCards.getInt("balance");
                        currentCard = new CreditCard(name, pin, balance);


                    } else {
                        System.out.println("\nWrong card number or PIN");
                        return;
                    }
                }
                System.out.println("\nYou have successfully logged in!");
                workWithAccount();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    void workWithAccount() {
        System.out.println("\n1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
        int answer = scanner.nextInt();
        if (answer == 1) {
            System.out.printf("\nBalance : %d\n", currentCard.getBalance());
            workWithAccount();
        } else if (answer == 2) {
            System.out.println("\nEnter income:");
            int moneyIn = scanner.nextInt();
            currentCard.setBalance(currentCard.getBalance() + moneyIn);
            try (Connection con = dataSource.getConnection()) {

                try (Statement statement = con.createStatement()) {
                    String command = "UPDATE Card SET balance = " + currentCard.getBalance() + " WHERE number = '" +
                            currentCard.getNumberCard() + "';" ;
                    if ( statement.executeUpdate(command) == 1) {
                        System.out.println("Income was added!");
                        workWithAccount();
                    }
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } else if (answer == 3) {
            System.out.println("\nTransfer");
            System.out.println("Enter card number:");
            String cardTo = scanner.next();
            if (cardTo.equals(currentCard.getNumberCard())) {
                System.out.println("You can't transfer money to the same account!");
                workWithAccount();
            } else {

                if (!CreditCard.isValid(cardTo)) {
                    System.out.println("You made mistake in the card number.");
                    System.out.println("Please try again!");
                    workWithAccount();
                } else {
                    try (Connection con = dataSource.getConnection()) {

                        try (Statement statement = con.createStatement()) {
                            String command = "SELECT * FROM Card WHERE number = " + cardTo + ";";
                            try (ResultSet validateCards = statement.executeQuery(command)) {
                                if (validateCards.next()) {
                                    int balanceFrom = validateCards.getInt("balance");
                                    System.out.println("Enter how much money you want to transfer:");
                                    int moneyTo = scanner.nextInt();
                                    if (moneyTo > currentCard.getBalance()) {
                                        System.out.println("Not enough money!");
                                        workWithAccount();
                                    } else {
                                        currentCard.setBalance(currentCard.getBalance() - moneyTo);
                                        moneyTo += balanceFrom;
                                        command = "UPDATE Card SET balance = " + currentCard.getBalance() + " WHERE number = '" +
                                                currentCard.getNumberCard() + "';";
                                        statement.executeUpdate(command);
                                        command = "UPDATE Card SET balance = " + moneyTo + " WHERE number = '" +
                                                cardTo + "';";
                                        statement.executeUpdate(command);
                                        System.out.println("Success!");
                                        workWithAccount();
                                    }

                                } else {
                                    System.out.println("Such a card does not exit");
                                    workWithAccount();
                                }

                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        } else if (answer ==4) {
            try (Connection con = dataSource.getConnection()) {

                try (Statement statement = con.createStatement()) {
                    String command = "DELETE FROM Card WHERE number = '" + currentCard.getNumberCard() + "';";
                    if (statement.executeUpdate(command) == 1) {
                        currentCard = null;
                        System.out.println("\nThe account has been closed!");
                    }
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }


        } else if (answer == 5) {
            currentCard = null;
            System.out.println("\nYou successfully logged out!");
        } else if (answer == 0) {
           System.out.println("\nBye!");
           isWorking = false;
        }
    }
}
