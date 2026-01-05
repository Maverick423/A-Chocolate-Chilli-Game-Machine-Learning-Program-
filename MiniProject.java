// Maverick Anushk Fernandes
// 24/10/2025
// VERSION 1

/* 
States the rules of the game. Than asks the user to input thier name and how many chocolates they wish to start with.
At the end of the method Introduction it returns the number of chocolate which is stored in numChocolate in main function.
Machine and user take turns to eat the chocolates until there are 0 left for a certain amount of games which the user decides.
At the end depending on who won the most it will say "Winner is machine" or "Winner is (name of user)" or "Its a draw".
Stores the record in memory, finds bad moves and avoids them. 
The memory is written in a file text so it remembers its past game and doesnt repeat bad moves.
*/

import java.util.Scanner; // Needed to make Scanner available
import java.util.Random; // Needed to make Random available
import java.io.*; // Needed to make io available

class ChocolateChilliGame { // START ChocolateChilliGame
    
    public static void main (String [] arg) throws IOException { // START main 
        // this calls all the functions and initialises Random and Scanner which is passed to the methods
        
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        String[] playerInfo = new String[3]; // number of chocolate to start with
        Rules(); // Method call
        playerInfo = Introduction(scanner); // Method call
        GameLoop(scanner, random, playerInfo);
        scanner.close();
        return;
    } // END main
    
    public static void Rules() { // START Rules
        // this function just prints out the rules for the user when they play the game
        
        System.out.println("Chocolate Chilli Game Rules\n"); // Rules
        System.out.println("1. There is a pile of chocolates on the table.");
        System.out.println("2. Two players take turns");
        System.out.println("3. On your turn, you must eat 1, 2, or 3 chocolates from the pile.");
        System.out.println("4. Players must take at least one chocolate each turn.");
        System.out.println("5. The player who has to take the last chocolate is the loser - they have to eat the chilli instead of chocolate!");
        System.out.println("6. The game ends as soon as there are no chocolates left.");
        System.out.println("7. The aim is to avoid taking the last chocolate - so you must try to force your opponent to take it.\n");
        return;
    } // END Rules

    public static String[] Introduction(Scanner scanner) { // START Introduction
        // this function takes in user details like thier name and how many chocolates they want to start with and the number of games they want to play
        
        String[] intro = new String[3]; // [name, intial chocolate, total games]
        System.out.println("What's your name?");
        intro[0] = scanner.nextLine(); // name
        do { // validation
        System.out.println("How many chocolates would you like to start with? (Greater than 10 inclusive)");
        intro[1] = scanner.nextLine(); // intial chocolate
        }while (Integer.parseInt(intro[1])<10);
        do { // validation
        System.out.println("How many games do you want to play?");
        intro[2] = scanner.nextLine(); // total games
        }while(Integer.parseInt(intro[2])<=0);
        System.out.println("Thanks, " + intro[0] + "! There are " + intro[1] + " chocolates on the table. I will go first.\n");
        return intro;
    } // END Introduction
    
    public static void GameLoop(Scanner scanner, Random random, String[] playerInfo) throws IOException{ // START GameLoop
        /*
        this is the game loop which goes through the amount of games the user wanted to play. it calls machine and player methods repeatedly until 
        a winner is decided. than it calls the score method which stores who won and announces the winner. 
        */
        
        int leftChocolates = Integer.parseInt(playerInfo[1]); // initial chocolate
        int totalGame = Integer.parseInt(playerInfo[2]); // number of games to play
        boolean machineWon;
        String winner = null;
        int[] allGame = new int[totalGame];
        int[] allMoves = new int[leftChocolates];
        boolean[][] readFile = readMemory();
        boolean[][] memory = Memory(leftChocolates,readFile);
        for (int i = 1; i<=totalGame; i++){
            int count =0;
            System.out.println("Game " + i);
            do{ // so all variables are initialised 
                leftChocolates = Machine(memory, allMoves, random, leftChocolates, count); // machines turn
                if (leftChocolates==-1){
                    machineWon=false;
                    break;
                }
                machineWon = true; 
                allMoves[count] = leftChocolates;
                count+=1;
                if (leftChocolates<=0) { // checks if there are any chocolates left 
                    break; // breaks do while loop
                }
                leftChocolates = Player(scanner, leftChocolates); // players turn
                machineWon = false;
                allMoves[count] = leftChocolates;
                count+=1;
            } while (!(leftChocolates<=0)); // runs until there is no more chocolates left
            leftChocolates = Integer.parseInt(playerInfo[1]); // gets original starting number of chocolates as new round
            allGame = Score(playerInfo,machineWon,allGame); //records who won
            winner = Result(playerInfo,allGame);
            if (allGame[i-1]==2){
                MemoryRemove(memory, allMoves, -1, count-1); 
            }                                           
        } 
        if (winner.equals("Draw")){ // draw
            System.out.println("Its a draw");
        }else{
            System.out.println("Winner is " + winner); // someone won
        }
        writeMemory(memory, readFile);
        return;
    } // END GameLoop

