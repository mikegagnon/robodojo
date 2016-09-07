
Robo Dojo is a game where you program bots to battle each other with home-brewed
viruses ([screenshot](##4way)).
Robo Dojo clones the 1998 game [RoboCom](##robocom).

* [Play Robo Dojo](http://robodojo.club)
* [Guide](##guide)
* [Reference](##ref)
* [How to navigate this document](##navigate)

~navigate
## How to navigate this document

This is a [Sidenote](http://sidenote.io) document.

Sidenote opens links in new columns.

TODO

~robocom
## RoboCom

RoboCom is no longer actively maintained but its website is archived
[here](http://robocom.rrobek.de/). Here's a [screenshot](##robocomscreenshot).

Robo Dojo only implements the "Original" instruction set from RoboCom.

Robo Dojo uses the "Classic" timing model from RoboCom.

I have strove to make the Robo Dojo simulation semantics identical to RoboCom --
however [I could not create a cycle-for-cycle duplicate of RoboCom](##compatibility).

Nevertheless, Robo Dojo and RoboCom are similar enough to produce
[similar outputs for similar inputs](##rdrc-battles).

~4way
## Screenshot of a four-way battle

<img src="img/battle.png">

- Each tear-drop shape is a different bot
- If a bot contains a circle of a different color, that means the bot
  is infected
- If a bot has a white line through it, that means the bot is deactivated


~guide
## Guide

1. [Hello World](##guide-hello)
2. Replication
3. 

~guide-hello
## Hello World

Here is your "Hello World" program:

    bank main
    move

[Run your program](##guide-hello-run).

Concepts:

- The board
- The bot
- The `bank` directive
- The `move` instruction

~guide-hello-run
## Run your program

1. Go to [RoboDojo.club](http://robodojo.club)
2. In the editor, type in your program
3. Click compile
4. Click the play button. You will see your bot running.
5. Click the pause button once you're bored

~compatibility
## Compatibility with RoboCom

The problem is at least twofold:

1. RoboCom is closed source
2. Some of RoboCom's semantics appear to be undocumented

Here is one example of [mysterious RoboCom semantics](##compat-mystery).

~rdrc-battles
## Tournament

**TODO**: Rename header

Running of of version 93d5a2177f910cf17aa3885102b5687a962531b6

**TODO**: Explain X-Y-Z system

Round 1

    Match                             Robo Dojo   RoboCom
    ------------------------------------------------------
    Activator3 vs. Alien3.4           0-2-5       0-1-7
    Alpha vs. Fruchtzwerk 2           2-0-5       1-0-6
    Conciler vs. Kommari 2.1          6-0-1       6-0-1
    Delusion 3.7 vs. Einfachst-DM.DV  7-0-0       7-0-0
    DoomMob 1.6 vs. Flooder 1.0       0-7-0       0-7-0
    Geza's McRobi vs. Goody2          0-7-0       0-7-0
    Martins Echter vs. Goodymorph     0-0-7       0-0-7
    HotBot V2 vs. inter#active        6-0-1       5-0-2

Round 2

    Match                             Robo Dojo   RoboCom
    ------------------------------------------------------
    Alien3.4 vs. Alpha                0-0-0       0-0-0
    Conciler vs. Einfachst-DM.DV      0-0-0       0-0-0
    Flooder 1.0 vs. Goody2            0-0-0       0-0-0
    Goodymorph vs. HotBot V2          0-0-0       0-0-0


~compat-mystery
## Mysterious RoboCom semantics

I loaded up [two robots](##compat-mystery-2robots) from the [July Package](http://robocom.rrobek.de/?area=d_bots),
and placed them in the [same starting position](##compat-mystery-start) in both
Robo Dojo and RoboCom.

Then, the [simulations diverged](##compat-myster-diff) between Robo Dojo and RoboCom.

**TODO**: explain how I know my semantics are correct

I investigated several changes to Robo Dojo to see if I could get the 
simulations to converge, but none of the changes worked.

~compat-myster-diff
## Simulations diverged

- At [T=822](##t822) the simulations for the selected bot were in sync 
- At [T=826](##t826) the selected bot in the Robo Dojo simulation completed executing a
    `set` instruction, and loaded a `bjump` instruction
- At [T=827](##t827) the selected bot in RoboCom performed the same operation as Robo
    Dojo at T=826

~t822
## T=822

We know this because:

1. At [T=821](##t821-demo) the selected bots were deactivated
2. At [T=822](##t822-demo) the selected bots were activated

[Note](##t822-note) on Robo Dojo execution.

~t822-note
## Note on Robo Dojo execution

In one board cycle, the selected bot becomes activated *and* executes one
cycle of the `set` instruction.

You might think that a bot shouldn't be able to become activated *and*
execute a bot-cycle in the same board-cycle, however these semantics are
intentional.

[Here's how it works](##t822-note-works).

I have investigated preventing bots from executing a cycle immediately after
their activation, however this change did not effect convergence.

~t822-note-works
## Here's how it works

1. During T=821 every bot executes one cycle (or zero cycles if the bot is deactivated)
2. The bots execute in order from oldest bot to youngest bot
1. During T=821 The mother bot activates the selected bot
2. Since the selected bot is younger than the mother bot, it gets to execute
a cycle during T=821 after its mother




~t826
## T=826

We this because:

1. At [T=825](##t825-rd-demo) the selected bot was executing a `set` instruction
2. At [T=826](##t826-rd-demo) the selected bot was executing a `bjump` instruction

~t827
## T=827

We this because:

1. At [T=826](##t826-rc-demo) the selected bot was executing a `set` instruction
2. At [T=827](##t827-rc-demo) the selected bot was executing a `bjump` instruction


~t821-demo
## T=821
s
<img src="img/compat-821-rd.png">

<img src="img/compat-821-rc.png">

~t822-demo
## T=822

<img src="img/compat-822-rd.png">

<img src="img/compat-822-rc.png">

~t825-rd-demo
## T=825 in Robo Dojo

<img src="img/compat-825-rd.png">

~t826-rd-demo
## T=826 in Robo Dojo

<img src="img/compat-826-rd.png">



~t826-rc-demo
## T=826 in RoboCom

<img src="img/compat-826-rc.png">

~t827-rc-demo
## T=827 in RoboCom

<img src="img/compat-827-rc.png">






~compat-mystery-2robots
## Two robots

Activator3 (orange/red) and Alien 1.4 (blue)

~compat-mystery-start
## Same starting position

<img src="img/compat-start-robodojo.png">

<img src="img/compat-start-robocom.png">





~robocomscreenshot
## RoboCom Screenshot

<img src="img/robocom-screenshot.png">