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
        int count =0 ; 
        while( num > 0 ){ 
            if( cards_name.size() < 6 ){ 
                shuffle();  
                list_of_cards_used = ""; 
            }
            int initial_random = (int)(Math.random() * cards_name.size());
            used_cards.add(cards_name.get(initial_random));  
            used_cards_values.add(cards_value.get(initial_random)); 
            list_of_cards_used += ":" + cards_name.get(initial_random); 
            cards_name.remove(initial_random); 
            cards_value.remove(initial_random); 
            int secondary_random = (int)(Math.random() * cards_name.size()); 
            used_cards.add(cards_name.get(secondary_random)); 
            list_of_cards_used += ":" + cards_name.get(secondary_random); 
            used_cards_values.add(cards_value.get(secondary_random)); 
            cards_name.remove(secondary_random); 
            cards_value.remove(secondary_random);
            int dealer_initial = (int)(Math.random() * cards_name.size()); 
            dealer_used_values.add(cards_value.get(dealer_initial));
            list_of_cards_used += ":" + cards_name.get(dealer_initial);
            cards_name.remove(dealer_initial); 
            cards_value.remove(dealer_initial); 
            int dealer_secondary = (int)(Math.random() * cards_name.size()); 
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
            int random = (int)(Math.random() * cards_name.size());
            int dealer_random = (int)(Math.random() * cards_name.size()); 
            while( random == dealer_random ){ 
                dealer_random = (int)(Math.random() * cards_name.size()); 
            }
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
                    boolean same = false; 
                    for( int i : used_cards_values ){ 
                        if( used_cards_values.indexOf(i) == used_cards_values.lastIndexOf(i) ){ 
                            same = true; 
                        }
                    }
                    if( amount_bet * 2 > num || same ){ 
                        dos.writeUTF( "done:cheat" ); 
                        break; 
                    }
                }
                if(line.equals("hit")){  
                    used_cards.add( cards_name.get(random) ); 
                    used_cards_values.add( cards_value.get(random) ); 
                    list_of_cards += ":" + cards_name.get(random); 
                    list_of_cards_used += ":" + cards_name.get(random);  
                    valid += cards_value.get(random); 
                    cards_name.remove(random); 
                    cards_value.remove(random); 
                    random = (int)(Math.random() * cards_name.size()); 
                    dealer_random = (int)(Math.random() * cards_name.size()); 
                    while( random == dealer_random ){ 
                        dealer_random = (int)(Math.random() * cards_name.size()); 
                    }
                }
                if( dealer_valid + cards_value.get(dealer_random) < 17 ){ 
                    dealer_valid += cards_value.get(dealer_random); 
                    cards_name.remove(dealer_random); 
                    cards_value.remove(dealer_random); 
                }
                if( valid > 21 ){ 
                    dos.writeUTF( "status:lose:you:"+valid); 
                    num -= amount_bet; 
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
                    dos.writeUTF("play:dealer:"+cards_name.get(random)+":you"+list_of_cards);
                    line = dis.readUTF(); 
                }
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