    public static int Player(Scanner scanner, int chocolatesLeft) { // START Player
        /* 
        this asks the user how many chocolates do you wish to eat? (1, 2 or 3) if the user didnt pick the given than repeats it until answer is given. 
        than it checks is the user has won if they didnt than returns how many chocolates are left after they eat.
        */

        int pickedChocolates;
        do { 
            System.out.println("How many chocolates do you wish to eat? (1, 2 or 3)");
            pickedChocolates = Integer.parseInt(scanner.nextLine()); // converts string to int
        } while (!(pickedChocolates == 1 || pickedChocolates == 2 || pickedChocolates == 3)); // makes sure user pick 1, 2 or 3
        if (pickedChocolates > chocolatesLeft) {
            System.out.println("There are only " + chocolatesLeft + " left. Try again\n");
            chocolatesLeft = Player(scanner, chocolatesLeft);
        }else {
            chocolatesLeft -= pickedChocolates; // subtracts by input
        }
        System.out.println("There are " + chocolatesLeft + " left.\n"); // total number of chocolates left
        return chocolatesLeft;
    } //END Player
    
    public static int Machine(boolean[][] memory, int[] moves, Random random, int chocolatesLeft, int index) { //START Machine
        /*
        this picks randomly a number between 1 to 3 and eats that amout of chocolate. however before doing that it looks into memory for the available choices
        than from the available choices picks randomly using the random library. also if the move is a bad move than removes it from memory by making it false.
        */
        int pickedChocolates = MemoryGetNum(random, memory, chocolatesLeft);
        int [] listNum = CheckMemory(memory, chocolatesLeft); // list of all moves that can be picked
        if (listNum[0]==0 && listNum[1]==0 && listNum[2]==0 || pickedChocolates == 0) { 
            return -1; // all move set is false so can't pick 
        }
        System.out.println("The machine has picked " + pickedChocolates);
        if (pickedChocolates > chocolatesLeft) {
            System.out.println("macine picked : " + pickedChocolates);
            MemoryRemove(memory, moves, pickedChocolates, index); // if what machine picked exceeds number of chocolate left than removes it from move set
            chocolatesLeft = Machine(memory, moves, random, chocolatesLeft, index); // calls it again to get different value
        }else {
            chocolatesLeft -= pickedChocolates; // subtracts by input
        }
        System.out.println("There are " + chocolatesLeft + " left.\n"); // total number of chocolates left       
        return chocolatesLeft;
    } // END Machine
    
    public static int[] Score(String[] infoPlayer, boolean machineWin, int[] gameTotal) { // START Score
        // creates a array with size the total games. than stores 1 if machine won and 2 if player won.

        int j = gameTotal.length; // j is max
        for(int i = 0; i<gameTotal.length; i++) {
            if (gameTotal[i] == 0) { // when initialised contains all 0 
                j-=1; // gets the position of the next empty space
            }
        }
        if (machineWin) { // checks boolean value
            gameTotal[j] = 1; // machine won
        }else {
            gameTotal[j] = 2; // player won
        }
        return gameTotal;
    } // END Score
    
