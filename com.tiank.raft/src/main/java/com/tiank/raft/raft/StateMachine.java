package com.tiank.raft.raft;

import com.tiank.raft.game.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author Kuan Tian
 * 2019-05-11
 */
public class StateMachine implements Callable {
    private final String command;

    public StateMachine(String c) {
        command = c;
    }

    public Object call() throws Exception {
        String[] requestAsArray = new String[5];
        requestAsArray = command.split("\\|");
        File dir = new File("src/userFile");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] userFiles = dir.listFiles();
        String gameResult = "";
        // user register
        if (requestAsArray[0].equals("register")) {
            boolean exist = false;
            for (File userFile : userFiles) {
                String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                // if username had been occupied
                if (userFileName.equals(fileName)) {
                    exist = true;
                }
            }
            // if username is valid, create new userfile
            if (!exist) {
                User newUser = new User();
                newUser.setUsername(requestAsArray[1]);
                newUser.setPassword(requestAsArray[2]);
                newUser.setEmail(requestAsArray[3]);
                String newPath = "src/userFile" + "/" + requestAsArray[1] + ".dat";
                // create new user file
                try {
                    ObjectOutputStream outputFile = new ObjectOutputStream(new FileOutputStream(newPath));
                    outputFile.writeObject(newUser);
                    outputFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gameResult = "succ";
            } else {
                gameResult = "wrongUsername";
            }
        }
        // user login with username and password
        else if (requestAsArray[0].equals("login")) {
            boolean exist = false;
            for (File userFile : userFiles) {
                // find user file
                String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                if (userFileName.equals(fileName)) {
                    exist = true;
                    String userFilePath = userFile.getPath();
                    try {
                        ObjectInputStream inputFile = new ObjectInputStream(new FileInputStream(userFilePath));
                        User user = (User) inputFile.readObject();
                        inputFile.close();
                        if (requestAsArray[2].equals(user.getPassword())) {
                            int balance = user.getBalance();
                            gameResult = "succ|" + balance;
                        } else {
                            gameResult = "wrongPassword";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!exist) {
                gameResult = "wrongUsername";
            }
        }
        // 'deal', 'hit', 'stand' or 'double'
        else {
            String userFilePath = "";
            User user = new User();
            for (File userFile : userFiles) {
                // find user file
                String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                if (userFileName.equals(fileName)) {
                    userFilePath = userFile.getPath();
                }
            }
            try {
                ObjectInputStream inputFile = new ObjectInputStream(new FileInputStream(userFilePath));
                user = (User) inputFile.readObject();
                inputFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // if request is "deal", deal 2 cards to both sides
            if (requestAsArray[0].equals("deal")) {
                user.resetDeck();
                user.clearPlayerCards();
                user.clearDealerCards();
                user.setInGameState(true);
                String playerCard_1 = user.dealCards();
                user.addPlayerCards(playerCard_1);
                String playerCard_2 = user.dealCards();
                user.addPlayerCards(playerCard_2);
                String dealerCard_1 = user.dealCards();
                user.addDealerCards(dealerCard_1);
                String dealerCard_2 = user.dealCards();
                user.addDealerCards(dealerCard_2);
                int playerSum = user.sumPlayerCards();
                int dealerSum = user.sumDealerCards();
                int dealerHideSum = user.dealerHide();
                int bidding = Integer.parseInt(requestAsArray[2]);
                user.setBidding(bidding);
                int balance = user.getBalance() - bidding;
                user.setBalance(balance);
                gameResult = playerCard_1 + "|" + playerCard_2 + "|" + dealerCard_1 + "|" + dealerCard_2 + "|"
                        + playerSum + "|" + dealerSum + "|" + dealerHideSum;
                // player gets a blackjack while dealer doesn't, player gets double paid
                if (playerSum == 21 && dealerSum != 21) {
                    user.setInGameState(false);
                    user.resetDeck();
                    user.setBalance(user.getBalance() + 3 * user.getBidding());
                    user.setBidding(0);
                }
                // both player and dealer get blackjack, tie game
                else if (playerSum == 21) {
                    user.setInGameState(false);
                    user.resetDeck();
                    user.setBalance(user.getBalance() + user.getBidding());
                    user.setBidding(0);
                }
                // dealer gets a blackjack while player doesn't
                else if (dealerSum == 21) {
                    user.setInGameState(false);
                    user.resetDeck();
                    if (user.getBalance() > user.getBidding()) {
                        user.setBalance(user.getBalance() - user.getBidding());
                    } else {
                        user.setBalance(0);
                    }
                    user.setBidding(0);
                }
            }
            // if request is "hit", player draws a card
            else if (requestAsArray[0].equals("hit")) {
                String addCard = user.dealCards();
                user.addPlayerCards(addCard);
                int playerSum = user.sumPlayerCards();
                int dealerSum = user.sumDealerCards();
                int count = user.countPlayerCards();
                // player busts
                if (playerSum > 21) {
                    user.setInGameState(false);
                    user.setBidding(0);
                    user.resetDeck();
                    ArrayList<String> dealerCards = user.getDealerCards();
                    String dealerCard_1 = dealerCards.get(0);
                    gameResult = addCard + "|" + playerSum + "|" + count + "|" + dealerSum + "|" + dealerCard_1;
                } else {
                    if (count < 6) {
                        gameResult = addCard + "|" + playerSum + "|" + count;
                    }
                    // player has 6 cards, win!
                    else {
                        user.setInGameState(false);
                        user.setBalance(user.getBalance() + 2 * user.getBidding());
                        user.setBidding(0);
                        user.resetDeck();
                        ArrayList<String> dealerCards = user.getDealerCards();
                        String dealerCard_1 = dealerCards.get(0);
                        gameResult = addCard + "|" + playerSum + "|" + count + "|" + dealerSum + "|" + dealerCard_1;
                    }
                }
            }
            // if request is "stand". Showdown! Dealer must stand on 17 and draw to 16
            else if (requestAsArray[0].equals("stand")) {
                int playerSum = user.sumPlayerCards();
                int dealerSum = user.sumDealerCards();
                int bidding = user.getBidding();
                int balance = user.getBalance();
                int count = 2;
                while (dealerSum < 17 && count < 6) {
                    String addedCard = user.dealCards();
                    user.addDealerCards(addedCard);
                    dealerSum = user.sumDealerCards();
                    count += 1;
                }
                // dealer busts
                if (dealerSum > 21) {
                    user.setBalance(balance + 2 * bidding);
                } else {
                    // dealer has 6 cards, player wins!
                    if (count == 6) {
                        user.setBalance(balance + 2 * bidding);
                    } else {
                        if (dealerSum > playerSum) {
                        } else if (dealerSum == playerSum) {
                            user.setBalance(balance + bidding);
                        } else {
                            user.setBalance(balance + 2 * bidding);
                        }
                    }
                }
                ArrayList<String> dealerCards = user.getDealerCards();
                StringBuilder cardsStr = new StringBuilder();
                for (String dealerCard : dealerCards) {
                    cardsStr.append("|").append(dealerCard);
                }
                gameResult = playerSum + "|" + dealerSum + "|" + count + cardsStr;
                user.setInGameState(false);
                user.setBidding(0);
                user.resetDeck();
            }
            // if request is "double", player doubles the bidding and gets 1 more cards.
            // Showdown!
            else if (requestAsArray[0].equals("double")) {
                int balance = user.getBalance() - user.getBidding();
                int bidding = user.getBidding() * 2;
                user.setBalance(balance);
                user.setBidding(bidding);
                String playerAddCard = user.dealCards();
                user.addPlayerCards(playerAddCard);
                int playerSum = user.sumPlayerCards();
                int dealerSum = user.sumDealerCards();
                int count = 2;
                // player busts
                if (playerSum > 21) {
                    String dealerCard_1 = user.getDealerCards().get(0);
                    gameResult = playerAddCard + "|" + playerSum + "|" + dealerSum + "|" + count + "|" + dealerCard_1;
                } else {
                    while (dealerSum < 17 && count < 6) {
                        String addedCard = user.dealCards();
                        user.addDealerCards(addedCard);
                        dealerSum = user.sumDealerCards();
                        count += 1;
                    }
                    // dealer busts
                    if (dealerSum > 21) {
                        user.setBalance(balance + 2 * bidding);
                    } else {
                        // dealer has 6 cards, player wins!
                        if (count == 6) {
                            user.setBalance(balance + 2 * bidding);
                        } else {
                            if (dealerSum > playerSum) {
                            } else if (dealerSum == playerSum) {
                                user.setBalance(balance + bidding);
                            } else {
                                user.setBalance(balance + 2 * bidding);
                            }
                        }
                    }
                    ArrayList<String> dealerCards = user.getDealerCards();
                    String cardsStr = "";
                    for (String dealerCard : dealerCards) {
                        cardsStr = cardsStr + "|" + dealerCard;
                    }
                    gameResult = playerAddCard + "|" + playerSum + "|" + dealerSum + "|" + count + cardsStr;
                }
                user.setInGameState(false);
                user.setBidding(0);
                user.resetDeck();
            }
            // wirtes changes to user file
            try {
                ObjectOutputStream outputFile = new ObjectOutputStream(new FileOutputStream(userFilePath));
                outputFile.writeObject(user);
                outputFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gameResult;
    }
}