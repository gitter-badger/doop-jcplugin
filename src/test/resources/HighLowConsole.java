
/* Simulation of console-I/O program HighLow,
   using ConsoleApplet as a basis.  See the file
   ConsoleApplet.java for more information.
   This class uses the classes Deck and Card.
   
   David Eck
   eck@hws.edu
   
*/

public class HighLowConsole extends ConsoleApplet {

   protected String getTitle() {
      return "Sample program \"HighLow\"";
   }

   protected void program() {
      
      /*
         This program lets the user play HighLow, a simple card game that is
         described in the following output statements.  After the user plays several
         games, the user's average score is reported.
      */
      
      console.putln("This program lets you play the simple card game HighLow.");
      console.putln("A card is dealt from a deck of cards.  You have to predict");
      console.putln("whether the next card will be higher or lower.  Your score");
      console.putln("in the game is how many correct predictions you make before");
      console.putln("you guess wrong.");
      console.putln();
      
      int gamesPlayed = 0;     // Number of games user has played.
      int sumOfScores = 0;     // The sum of all the scores from all the
                               //      games played.
      double averageScore;     // Average score, computed by dividing
                               //      sumOfScores by gamesPlayed.
      boolean playAgain;       // Record user's response when user is asked
                               //   whether he wants to play another game.
      
      do {
         int scoreThisGame;        // Score for one game.
         scoreThisGame = play();   // Play the game and get the score.
         sumOfScores += scoreThisGame;
         gamesPlayed++;
         console.put("Play again? ");
         playAgain = console.getlnBoolean();
      } while (playAgain);
      
      averageScore = ((double)sumOfScores) / gamesPlayed;
      
      console.putln();
      console.putln("You played " + gamesPlayed + " games.");
      console.putln("Your average score was " + averageScore);

   }


   int play() {
         // Lets the user play one game of HighLow, and returns the
         // user's score in the game.
   
      Deck deck = new Deck();  // Get a new deck of cards, and store
                               //   a reference to it in the variable, Deck.
      
      Card currentCard;  // The current card, which the user sees.

      Card nextCard;     // The next card in the deck.  The user tries
                         //    to predict whether this is higher or lowe
                         //    than the current card.

      int correctGuesses ;  // The number of correct predictions the
                            //   user has made.  At the end of the game,
                            //   this will be the user's score.

      char guess;   // The user's guess.  'H' if the user predicts that
                    //   the next card will be higher, 'L' if the user
                    //   predicts that it will be lower.
      
      deck.shuffle();
      correctGuesses = 0;
      currentCard = deck.dealCard();
      console.putln("The first card is the " + currentCard);
      
      while (true) {  // Loop ends when user's prediction is wrong.
         
         /* Get the user's predition, 'H' or 'L'. */
         
         console.put("Will the next card be higher (H) or lower (L)?  ");
         do {
             guess = console.getlnChar();
             guess = Character.toUpperCase(guess);
             if (guess != 'H' && guess != 'L') 
                console.put("Please respond with H or L:  ");
         } while (guess != 'H' && guess != 'L');
         
         /* Get the next card and show it to the user. */
         
         nextCard = deck.dealCard();
         console.putln("The next card is " + nextCard);
         
         /* Check the user's prediction. */
         
         if (nextCard.getValue() == currentCard.getValue()) {
            console.putln("The value is the same as the previous card.");
            console.putln("You lose on ties.  Sorry!");
            break;  // End the game.
         }
         else if (nextCard.getValue() > currentCard.getValue()) {
            if (guess == 'H') {
                console.putln("Your prediction was correct.");
                correctGuesses++;
            }
            else {
                console.putln("Your prediction was incorrect.");
                break;  // End the game.
            }
         }
         else {  // nextCards is lower
            if (guess == 'L') {
                console.putln("Your prediction was correct.");
                correctGuesses++;
            }
            else {
                console.putln("Your prediction was incorrect.");
                break;  // End the game.
            }
         }
         
         /* To set up for the next interation of the loop, the nextCard
            becomes the currentCard, since the currentCard has to be
            the card that the user sees, and the nextCard will be
            set to the next card in the deck after the user makes
            his prediction.  */
         
         currentCard = nextCard;
         console.putln();
         console.putln("The card is " + currentCard);
         
      } // end of while loop
      
      console.putln();
      console.putln("The game is over.");
      console.putln("You made " + correctGuesses + " correct predictions.");
      console.putln();
      
      return correctGuesses;
      
   }  // end play()
   
}