    public static String Result(String[] infoPlayer, int[] gameResult) { // START Result
        /*  
        uses the array from score to check who won the most, goes through the array and checks who won 1 if machine won and 2 if player won. than whoever has 
        most point is the winner. it than returns who won player name if user won "machine" if machine won and "draw" if its a draw
        */

        String name = infoPlayer[0]; //S first value has name
        int playerScore = 0; // start score
        int machineScore = 0; // start score
        for (int i = 0; i<gameResult.length; i++) {
            if (gameResult[i] == 2) { // player won 
                playerScore += 1;
            }else if (gameResult[i] == 1) { // machine won
                machineScore += 1;
            }
        }
        System.out.println(name + ": " + playerScore); // print score 
        System.out.println("Machine: " + machineScore + "\n"); // print score
        if (playerScore>machineScore) { // player won
            return infoPlayer[0];
        }else if (playerScore<machineScore) { // machine won
            return "Machine";
        }else {
            return "Draw"; // draw
        }
    } // END Result

    public static boolean[][] Memory(int totalChocolate, boolean[][] readMemory) { // START Memory
        /*
        this is the memory a 2d array which holds the possible moves for this 2d array i made it start with 1 so the first [[],[]] is 1 chocolate remaining than 2 and so on
        this reads memory from the file which stores it. if more starting chocolate are picked than in memory it loads that pasrt of memory and fills the rest than writes 
        whole memory in the file. if its shorter than only reads part of file. 
        */

        boolean[][] memory = new boolean[totalChocolate][3]; // 2d array with static memory 
        if (totalChocolate<=readMemory.length){
            for (int i = 0; i < totalChocolate; i++){
                for (int j = 0; j < 3; j++){
                    memory[i][j] = readMemory[i][j];
                }
            }
        } else {
            for (int i = 0; i < readMemory.length; i++){
                for (int j = 0; j < 3; j++){
                    memory[i][j] = readMemory[i][j];
                }
            }
            for (int i = readMemory.length; i < totalChocolate; i++){
                for (int j = 0; j < 3; j++){
                    memory[i][j] = true;
                }
            }
        }
        return memory;
    } // END Memory

    public static void MemoryRemove(boolean[][] memory, int[] moves, int chocolatePicked, int index) { // START MemoryRemove
        /*
        this removes any bad moves chocolate picked -1 is at the end of game if the player won it goes in the memory imagine this is the array of chocolates left
        [13,11,8,7,4,3,0,0,0]  player picked 3 when there were 3 chocolates left so 0 left so it will go to 4 and change the chocolate it choose to eat tp false
        and if all are false so no more moves when 4 chocolates left than calls edit memory. the else is when there are 2 chocolates left but machine picks 3 so it makes 3 false.
        */

        if (!(chocolatePicked ==-1)){
            memory[moves[index-2]-1][chocolatePicked-1] = false; //[13,11,8,7,4,3,0,0,0]
            if (CheckMemory(memory,chocolatePicked)[0]==0 && CheckMemory(memory,chocolatePicked)[1]==0 && CheckMemory(memory,chocolatePicked)[2]==0){ // checks if all of it is false
                EditMemory(memory,moves,index); // changes the previous move 
            }
        }else{           //[15,12,8,6,3,0]                                                   
            memory[moves[index-2]-1][(moves[index-2]-moves[index-1])-1] = false;
            if (CheckMemory(memory,moves[index-2]-moves[index-1])[0]==0 && CheckMemory(memory,moves[index-2]-moves[index-1])[1]==0 && CheckMemory(memory,moves[index-2]-moves[index-1])[2]==0){ // checks if all of it is false
                EditMemory(memory,moves,index); // changes the previous move 
            }
        }
        return;
    } // END MemoryRemove
    
    public static int MemoryGetNum(Random random, boolean[][] memory, int chocolatesLeft) { // START MemoryGetNum
        /*
       this gets the available option for the machine to pick number of chocolate it wants to eat. it checks which options are availabe than uses random to pick 
       value from array 
        */

        int[] listNum = CheckMemory(memory, chocolatesLeft); // gets all possible move so (1,2,3) based on which is true 
        int values=0;
        int number=0;
        for (int i=0; i<3; i++){
            if (listNum[i]!=0){
                values+=1;
                number = listNum[i];
            }
        }
        if (values > 1){
            int pickedChocolates = listNum[random.nextInt(values-1)]; // randomly picks one of the availabe options
            return pickedChocolates;
        }else if (values == 1){
           return number;
        }else{
            return 0;
        }
    } // END MemoryGetNum

