import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.Date; 

/*
 * This is my own version of how I expect Dealer to function based on what I know so far 
 * 
 * @author Mak1ma2 
 */

public class Dealer{
    
    static ServerSocket server;
    static int port = 9876;
    static DataInputStream dis; 
    static DataOutputStream dos; 
    static boolean status = true ; 
    static ArrayList< String > cards_name = new ArrayList<>(); 
    static ArrayList< Integer > cards_value = new ArrayList<>();
    static ArrayList< String > cards = new ArrayList<>(); 
    
    public static void main(String[]args) throws IOException, ClassNotFoundException{
        server = new ServerSocket(port);
        System.out.println("Waiting for client"); 
        Socket socket = server.accept();
        System.out.println("client accepted"); 
        try{ 
            TimeUnit.SECONDS.sleep(30);
        }
        catch( InterruptedException e ){
            e.printStackTrace();
        }
        int num = 500; 
        dos = new DataOutputStream(socket.getOutputStream()); 
        dis = new DataInputStream(socket.getInputStream());
        shuffle(); 
        dos.writeUTF( "login" ); 
        String line = dis.readUTF();
        String[]login = line.split(":"); // this works 
        System.out.println( login[1] + " joined!" ); // goes here fine 
        ArrayList< String > used_cards = new ArrayList<>();
        ArrayList< Integer > used_cards_values = new ArrayList<>(); 
        ArrayList< Integer > dealer_used_values = new ArrayList<>(); 
        String list_of_cards_used = ""; 
        while( num > 0 ){ 
            if( cards_name.size() < 10 ){ 
                shuffle();  
                list_of_cards_used = ""; 
            } 
            int initial_random = randomize(); 
            used_cards.add(cards_name.get(initial_random));  
            used_cards_values.add(cards_value.get(initial_random)); 
            list_of_cards_used += ":" + cards_name.get(initial_random); 
            cards_name.remove(initial_random); 
            cards_value.remove(initial_random); 
            int secondary_random = randomize();
            used_cards.add(cards_name.get(secondary_random)); 
            list_of_cards_used += ":" + cards_name.get(secondary_random); 
            used_cards_values.add(cards_value.get(secondary_random)); 
            cards_name.remove(secondary_random); 
            cards_value.remove(secondary_random);
            int dealer_initial = randomize();
            dealer_used_values.add(cards_value.get(dealer_initial));
            list_of_cards_used += ":" + cards_name.get(dealer_initial);
            cards_name.remove(dealer_initial); 
            cards_value.remove(dealer_initial); 
            int dealer_secondary = randomize();
            dealer_used_values.add(cards_value.get(dealer_secondary)); 
            list_of_cards_used += ":" + cards_name.get(dealer_secondary); 
            cards_name.remove(dealer_secondary); 
            cards_value.remove(dealer_secondary); 
            String used_card = "";
            if( !used_cards.isEmpty() ){
                for( int i = 0 ; i < used_cards.size() ; i++ ){ 
                    used_card += ":" + used_cards.get(i); 
                } 
            }
            dos.writeUTF("bet:"+num+":All"+list_of_cards_used); // everything went well until here 
            line = dis.readUTF(); // bet:339
            ArrayList< String > bet_str = new ArrayList<>(); 
            for( String bets : line.split( ":" )){ 
                bet_str.add( bets ); 
            }
            int amount_bet = Integer.parseInt(bet_str.get( 1 )); // reads the bet find meaning that up until login everything worked 
            num -= amount_bet; // num = 339 
            bet_str.clear();    
            int random = randomize(); 
            int dealer_random = randomize(); 
            String list_of_cards = ""; // not working until here
            for( int j = 0 ; j < used_cards.size() ; j++ ){ 
                list_of_cards += ":" + used_cards.get(j) ;  
            }
            dos.writeUTF("play:dealer:"+cards_name.get(dealer_random)+":you"+used_card);
            line = dis.readUTF(); 
            int valid = 0; 
            int dealer_valid = 0; 
            for( int x : used_cards_values ){ 
                valid += x; 
            }
            for( int y : dealer_used_values ){ 
                dealer_valid += y; 
            }
            boolean early_break = true; 
            while( !line.equals( "stand" ) ){
                if( line.equals("double")){ 
                    if( amount_bet * 2 > num ){ 
                        dos.writeUTF("done:cheat");
                        break; 
                    }
                    amount_bet *= 2; 
                }
                if( line.equals( "split" ) ){   // not going too deep because i have absolutely no idea what split does can't english 
                // only split at the start, only if it's a pair (matches in value) <2 Ace no Ace and other face card> (if 2 ace then always split) 
                // double the bet (splitting the bet) 
                // adds a random card to each hand 
                // both needs to end in one way or the other 
                // same dealer's hand 
                    boolean same = false; 
                    if( used_card.size() > 2 ){ 
                        dos.writeUTF( "done:cheat more than 2 cards in deck"); 
                        break; 
                    }
                    if( used_cards_values.get(0) == used_cards_values.get(1) ){
                        same = true; 
                    }
                    if( amount_bet * 2 > num ){ 
                        dos.writeUTF( "done:cheat invalid bet" ); 
                        break; 
                    }
                    if( same == false ){ 
                        dos.writeUTF( "done:cheat no same cards" ); 
                        break; 
                    }
                    ArrayList< Integer > mock1 = new ArrayList<>(); 
                    ArrayList< Integer > mock2 = new ArrayList<>(); 
                    
                    mock1.add( used_card_values.get(0)); 
                    mock2.add( used_cards_values.get(1)); 
                    split(mock1, dealer_used_values, line, list_of_cards, list_of_cards_used, num, amount_bet ); 
                    split(mock2, dealer_used_values, line, list_of_cards, list_of_cards_used, num, amount_bet ); 
                }
                if(line.equals("hit") || line.equals( "double")){  
                    used_cards.add( cards_name.get(random) ); 
                    used_cards_values.add( cards_value.get(random) ); 
                    list_of_cards += ":" + cards_name.get(random); 
                    list_of_cards_used += ":" + cards_name.get(random);  
                    if( valid + cards_value.get(random) > 21 && cards_value.get(random) == 11 ){ 
                        valid += 1; 
                    }
                    else{ 
                        valid += cards_value.get(random); 
                    }
                    cards_name.remove(random); 
                    cards_value.remove(random); 
                    random = randomize();  
                    dealer_random = randomize(); 
                    while( random == dealer_random ){ 
                        dealer_random = randomize(); 
                    }
                }
                if( dealer_valid + cards_value.get(dealer_random) < 17 ){ 
                    dealer_valid += cards_value.get(dealer_random); 
                    cards_name.remove(dealer_random); 
                    cards_value.remove(dealer_random); 
                }
                else if( cards_value.get(dealer_random) == 11 ){ 
                    dealer_valid += 1; 
                    cards_name.remove( dealer_random ); 
                    cards_value.remove( dealer_random ); 
                }
                if( valid > 21 ){ 
                    dos.writeUTF( "status:lose:you:"+valid); 
                    early_break = false; 
                    break; 
                }
                if( valid == 21 && dealer_valid != 21 ){ 
                    dos.writeUTF( "status:win:you:blackjack"); 
                    num += (amount_bet + (amount_bet + (amount_bet / 2 ))); 
                    early_break = false; 
                    break; 
                }
                else{
                    if( line.equals( "double" ) ){ 
                        break; 
                    }
                    random = randomize(); 
                    dealer_random = randomize(); 
                    while( dealer_random == cards_name.size() ){ 
                        dealer_random = randomize(); 
                    }
                    dos.writeUTF("play:dealer:"+cards_name.get(random)+":you"+list_of_cards);
                }
                line = dis.readUTF(); 
            }
            if( valid == dealer_valid ){ 
                num += amount_bet; 
                dos.writeUTF( "status:push:dealer:"+dealer_valid+":you:"+valid);   
            }
            if( 21 - valid < 21 - dealer_valid && early_break ){ 
                dos.writeUTF( "status:win:dealer:"+dealer_valid+":you:"+valid); 
                num += (2 * amount_bet); 
            }
            else if( 21 - dealer_valid < 21 - valid && early_break){ 
                dos.writeUTF( "status:lose:dealer:"+dealer_valid+":you:"+valid); 
            }
            used_cards.clear(); 
            used_cards_values.clear(); 
            dealer_used_values.clear();
            valid = 0; 
            dealer_valid = 0; 
            amount_bet = 0; 
        }
        dos.writeUTF( "done:No More Money" ); 
    }

