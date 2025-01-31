# Rummikub

1. You should be able to play Rummikub with 2-4 people over a network with a client &
   server application.\
This is possible by simply creating more instances of the game and entering the player's name.
After that, the console asks if you wnat to create or join. If you select join, the 
game will automatically add you to the existing game.

2. The client should be able to connect to a server, play a game and announce the winner
   in the end.\
This is possible, it was tested by simply playing the game until the end and waiting for the winner 
announcement.

3. The server should be able to host at least one game, following the rules of Rummikub,
   and determine the winner in the end.\
Also true, tested by playing the game.
4. It is required to have a UI in the client in order to play the game. This may be a TUI, and
   it doesn’t matter what it looks like.\
The game implements a TUI in the Controller class, which is being printed on each client's screen.
5. Computer Player, You should have a computer player to play with. This player should at
   least only do valid actions.
The computer player only places tiles on the table, but they are always valid. 