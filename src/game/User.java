package game;

/**
  * @author Kuan Tian
  * 2019-05-11
  */

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable
{
    private String username;
    private String password;
    private String email;
    private int balance;
    private boolean online;
    private boolean inGame;
    private ArrayList<String> deck;
    private ArrayList<String> dealerCards;
    private ArrayList<String> playerCards;
    private int bidding;
    
    //initialize the deck, set balance to 10000
    public User() 
    {
    	dealerCards = new ArrayList<String>();
    	playerCards = new ArrayList<String>();
    	deck = new ArrayList<String>();
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("heart" + "_" + i);
    	}
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("spade" + "_" + i);
    	}

    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("diamond" + "_" + i);
    	}
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("club" + "_" + i);
    	}
    	
    	this.balance = 10000;
    	this.online = false;
    	this.inGame = false;
    }
    
    //reset 52 cards
    public void resetDeck()
    {
    	deck.clear();
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("heart" + "_" + i);
    	}
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("spade" + "_" + i);
    	}

    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("diamond" + "_" + i);
    	}
    	
    	for(int i = 1; i <= 13; i++)
    	{
    		deck.add("club" + "_" + i);
    	}
    }
    
    public void setUsername(String username)
    {
    	this.username = username;
    }
    
    public void setPassword(String password)
    {
    	this.password = password;
    }
    
    public void setEmail(String email)
    {
    	this.email = email;
    }
    
    public void setBalance(int balance)
    {
    	this.balance = balance;
    }
    
    public void setOnlineState(boolean state)
    {
    	this.online = state;
    }
    
    public void setInGameState(boolean state)
    {
    	this.inGame = state;
    }
    
    public void setBidding(int bidding)
    {
    	this.bidding = bidding;
    }   
    
    public void addDealerCards(String card)
    {
    	if(this.dealerCards.isEmpty())
    	{
    		this.dealerCards = new ArrayList<String>();
    	}
    	this.dealerCards.add(card);
    }
    
    public void addPlayerCards(String card)
    {
    	if(this.playerCards.isEmpty())
    	{
    		this.playerCards = new ArrayList<String>();
    	}
    	this.playerCards.add(card);
    }
    
    public void clearDealerCards()
    {
    	this.dealerCards.clear();
    }
    
    public void clearPlayerCards()
    {
    	this.playerCards.clear();
    }
    
    public int countPlayerCards()
    {
    	int count = this.playerCards.size();
    	return count;
    }
    
    public int countDealerCards()
    {
    	int count = this.dealerCards.size();
    	return count;
    }
    
    public int sumPlayerCards()
    {
    	int sum = 0;
    	for(String playerCard: playerCards)
		{
			String[] cardAsArray = playerCard.split("_");               				
			int value = Integer.parseInt(cardAsArray[1]);
			//Ace = 1, 2-9 = 2-9, 10 J Q K = 10
			if(value == 1)
			{	
				sum += 1;
			}
			else if(value >= 10)
			{
				sum += 10;
			}
			else
			{
				sum += value;
			}
		}
    	//if Ace appears in the first 2 cards, Ace = 11
    	String card1 = playerCards.get(0);
    	String[] card1AsArray = card1.split("_");
    	int value1 = Integer.parseInt(card1AsArray[1]);
    	if(value1 == 1)
    	{
    		sum += 10;
    	}
    	String card2 = playerCards.get(1);
    	String[] card2AsArray = card2.split("_");
    	int value2 = Integer.parseInt(card2AsArray[1]);
    	if(value2 == 1)
    	{
    		sum += 10;
    	}
    	//if there is a pair of Aces in the first 2 cards, they make 12
    	if(value1 == 1 && value2 == 1)
    	{
    		sum -= 10;
    	}
    	return sum;
    }
    
    public int sumDealerCards()
    {
    	int sum = 0;
    	for(String dealerCard: dealerCards)
		{
			String[] cardAsArray = dealerCard.split("_");               				
			int value = Integer.parseInt(cardAsArray[1]);
			//Ace = 1, 2-9 = 2-9, 10 J Q K = 10
			if(value == 1)
			{
				sum += 1;
			}
			else if(value >= 10)
			{
				sum += 10;
			}
			else
			{
				sum += value;
			}
		}
    	//if Ace appears in the first 2 cards, Ace = 11
    	String card1 = dealerCards.get(0);
    	String[] card1AsArray = card1.split("_");
    	int value1 = Integer.parseInt(card1AsArray[1]);
    	if(value1 == 1)
    	{
    		sum += 10;
    	}
    	String card2 = dealerCards.get(1);
    	String[] card2AsArray = card2.split("_");
    	int value2 = Integer.parseInt(card2AsArray[1]);
    	if(value2 == 1)
    	{
    		sum += 10;
    	}
    	//if there is a pair of Aces in the first 2 cards, they make 12
    	if(value1 == 1 && value2 == 1)
    	{
    		sum -= 10;
    	}
    	return sum;
    }
    
    public ArrayList<String> getDealerCards()
    {
    	return this.dealerCards;
    }
    
    public ArrayList<String> getPlayerCards()
    {
    	return this.playerCards;
    }
    
    public String getUsername()
    {
    	return this.username;
    }
    
    public String getPassword()
    {
    	return this.password;
    }
    
    public String getEmail()
    {
    	return this.email;
    }
    
    public int getBalance()
    {
    	return this.balance;
    }
    
    public boolean getOnlineState()
    {
    	return this.online;
    }
    
    public boolean getInGameState()
    {
    	return this.inGame;
    }
    
    public int getBidding()
    {
    	return this.bidding;
    } 
    
    public ArrayList<String> getDeck()
    {
    	return this.deck;
    }
    
    public String dealCards()
    {
    	int index = (int) (Math.random()* this.deck.size());
		String card = deck.get(index);
		this.deck.remove(index);
		return card;
    }
       
}