    private static void split( ArrayList< Integer > used_cards_values, ArrayList< Integer > dealer_used_values, String line, String list_of_cards, String list_of_cards_used, int num, int amount_bet ){ // line.equals( "split" ), num is the total amount player has, amount_bet is the total amount bet 
    // this has been done in case tehre is another stand or double 
        int valid = 0; 
        int dealer_valid = 0; 
        for( int x : used_cards_values ){ 
            valid += x; 
        }
        for( int y : dealer_used_values ){ 
            dealer_valid += y; 
        }
        boolean early_break = true;
        while( !line.equals( "stand" ) ){
            if( line.equals("double")){ 
                if( amount_bet * 2 > num ){ 
                    dos.writeUTF("done:cheat");
                    break; 
                }
                amount_bet *= 2; 
            }
            if( line.equals( "split" ) ){   // not going too deep because i have absolutely no idea what split does can't english 
                boolean same = false; 
                if( used_card.size() > 2 ){ 
                    dos.writeUTF( "done:cheat more than 2 cards in deck"); 
                    break; 
                }
                if( used_cards_values.get(0) == used_cards_values.get(1) ){
                    same = true; 
                }
                if( amount_bet * 2 > num ){ 
                    dos.writeUTF( "done:cheat invalid bet" ); 
                    break; 
                }
                if( same == false ){ 
                    dos.writeUTF( "done:cheat no same cards" ); 
                    break; 
                }
                split(used_cards_values, dealer_used_values, line, list_of_cards, list_of_cards_used, num, amount_bet ); 
            }
            if(line.equals("hit") || line.equals( "double")){  
                used_cards.add( cards_name.get(random) ); 
                used_cards_values.add( cards_value.get(random) ); 
                list_of_cards += ":" + cards_name.get(random); 
                list_of_cards_used += ":" + cards_name.get(random);  
                if( valid + cards_value.get(random) > 21 && cards_value.get(random) == 11 ){ 
                    valid += 1; 
                }
                else{ 
                    valid += cards_value.get(random); 
                }
                cards_name.remove(random); 
                cards_value.remove(random); 
                random = randomize();  
                dealer_random = randomize(); 
                while( random == dealer_random ){ 
                    dealer_random = randomize(); 
                }
            }
            if( dealer_valid + cards_value.get(dealer_random) < 17 ){ 
                dealer_valid += cards_value.get(dealer_random); 
                cards_name.remove(dealer_random); 
                cards_value.remove(dealer_random); 
            }
            else if( cards_value.get(dealer_random) == 11 ){ 
                dealer_valid += 1; 
                cards_name.remove( dealer_random ); 
                cards_value.remove( dealer_random ); 
            }
            if( valid > 21 ){ 
                dos.writeUTF( "status:lose:you:"+valid); 
                early_break = false; 
                break; 
            }
            if( valid == 21 && dealer_valid != 21 ){ 
                dos.writeUTF( "status:win:you:blackjack"); 
                num += (amount_bet + (amount_bet + (amount_bet / 2 ))); 
                early_break = false; 
                break; 
            }
            else{
                if( line.equals( "double" ) ){ 
                break; 
                }
                random = randomize(); 
                dealer_random = randomize(); 
                while( dealer_random == cards_name.size() ){ 
                    dealer_random = randomize(); 
                }
                dos.writeUTF("play:dealer:"+cards_name.get(random)+":you"+list_of_cards);
            }
            line = dis.readUTF(); 
        }
    }

    private static int randomize(){ 
        int random = (int)(Math.random() * cards_name.size() ); 
        while( random == cards_name.size() ){
            random = (int)(Math.random() * cards_name.size() ); 
        }
        return random; 
    }

    private static void shuffle(){ 
        cards_name.clear(); 
        cards_value.clear(); 
        String[]suits = {"S","H","D","C"};
        for( String suit : suits ){ 
            for( int i = 2 ; i < 15 ; i++ ){ // 11 = j, 12 = q, 13 = k, 14 = A
                if(i == 11){ 
                    cards_name.add("J"+suit); 
                    cards_value.add( 10 ); 
                }
                else if(i==12){
                    cards_name.add("Q"+suit); 
                    cards_value.add(10);
                }
                else if(i==13){
                    cards_name.add("K"+suit);
                    cards_value.add(10);
                }
                else if(i==14){
                    cards_name.add("A"+suit);
                    cards_value.add(11); 
                }
                else{
                    cards_name.add(""+i+suit); 
                    cards_value.add(i);
                }
            }
        }
    }
}