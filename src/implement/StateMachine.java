package implement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import game.User;

/**
  * @author Kuan Tian
  * 2019-05-11
  */

public class StateMachine implements Callable {
	private String command;
	
	public StateMachine(String c) {
		command = c;
	}
	
	public Object call() throws Exception{
		String[] requestAsArray = new String[5];
		requestAsArray = command.split("\\|");
		File dir = new File("d:\\");
	    File[] userFiles = dir.listFiles();
		String gameResult = "";
		//user register
        if(requestAsArray[0].equals("register"))
        {
            boolean exist = false;
            for(File userFile: userFiles)
            {
                String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                //if username had been occupied
                if(userFileName.equals(fileName))
                {
                    exist = true;
                }
            }
            if(!exist)
            {
            	User newUser = new User();
                newUser.setUsername(requestAsArray[1]);
                newUser.setPassword(requestAsArray[2]);
                newUser.setEmail(requestAsArray[3]);
                String newPath = "d:\\"+"\\"+requestAsArray[1]+".dat";
                //create new user file
                try
                {
                    ObjectOutputStream outputFile = new ObjectOutputStream(new FileOutputStream(newPath));
                    outputFile.writeObject(newUser);
                    outputFile.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace(); 
                }
                gameResult = "succ";
            }
            else
            {
            	gameResult = "wrongUsername";
            }
        }
        //user login
        else if(requestAsArray[0].equals("login"))
        {
        	boolean exist = false;
        	for(File userFile: userFiles)
            {
                String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                if(userFileName.equals(fileName))
                {
                	exist = true;
                	String userFilePath = userFile.getPath();
                	try
                	{
                		ObjectInputStream inputFile = new ObjectInputStream(new FileInputStream(userFilePath));
	                    User user = (User)inputFile.readObject();
	                    inputFile.close();
	                    if(requestAsArray[2].equals(user.getPassword()))
            			{
            				int balance = user.getBalance();
            				gameResult = "succ|" + balance;
            			}
	                    else
            			{
	                    	gameResult = "wrongPassword";
            			}
                	}
	                catch(Exception e)
	                {
	                    e.printStackTrace();
	                }	
                }
            }
        	if(!exist)
        	{
        		gameResult = "wrongUsername";
        	}
        }
        //'deal', 'hit', 'stand' or 'double'
        else
        {
        	String userFilePath = "";
        	User user = new User();
        	for(File userFile: userFiles)
            {
                //find user file
            	String fileName = userFile.getName();
                String userFileName = requestAsArray[1] + ".dat";
                if(userFileName.equals(fileName))
                {
                	userFilePath = userFile.getPath();
                }
            }
        	try
        	{
        		ObjectInputStream inputFile = new ObjectInputStream(new FileInputStream(userFilePath));
                user = (User)inputFile.readObject();
                inputFile.close();
        	}
        	catch(Exception e)
            {
                e.printStackTrace();
            }
        	//press 'deal'. Start game, deal 2 cards to both sides
            if(requestAsArray[0].equals("deal"))
            {
                user.resetDeck();
                user.clearPlayerCards();
                user.clearDealerCards();
                user.setInGameState(true);
                //randomly deal 2 cards to both sides	                			
                String playerCard_1 = user.dealCards();
                user.addPlayerCards(playerCard_1);
                String playerCard_2 = user.dealCards();
                user.addPlayerCards(playerCard_2);	                			
                String dealerCard_1 = user.dealCards();
                user.addDealerCards(dealerCard_1);
                String dealerCard_2 = user.dealCards();
                user.addDealerCards(dealerCard_2);
                //return the sum of player's cards
                int playerSum = user.sumPlayerCards();
                int bidding = Integer.parseInt(requestAsArray[2]);
        		user.setBidding(bidding);
        		int balance = user.getBalance() - bidding; 
        		user.setBalance(balance);
        		gameResult = playerCard_1+"|"+playerCard_2+"|"+dealerCard_2+"|"+playerSum;
            }
            //Press 'hit', player draws a card.
            else if(requestAsArray[0].equals("hit"))
            {
            	String card = user.dealCards();
    			user.addPlayerCards(card);
    			int sum = user.sumPlayerCards();
    			int count = user.countPlayerCards();
    			if(sum > 21)
    			{
    				user.setInGameState(false);
    				ArrayList<String> dealerCards = user.getDealerCards();
    				String dealerCard_1 = dealerCards.get(0);
    				gameResult = card + "|" + sum + "|" + count + "|" + dealerCard_1;
    			}
    			else
    			{
    				if(count < 6)
    				{
    					gameResult = card + "|" + sum + "|" + count;
    				}
    				else
    				{
    					//6 charlie! Player wins.
    					user.setInGameState(false);
    					int bidding = user.getBidding();
    	            	int balance = user.getBalance();
    	            	user.setBalance(balance + 2*bidding);
    	            	int newBalance = user.getBalance();
        				ArrayList<String> dealerCards = user.getDealerCards();
        				String dealerCard_1 = dealerCards.get(0);
        				gameResult = card + "|" + sum + "|" + count + "|" + dealerCard_1 + "|" + newBalance;
    				}
    			}
            }
            //Press 'stand', showdown.
            else if(requestAsArray[0].equals("stand"))
            {
            	int playerSum = user.sumPlayerCards();
            	int dealerSum = user.sumDealerCards();
            	int bidding = user.getBidding();
            	int balance = user.getBalance();
            	ArrayList<String> dealerCards = user.getDealerCards();
            	String dealerCard1 = dealerCards.get(0);
            	//if dealer overwhelms player with 2 cards, dealer wins
            	if(dealerSum > playerSum)
    			{
            		gameResult = "lose" + "|" + dealerCard1;
    			}
            	//if dealer matches player with 2 cards
            	else if (dealerSum == playerSum)
            	{
            		//if sum of dealer's cards was smaller than 14, ask for 1 more card, dealer has 3 cards now
    				if(dealerSum <= 13)
    				{
    					String addedCard_1 = user.dealCards();
        				user.addDealerCards(addedCard_1);
        				dealerSum = user.sumDealerCards();
        				//dealer busts with 3 cards, player wins
        				if(dealerSum > 21)
        				{
        					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1;
        					user.setBalance(balance + 2*bidding);
        				}
        				//dealer doesn't bust with 3 cards, dealer wins
        				else
        				{
        					gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1;
        				}
    				}
    				//if sum of dealer's cards was larger than 14, tie game
    				else
    				{
    					gameResult = "tie" + "|" + dealerCard1;
    					user.setBalance(balance + bidding);
    				}
            	}
            	//if player overwhelms dealer, dealer asks for 1 more card, dealer has 3 cards now
            	else
            	{
            		String addedCard_1 = user.dealCards();
    				user.addDealerCards(addedCard_1);
    				dealerSum = user.sumDealerCards();
    				//dealer busts with 3 cards, player wins
    				if(dealerSum > 21)
    				{
    					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1;
    					user.setBalance(balance + 2*bidding);
    				}
    				//dealer doesn't bust with 3 cards
    				else
    				{
    					//dealer overwhelms player with 3 cards, dealer wins
    					if(dealerSum > playerSum)
    					{
    						gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1;
    					}
    					//if dealer matches player with 3 cards
    					else if(dealerSum == playerSum)
    					{
    						//dealer asks for the 4th card
    						if(dealerSum <= 13)
            				{
            					String addedCard_2 = user.dealCards();
                				user.addDealerCards(addedCard_2);
                				dealerSum = user.sumDealerCards();
                				//dealer busts with 4 cards, player wins
                				if(dealerSum > 21)
                				{
                					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2;
                					user.setBalance(balance + 2*bidding);
                				}
                				//dealer dosen't bust with 4 cards, dealer wins
                				else
                				{
                					gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2;
                				}
            				}
    						//tie game
            				else
            				{
            					gameResult = "tie" + "|" + dealerCard1 + "|" + addedCard_1;
            					user.setBalance(balance + bidding);
            				}
    					}
    					//player still overwhelms dealer, dealer asks for the 4th card
    					else
    					{
    						String addedCard_2 = user.dealCards();
            				user.addDealerCards(addedCard_2);
            				dealerSum = user.sumDealerCards();
            				//dealer busts, player wins
            				if(dealerSum > 21)
            				{
            					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2;
            					user.setBalance(balance + 2*bidding);
            				}
            				//dealer doesn't bust
            				else
            				{
            					//dealer overwhelms player with 4 cards, dealer wins
            					if(dealerSum > playerSum)
            					{
            						gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2;
            					}
            					//dealer matches player with 4 cards
            					else if(dealerSum == playerSum)
            					{
            						//dealer asks for the 5th card
            						if(dealerSum <= 13)
            						{
            							String addedCard_3 = user.dealCards();
                        				user.addDealerCards(addedCard_3);
                        				dealerSum = user.sumDealerCards();
                        				//dealer busts with 5 cards, player wins
                        				if(dealerSum > 21)
                        				{
                        					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        					user.setBalance(balance + 2*bidding);
                        				}
                        				//dealer doesn't bust with 5 cards, dealer wins
                        				else
                        				{
                        					gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        				}
            						}
            						//tie game
            						else
            						{
            							gameResult = "tie" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2;
            							user.setBalance(balance + bidding);
            						}
            					}
            					//player still overwhelms dealer, dealer asks for the 5th card
            					else
            					{
            						String addedCard_3 = user.dealCards();
                    				user.addDealerCards(addedCard_3);
                    				dealerSum = user.sumDealerCards();
                    				//dealer bust with 5 cards, player wins
                    				if(dealerSum > 21)
                    				{
                    					gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                    					user.setBalance(balance + 2*bidding);
                    				}
                    				//dealer doesn't bust with 5 cards
                    				else
                    				{ 
                    					if(dealerSum > playerSum)
                    					{
                    						gameResult = "lose" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                    					}
                    					else if(dealerSum == playerSum)
                    					{
                    						gameResult = "tie" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                    						user.setBalance(balance + bidding);
                    					}
                    					else
                    					{
                    						gameResult = "win" + "|" + dealerCard1 + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                    						user.setBalance(balance + 2*bidding);
                    					}
                    				}
            					}
            				}
    					}
    				}
            	}
            	user.setInGameState(false);
            	user.resetDeck();
            }
            //press 'double', player gets 1 more cards, then showdown
            else if(requestAsArray[0].equals("double"))
            {
            	int bidding = user.getBidding() * 2;
            	int balance = user.getBalance() - user.getBidding();
            	user.setBalance(balance);
            	user.setBidding(bidding);
            	String playerAddCard = user.dealCards();
            	user.addPlayerCards(playerAddCard);
            	int playerSum = user.sumPlayerCards();
            	int dealerSum = user.sumDealerCards();
            	//player busts, dealer wins
            	if(playerSum > 21)
            	{
            		gameResult = "lose" + "|" + playerAddCard;
            	}
            	//player doesn't bust, dealer follows the same strategy as 'stand'
            	else
            	{
            		//if dealer overwhelms player with 2 cards, dealer wins
	            	if(dealerSum > playerSum)
        			{
	            		gameResult = "lose" + "|" + playerAddCard;
        			}
	            	//if dealer matches player with 2 cards
	            	else if (dealerSum == playerSum)
	            	{
	            		//if sum of dealer's cards was smaller than 14, ask for 1 more card, dealer has 3 cards now
        				if(dealerSum <= 13)
        				{
        					String addedCard_1 = user.dealCards();
            				user.addDealerCards(addedCard_1);
            				dealerSum = user.sumDealerCards();
            				//dealer busts with 3 cards, player wins
            				if(dealerSum > 21)
            				{
            					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1;
            					user.setBalance(balance + 2*bidding);
            				}
            				//dealer doesn't bust with 3 cards, dealer wins
            				else
            				{
            					gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1;
            				}
        				}
        				//if sum of dealer's cards was larger than 14, tie game
        				else
        				{
        					gameResult = "tie";
        					user.setBalance(balance + bidding);
        				}
	            	}
	            	//if player overwhelms dealer, dealer asks for 1 more card, dealer has 3 cards now
	            	else
	            	{
	            		String addedCard_1 = user.dealCards();
        				user.addDealerCards(addedCard_1);
        				dealerSum = user.sumDealerCards();
        				//dealer busts with 3 cards, player wins
        				if(dealerSum > 21)
        				{
        					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1;
        					user.setBalance(balance + 2*bidding);
        				}
        				//dealer doesn't bust with 3 cards
        				else
        				{
        					//dealer overwhelms player with 3 cards, dealer wins
        					if(dealerSum > playerSum)
        					{
        						gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1;
        					}
        					//if dealer matches player with 3 cards
        					else if(dealerSum == playerSum)
        					{
        						//dealer asks for the 4th card
        						if(dealerSum <= 13)
                				{
                					String addedCard_2 = user.dealCards();
                    				user.addDealerCards(addedCard_2);
                    				dealerSum = user.sumDealerCards();
                    				//dealer busts with 4 cards, player wins
                    				if(dealerSum > 21)
                    				{
                    					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2;
                    					user.setBalance(balance + 2*bidding);
                    				}
                    				//dealer dosen't bust with 4 cards, dealer wins
                    				else
                    				{
                    					gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2;
                    				}
                				}
        						//tie game
                				else
                				{
                					gameResult = "tie" + "|" + playerAddCard + "|" + addedCard_1;
                					user.setBalance(balance + bidding);
                				}
        					}
        					//player still overwhelms dealer, dealer asks for the 4th card
        					else
        					{
        						String addedCard_2 = user.dealCards();
                				user.addDealerCards(addedCard_2);
                				dealerSum = user.sumDealerCards();
                				//dealer busts, player wins
                				if(dealerSum > 21)
                				{
                					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2;
                					user.setBalance(balance + 2*bidding);
                				}
                				//dealer doesn't bust
                				else
                				{
                					//dealer overwhelms player with 4 cards, dealer wins
                					if(dealerSum > playerSum)
                					{
                						gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2;
                					}
                					//dealer matches player with 4 cards
                					else if(dealerSum == playerSum)
                					{
                						//dealer asks for the 5th card
                						if(dealerSum <= 13)
                						{
                							String addedCard_3 = user.dealCards();
                            				user.addDealerCards(addedCard_3);
                            				dealerSum = user.sumDealerCards();
                            				//dealer busts with 5 cards, player wins
                            				if(dealerSum > 21)
                            				{
                            					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                            					user.setBalance(balance + 2*bidding);
                            				}
                            				//dealer doesn't bust with 5 cards, dealer wins
                            				else
                            				{
                            					gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                            				}
                						}
                						//tie game
                						else
                						{
                							gameResult = "tie" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2;
                							user.setBalance(balance + bidding);
                						}
                					}
                					//player still overwhelms dealer, dealer asks for the 5th card
                					else
                					{
                						String addedCard_3 = user.dealCards();
                        				user.addDealerCards(addedCard_3);
                        				dealerSum = user.sumDealerCards();
                        				//dealer bust with 5 cards, player wins
                        				if(dealerSum > 21)
                        				{
                        					gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        					user.setBalance(balance + 2*bidding);
                        				}
                        				//dealer doesn't bust with 5 cards
                        				else
                        				{ 
                        					if(dealerSum > playerSum)
                        					{
                        						gameResult = "lose" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        					}
                        					else if(dealerSum == playerSum)
                        					{
                        						gameResult = "tie" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        						user.setBalance(balance + bidding);
                        					}
                        					else
                        					{
                        						gameResult = "win" + "|" + playerAddCard + "|" + addedCard_1 + "|" + addedCard_2 + "|" + addedCard_3;
                        						user.setBalance(balance + 2*bidding);
                        					}
                        				}
                					}
                				}
        					}
        				}
	            	}
            	}
            	user.setInGameState(false);
            	user.resetDeck();
            }
            try
            {
                ObjectOutputStream outputFile = new ObjectOutputStream(new FileOutputStream(userFilePath));
                outputFile.writeObject(user);
                outputFile.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }  
		return gameResult;
		}
	}