    public static int[] CheckMemory(boolean[][] memory, int chocolatesLeft){
        /*
        checks what the availabe options are for that number of chocolate left than puts them in a array and returs it. for example [[true,false,false],[]]
        for 1 chocolate left there is only 1 option available so it will return [1,0,0] 0 because thats how the array are iniatialised 
        */

        int[] listNum = new int[3]; // static 1d array
        int index=0;
        for (int i = 0; i<3; i++){ // [[],[]] gets the size of the inner array 
            if (memory[chocolatesLeft-1][i]){ // checks if true eg [[true,false,true],[]]
                listNum[index]=i+1; // if true it will return the index value so new list will be [1,3,0] from above
                index+=1;
            }
        }
        return listNum;
    }

    public static void EditMemory(boolean[][] memory, int[] moves, int index) { // END EditMemory
        /*
        this is when there are no option available for example if all options for 4 chocolate left is false in [13,11,8,7,4,3,0,0,0] than it will go to 8 and 
        change what it picked to false.
        */
        
        for (int i = index; i >= 0; i-=2){ // finds the position of the empty (all false) inner array
            if (!(CheckMemory(memory, moves[i])[0]==0 && CheckMemory(memory, moves[i])[1]==0 && CheckMemory(memory, moves[i])[2]==0)){ // checks if there are possible moves list is empty
                memory[moves[i]-1][moves[i]-moves[i+1]-1] = false; // [13,11,8,7,4,3,0,0,0] is all is false for 4 in the inner than will go to 8 and change that  
                break;
            }
        }
        return;
    } // END EditMemory 

    public static void writeMemory(boolean[][] memory, boolean[][] readMemory) throws IOException{
        /* 
        reads the memory and stores all of its choices in the string data and depending on size of memory and size of file memory i will change 
        so if file memory is smaller than it will just over write it  if its bigger than and it will add the memory to data than fill in the remaining 
        with values from file memory and write it in file.
        */

        String data="";
        PrintWriter writeFile = new PrintWriter(new FileWriter("Memory.txt"));
        if (memory.length >= readMemory.length){ // this game played picked more or equal chocolate than past games
            for (int i = 0; i<memory.length;i++){
                data = data +"[";
                for (int j = 0; j<3;j++){
                    data = data + memory[i][j];
                    data = data +" ";
                }
                data = data + "] "; // writing in the file in this formet [...] [...]
            }
            
        }else{ // this game played picked less chocolate than past games
            for (int i = 0; i < memory.length; i++){ 
                for (int j = 0; j < 3; j++){
                    readMemory[i][j] = memory[i][j];
                }
            }
            data = data + "[";
            for (int i = 0; i<readMemory.length;i++){
                for (int j = 0; j<3;j++){
                    data = data + readMemory[i][j];
                    data = data +" ";
                }
                data = data + "] "; // writing in the file in this formet [...] [...]
            }
        }
        writeFile.println(data); // writing the string with data into memory
        writeFile.close();
    }
    
    public static boolean[][] readMemory() throws IOException {
        /*
        this checks if the file exits, if it doesnt makes it. checks if file is empty if it is than add " " so it doesnt break when i read it by saying its emoty cant read
        i get rid of "[" and "]" which i used for visuals than split to make it into a boolean array as it had true and false in it.
        than i read it in intervals of 3 into to memory which will be used for the program. intervals of 3 beacuse u can pick 1, 2 or 3 for each number of chocolates left. 
        */
        
        File file = new File("Memory.txt");
        int k=0;
        if (!(file.exists())) {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.println(""); // writes in the file so not empty
            pw.close();
            return new boolean[0][3]; 
        }
        BufferedReader readFile = new BufferedReader(new FileReader("Memory.txt"));
        String text= readFile.readLine();
        if (text == null || text.isEmpty()) { // checks if file is empty 
            readFile.close();
            return new boolean[0][3]; // retuns a 2d array with inner size 3
        }
        text= text.replace("[", ""); 
        text= text.replace("] ", ""); // makes the array go from [...] [...] [...] to [......] 
        String[] mem = text.split(" "); // makes it into a functioning array with commas [., ., ., .]
        boolean[][] memory = new boolean[mem.length/3][3];
        for (int i = 0; i<mem.length/3; i++){
            for (int j = 0; j<3; j++){
                memory[i][j] = Boolean.parseBoolean(mem[k]);
                k++;
            }
        }
        readFile.close();
        return memory;
    }
} // END ChocolateChilliGame