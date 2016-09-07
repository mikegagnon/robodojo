
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
2. [Replication](##replication)
3. 

~replication
## Replication

Here is your replication-demo program:

    bank main
    create 2,1,0
    trans 1,1
    set %active, 1

### How it works

- [The <tt>create</tt> instruction](##guide-repl-create)
- [The <tt>trans</tt> instruction](##guide-repl-trans)
- [The <tt>set</tt> instruction](##guide-repl-set)

~guide-repl-create
## The <tt>create</tt> instruction

The `create` instruction builds a new bot in front of the builder.

There are three parameters:

1. [Instruction set](##guide-repl-create-instrset)
2. [Number of banks](##guide-repl-create-numbanks)
3. [Mobility](##guide-repl-create-mobility)

~guide-repl-create-instrset
## Instruction set

There are three instruction sets: Basic (0), Advanced (1), and Super (2).

Different instructions belong to different instruction sets.
[For example](##guide-repl-create-instrset-example).

Each bot is either Basic, Advanced, or Super -- which effects
[which instructions each bot may execute](##guide-repl-create-instrset-bot).

The first parameter of the `create` instruction specifies the instruction set
for the child bot.

In our Replication program, we use the parameter 2 to specify that the child
should have the Super instruction set, since we want the child to be able to
`create` a child of its own.

**TODO**: Link to instruction set documentation


~guide-repl-create-instrset-bot
## Which instructions each bot may execute

- A Basic bot can only execute Basic instructions
- An Advanced bot can execute Basic and Advanced instructions
- A Super bot can execute Basic, Advanced, and Super instructions

~guide-repl-create-instrset-example
## For example

- The `move` instruction belongs to the Basic instruction set
- The `create` instruction belongs to the Super instruction set


~guide-repl-create-numbanks
## Number of banks

Each bot contains one or more [banks](##guide-hello-bank).

The second parameter to the `create` instruction specifies how many banks
the child should have.

These will all be empty banks, which is to say every bank will have
zero instructions.

~guide-repl-create-mobility
## Mobility

Each bot is either mobile or immobile (i.e. whether or not it can execute the
`move` instruction).

The third parameter to the `create` instruction specifies whether or not the
child should be mobile:

- 0 signifies immobility
- 1 signifies mobility 


~guide-repl-trans
## The <tt>trans</tt> instruction

~guide-repl-set
## The <tt>set</tt> instruction



~guide-hello
## Hello World

Here is your "Hello World" program:

    bank main
    move

[How to run your program](##guide-hello-run)

### How it works

- [Editors](##guide-hello-editor)
- [Execution model](##guide-hello-execution)
- [Compile button](##guide-hello-compile)
- [The <tt>bank</tt> directive](##guide-hello-bank)
- [The <tt>move</tt> instruction](##guide-hello-move)

~guide-hello-move
## The <tt>move</tt> instruction

The `move` instruction tells the bot to move forward one cell.

It takes 18 [cycles](##guide-hello-execution) to execute; on the 18th cycle the bot 
attempts to move forward.

If the forward cell is occupied on the 18th cycle, then the `move` instruction
does nothing.


~guide-hello-bank
## The <tt>bank</tt> directive

Every program is segmented into banks; every instruction belongs to exactly
one bank.

For our hello-world program, our program has only one bank, which we have
named the <tt>main</tt> bank.

When the simulation launches, each bot begins executing at the first instruction
of the first bank of the program.

### See also

- [Working with Banks](##working-with-banks) TODO

~guide-hello-compile
## Compile button

When you click Compile, the board clears itself then uploads your program to a
fresh bot, then randomly places the bot on the board.

~guide-hello-editor
## Editors

There are four editors, one for each color: blue, red, green, and yellow.

The blue editor edits the blue bot's program, etc.

To select your editor, click the "Blue bot" drop down menu.

<img src="img/editor-selection.png">

~guide-hello-execution
## Execution model

The board is the 16 x 16 toroidal universe where bots play.

Each cell can hold only one bot at a time.

The board executes one step at a time.

During each board step, the board executes one [cycle](##cycle) for each bot, in order
from oldest bot to newest bot.

~cycle
## Cycle

To execute an instruction, the bot must execute one or more cycles for the
instruction.

For example, the `move` instruction requires 18 cycles.

During the 18th cycle, the `move` instruction will actually execute.

Before the 18th cycle, the `move` instruction does nothing.

**See also**

- [The Strategy of Cycles](##cycle-strategy)

~cycle-strategy
## The Strategy of Cycles

**TODO**

~guide-hello-run
## Run your program

1. Go to [RoboDojo.club](http://robodojo.club)
2. In the Editor, type in your program
3. Click Compile
4. Click the Play button. You will see your bot moving across the board.
5. Click the Pause button once you're bored

~compatibility
## Compatibility with RoboCom

The problem is at least twofold:

1. RoboCom is closed source
2. Some of RoboCom's semantics appear to be undocumented

Here is one example of [mysterious RoboCom semantics](##compat-mystery).

[Known differences](##compat-diff)

~compat-diff
## Known differences between Robo Dojo and RoboCom

- Elimintation trigger
- Mobile first bot

~rdrc-battles
## Tournament

**TODO**: Rename header

Running of of version 93d5a2177f910cf17aa3885102b5687a962531b6

**TODO**: Explain X-Y-Z system

Round 1

    Match                               Robo Dojo   RoboCom
    --------------------------------------------------------
    Activator3 vs. Alien3.4               0-2-5       0-1-7     *
    Alpha vs. Fruchtzwerk 2               2-0-5       1-0-6
    Conciler vs. Kommari 2.1              6-0-1       6-0-1
    Delusion 3.7 vs. Einfachst-DM.DV      7-0-0       7-0-0
    DoomMob 1.6 vs. Flooder 1.0           0-7-0       0-7-0
    Geza's McRobi vs. Goody2              0-7-0       0-7-0
    Martins Echter vs. Goodymorph         0-0-7       0-0-7
    HotBot V2 vs. inter#active            6-0-1       5-0-2
    LastHopeC vs. <Jo's little>           6-0-1       8-0-1     *
    Liquid Ice 1.2 vs. Lauf und Mine      0-7-1       0-4-3     *
    Mikrovirus vs. The Masterkiller 1     0-6-1       0-5-2
    Das Geschwür vs. TheMob.NextG2        0-0-7       0-0-7
    The Overkiller vs. Nullen2            4-1-2      14-0-0     * +
    Rainman vs. RecruteIT                 6-1-0       5-2-0
    Simple vs. Seuche3                    0-0-7       0-0-7
    Styx vs. Sweeper                      0-0-7       0-0-7
    Tie Fighter vs. Test 3-1              0-7-0       0-7-0
    UltimaDefender vs. Tom Himself        0-0-7       0-0-7
    Zwei Arten vs. Zellwucher4            7-0-0       7-0-0

Round 2

    Match                                 Robo Dojo   RoboCom
    ----------------------------------------------------------
    Alien3.4 vs. Alpha                    7-0-1       6-0-1     *
    Conciler vs. Delusion 3.7             0-7-0       0-7-0
    Flooder 1.0 vs. Goody2                1-2-4      1-0-13     * +
    Goodymorph vs. HotBot V2              0-0-0       0-0-0
    LastHopeC vs. Lauf und Mine           0-0-0       0-0-0
    The Masterkiller 1 vs. TheMob.NextG2  0-0-0       0-0-0
    The Overkiller vs. Rainman            0-0-0       0-0-0
    Seuche3 vs. Sweeper                   0-0-0       0-0-0
    Test 3-1 vs. Tom Himself              0-0-0       0-0-0
    Zwei Arten (bye)                      .....       .....

Round 3

    Match                                 Robo Dojo   RoboCom
    ----------------------------------------------------------
    Alien3.4 vs. Delusion 3.7             0-0-0       0-0-0


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