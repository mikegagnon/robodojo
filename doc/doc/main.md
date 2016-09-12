
Robo Dojo is a game where you program bots to battle each other with home-brewed
viruses ([screenshot](##4way)).
Robo Dojo clones the 1998 game [RoboCom](##robocom).

* [Play Robo Dojo](http://robodojo.club)
* [Guide](##guide)
* [Reference Manual](##ref)
* [Debugging Manual](##debugging)

~ref
## Reference Manual

- [Active](##ref-active)
- [Auto Reboot](##auto-reboot)
- [Banks](##ref-banks)
- [Comments](##ref-comments)
- [Steps and Cycles](##ref-cycles)
- [Data Hunger](##ref-data-hunger)
- [Forward Cell & Bot](##ref-forward-bot)
- [Instruction Sets](##ref-instrset)
- [Instructions](##ref-instructions)
- [Labels](##ref-labels)
- [Mobility](##ref-mobility)
- [Parameters](##ref-parameters)
- [Remote Access](##remote-access)

~ref-instructions
## Instructions

There are 12 Robo Dojo instructions

1. [<tt>add a, b</tt>](##add-instruction)
2. [<tt>bjump a, b</tt>](##bjump-instruction)
3. [<tt>comp a, b</tt>](##comp-instruction)
4. [<tt>create a, b, c</tt>](##create-instruction)
5. [<tt>jump a</tt>](##jump-instruction)
6. [<tt>move</tt>](##move-instruction)
7. [<tt>scan a</tt>](##scan-instruction)
8. [<tt>set a, b</tt>](##set-instruction)
9. [<tt>sub a, b</tt>](##sub-instruction)
10. [<tt>tapout</tt>](##tapout-instruction)
11. [<tt>trans a, b</tt>](##trans-instruction)
12. [<tt>turn a</tt>](##turn-instruction)

~sub-instruction
## <tt>sub a, b</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>Subtracts <tt>b</tt> from <tt>a</tt>, then stores the result in
            <tt>a</tt></td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>a</tt> must be writable <br>
            <tt>b</tt> can be any parameter
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>6</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>None</td>
    </tr>

</table>

~set-instruction
## <tt>set a, b</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>Stores the value from <tt>b</tt> into <tt>a</tt>.
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>a</tt> must be writable
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>5</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>None
        </td>
    </tr>

</table>

~scan-instruction
## <tt>scan a</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>If the forward cell contains...
            <ul>
                <li>
                    nothing: then sets <tt>a</tt> to zero
                </li>
                <li>
                    an opponent bot: then sets <tt>a</tt> to 1
                </li>
                <li>
                    a friend bot: then sets <tt>a</tt> to 2
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>a</tt> must be writable
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Advanced</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>6</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>If the bot's instruction set is less than Advanced, then the
            bot taps out.
        </td>
    </tr>

</table>

### See also

- [Forward cell](##ref-forward-bot)
- [Instruction Set](##ref-instrset)

~move-instruction
## <tt>move</tt>


<table>
    </tr>
        <td>Semantics</td>
        <td>During its final cycle, if the forward cell is empty, the bot
        moves into that cell. Otherwise, the instruction does nothing.
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td>N/A
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>18</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>None
        </td>
    </tr>

</table>

### See also

- [Forward cell](##ref-forward-bot)

~jump-instruction
## <tt>jump a</tt>


<table>
    </tr>
        <td>Semantics</td>
        <td>If <tt>a</tt> is a label, then jump to the first instruction
           that appears after that label. Otherwise (if <tt>a</tt> is
           an integer), then jump <tt>a</tt> instructions forward or backward
           (depending on if <tt>a</tt> is positive or negative).

           For example, if <tt>a</tt> is 3, then jump to the third instruction
           that appears after the <tt>jump</tt> instruction.
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>a</tt> can be any parameter. Also, it can be a label.
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>2</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>If the instruction jumps out of bounds (i.e. out side of its bank),
        then an Auto Reboot happens, possibly leading to Data Hunger.
        </td>
    </tr>

</table>

### See also

- [Labels](##ref-labels)
- [Auto Reboot](##auto-reboot)
- [Data Hunger](##ref-data-hunger)

~create-instruction
## <tt>create iset, banks, mobile</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>
        On its final cycle:

        <ul>
         <li> if the forward cell is occupied, then does nothing.</li>
         <li> if the forward cell is empty, then deploys a new bot in the
             forward cell:
                <ul>
                    <li>The new bot's <tt>$instrset</tt>, <tt>$banks</tt>, &
                        <tt>$mobile</tt> values are set to the
                        <tt>iset</tt>, <tt>banks</tt>, & <tt>mobile</tt> values
                        from the instruction.
                    </li>
                    <li>The new bot is initially deactivated</li>
                    <li>Each of the new bot's banks is empty</li>

                </ul>
        </ul>
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td>
            <tt>iset</tt>, <tt>banks</tt>, & <tt>mobile</tt> can be any
            parameter
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Super</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>
            
      <pre>
(50 + 20 * <tt>banks</tt>) *
(if (mobile == 1) 2 else 1) +
(if (iset == Advanced) 40 else 0) +
(if (iset == Super) 80 else 0) </pre>


        </td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>
        The parent bot taps out if any of the following events occur:
        <ul>
            <li>If the parent's <tt>$instrset</tt> is less than Super</li>
            <li>If <tt>iset</tt> < 0 or <tt>iset</tt> > 2</li>
            <li>If <tt>banks</tt> < 1 or <tt>banks</tt> > 50</li>
            <li>IF <tt>mobile</tt> < 0 or <tt>mobile</tt> > 1</li>


        </ul>

        </td>
    </tr>
</table>

### See also

- [Instruction Sets](##ref-instrset)
- [Banks](##ref-banks)
- [Mobility](##ref-mobility)

~add-instruction
## <tt>add a, b</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>Sums <tt>a</tt> and <tt>b</tt>, then stores the result in
            <tt>a</tt></td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>a</tt> must be writable <br>
            <tt>b</tt> can be any parameter
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>6</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>None</td>
    </tr>

</table>

~bjump-instruction
## <tt>bjump bank, instruction</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>
            Sets the instruction pointer to bank <tt>bank</tt>, instruction
            <tt>instruction</tt>
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td><tt>bank</tt> and <tt>instruction</tt> can be any parameter. <br>
        <tt>instruction</tt> can also be a label
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>5</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>
            <ul>
               <li>If <tt>bank</tt> > 50 or <tt>bank</tt> < 1,
                    then the bot taps out
               </li> 
               <li>Else if the bot does not have the specified <tt>bank</tt>,
               or the specified <tt>instruction</tt>, then the bot Auto
               Reboots
            </ul>
        </td>
    </tr>
</table>

### See also

- [Banks](##ref-banks)
- [Labels](##ref-labels)
- [Auto Reboot](##auto-reboot)


~comp-instruction
## <tt>comp a, b</tt>

<table>
    </tr>
        <td>Semantics</td>
        <td>If <tt>a</tt> == <tt>b</tt>, then the next instruction is skipped;
        otherwise, the next instruction is executed as normal.
        </td>
    </tr>
    <tr>
        <td>Data types</td>
        <td>
            <tt>a</tt> and <tt>b</tt> can be any parameter.
        </td>
    </tr>
    <tr>
        <td>Instruction set</td>
        <td>Basic</td>
    </tr>
    <tr>
        <td>Number of cycles</td>
        <td>3</td>
    </tr>
    <tr>
        <td>Runtime exceptions</td>
        <td>
            None
        </td>
    </tr>
</table>



~ref-labels
## Labels

Labels mark places in your program for [<tt>jump</tt>](##jump-instruction)
and [<tt>bjump</tt>](##bjump-instruction) instructions.

### Example 1

    @foo
    add #1, 1
    jump @foo

### Example 2

    bank main ; bank 1
        bjump 4, @start

    bank foo ; 2
    bank bar ; 3

    bank baz ; 4
        create 1, 1, 1
        
        @start
        turn 1






~ref-mobility
## Mobility

Each bot is either mobile or immobile.

Parent bots determine whether their children are mobile or immobile.

The `$mobile` constant is `1` if the bot is mobile, and `0` if the bot
is immobile.

An immobile bot taps out if it attempts to [<tt>move</tt>](##move-instruction).

### See also

- [<tt>create</tt> instruction](##create-instruction)

~ref-instrset
## Instruction Sets

Different instructions belong to different Instruction Sets.

There are three instruction sets:

- [Basic](##ref-instrset-basic)
- [Advanced](##ref-instrset-advanced)
- [Super](##ref-instrset-super)

### The <tt>$instrset</tt> constant

Each bot has an instruction-set constant: `$instrset` 

- If `$instrset` == 0, then the bot can only execute Basic instructions
- If `$instrset` == 1, then the bot can execute Basic and Advanced instructions
- If `$instrset` == 2, then the bot can execute all instructions

### Initializing <tt>$instrset</tt>

The `$instrset` value is determined when the bot is created. See the
[<tt>create</tt> instruction](##create-instruction).

~ref-instrset-basic
## Basic Instruction Set

The following instructions are in the Basic Instruction Set:

- [<tt>add a, b</tt>](##add-instruction)
- [<tt>bjump a, b</tt>](##bjump-instruction)
- [<tt>comp a, b</tt>](##comp-instruction)
- [<tt>jump a</tt>](##jump-instruction)
- [<tt>move</tt>](##move-instruction)
- [<tt>set a, b</tt>](##set-instruction)
- [<tt>sub a, b</tt>](##sub-instruction)
- [<tt>tapout</tt>](##tapout-instruction)
- [<tt>turn a</tt>](##turn-instruction) 

~ref-instrset-advanced
## Advanced Instruction Set

The following instructions are in the Advanced Instruction Set:

- [<tt>scan a</tt>](##scan-instruction)
- [<tt>trans a, b</tt>](##trans-instruction) 

~ref-instrset-super
## Super Instruction Set

There is only one instruction in the Super Instruction Set:

- [<tt>create a, b, c</tt>](##create-instruction)

~ref-data-hunger
## Data Hunger

If a bot finds an empty first bank after an [Auto Reboot](##auto-reboot), or
upon [activation](##ref-active) for the first time, then the bot
taps out from "Data Hunger."

~ref-comments
## Comments

You can include comments in your program with semicolons.

For example:

    bank main        ; bank #1
    comp 1, #2       ; if #2 == 1, skip the next instruction

~auto-reboot
## Auto Reboot

An Auto Reboot is when a bot jumps to bank 1, instruction 1 due to a
non-fatal error. There are four events that can lead to an Auto Reboot.

1. If a bot's execution reaches the end of its current bank, and the last
   instruction is not a [<tt>jump</tt>](##jump-instruction) instruction.
2. If a bot [<tt>bjump</tt>](##bjump-instruction)&#8217;s to a bank that does not
   exist. However, if the `bjump` jumps to a bank that couldn't possibly exist
   (such as bank 51), then the bot taps out.
3. If a bot's current bank is overwritten by an empty bank
4. If a bot's current bank is overwritten by a smaller bank, and the bot's
   instruction pointer becomes invalid

~ref-banks
## Banks

Every program is segmented into banks; every instruction belongs to exactly
one bank.

A bot can have a maximum of 50 banks.

The [<tt>bjump</tt> instruction](##ref-bank-instruction) is used to
jump between banks.

The [<tt>trans</tt> instruction](##ref-trans-instruction) is used to transfer
a bank from one bot to its forward bot.

If a bot's execution reaches the end of a bank, then the bot performs
an [Auto Reboot](##ref-auto-reboot).

~remote-access
## Remote Access

Every bot can remotely access its [forward bot's](##ref-forward-bot) data to a limited extent.

The key is using [remote parameters](##ref-parameters-remote).

For example, a bot can obtain the number of banks in the forward bot, by
using the `%banks` parameter.

### Default values

If an instruction attempts to read a remote value, but the forward cell is empty,
then the remote value is read as zero.

### Remote-access cost

Every instruction requires a certain number of cycles to execute.

When an instruction accesses a remote parameter (either reading or writing)
then the instruction takes longer to execute.

Specifically, the number of required cycles increases by `8` for every
remote parameter that is accessed.


~ref-forward-bot
## Forward bot

Say *Bot X* is some bot on the board.

Relative to Bot X, the term *forward cell* refers to the cell directly
in front of Bot X.

If there is a bot in that cell, it is referred to as the *forward bot*.


~ref-parameters
## Parameters

Most instructions take parameters. [For example...](##ref-parameters-example)

Every parameter has the data type of 16-bit integer.

### Read, write, local, & remote

Every parameter is *readable*.

Some parameters are [*writable*](##ref-parameters-writable),
[*local*](##ref-parameters-local), or [*remote*](##ref-parameters-remote).

### Parameter list







<table>
    <tr>
        <td><code>-32768</code> ... <code>32767</code></td>
        <td>These are 16-bit integer literals.</td>
    </tr>
    <tr>
        <td><code>#1</code> ... <code>#20</code></td>
        <td>These local registers store arbitrary integer values</td>
    </tr>
    <tr>
        <td><code>#active</code></td>
        <td>This is a special register: setting <code>#active</code> to 0 or negative deactivates the bot.</td>
    </tr>
    <tr>
        <td><code>%active</code></td>
        <td><code>%active</code> is the only remote parameter that is writable. Setting <code>%active</code> to 0 or negative
            deactivates the forward bot. In contrast, setting <code>%active</code> to a positive value activates the forward bot.</td>
    </tr>
    <tr>
        <td><code>$banks</code></td>
        <td>The number of banks in the local bot.</td>
    </tr>
    <tr>
        <td><code>%banks</code></td>
        <td>The number of banks in the forward bot.</td>
    </tr>
    <tr>
        <td><code>$fields</code></td>
        <td>The number of rows on the board.</td>
    </tr>
    <tr>
        <td><code>$instrset</code></td>
        <td>The instruction set of the local bot. <code>0</code> signifies
        the <i>Basic</i> instruction set; <code>1</code> signifies the
        <i>Advanced</i> instruction
        set; <code>2</code> signifies the <i>Super</i> instruction set.</td>
    </tr>
    <tr>
        <td><code>%instrset</code></td>
        <td>The instruction set of the forward bot. <code>0</code> signifies
        the <i>Basic</i> instruction set; <code>1</code> signifies the
        <i>Advanced</i> instruction
        set; <code>2</code> signifies the <i>Super</i> instruction set.</td>
    </tr>
    <tr>
        <td><code>$mobile</code></td>
        <td><code>0</code> if the bot is immobile; <code>1</code>
        if the bot is mobile.</td>
    </tr>
    <tr>
        <td><code>%mobile</code></td>
        <td><code>0</code> if the forward bot is  immobile; <code>1</code>
        if the forward bot is mobile.</td>
    </tr>

</table>
~ref-parameters-example
## For example

`set %active, 1` has parameters `%active` and `1`.


~ref-parameters-remote
## Remote parameters

Remote parameters refer to values in the [forward bot](##ref-forward-bot).
If the remote cell is empty though, then every remote parameter yields the 
value `0`.

Every remote parameter begins with `%`.

There is only one [writable](##ref-parameters-writable) remote parameter:
`%active`. The rest are read-only.

### Examples

- `set %active, 1` sets the forward bot's `#active` register to 1.
- `set #3, %banks` sets the local register `#3` to the number-of-banks
  in the forward bot. Or, sets `#3` to zero if the forward cell is 
  empty.

## List of all remote parameters

<table>
    <tr>
        <td><code>%active</code></td>
        <td><code>%active</code> is the only remote parameter that is writable. Setting <code>%active</code> to 0 or negative
            deactivates the forward bot. In contrast, setting <code>%active</code> to a positive value activates the forward bot.</td>
    </tr>
    <tr>
        <td><code>%banks</code></td>
        <td>The number of banks in the forward bot.</td>
    </tr>
    <tr>
        <td><code>%instrset</code></td>
        <td>The instruction set of the forward bot. <code>0</code> signifies
        the <i>Basic</i> instruction set; <code>1</code> signifies the
        <i>Advanced</i> instruction
        set; <code>2</code> signifies the <i>Super</i> instruction set.</td>
    </tr>
    <tr>
        <td><code>%mobile</code></td>
        <td><code>0</code> if the forward bot is  immobile; <code>1</code>
        if the forward bot is mobile.</td>
    </tr>
</table>

### See also

- [Active](##ref-active)
- [Remote Access](##remote-access)

~ref-parameters-local
## Local parameters

When a parameter is *local*, that means that the value is stored inside the
bot that is executing the instruction.

Local parameters begin with either `#` or `$`, or are an integer literal such as
`5`.

Parameters that begin with `#`
are [writable](##ref-parameters-writable).

Parameters that begin with `$` are not writable -- they are local constants.

### List of all local parameters

<table>
    <tr>
        <td><code>-32768</code> ... <code>32767</code></td>
        <td>These are 16-bit integer literals.</td>
    </tr>
    <tr>
        <td><code>#active</code></td>
        <td>This is a special register: setting <code>#active</code> to 0 or negative deactivates the bot.</td>
    </tr>
    <tr>
        <td><code>$banks</code></td>
        <td>The number of banks in the local bot.</td>
    </tr>
    <tr>
        <td><code>$instrset</code></td>
        <td>The instruction set of the local bot. <code>0</code> signifies
        the <i>Basic</i> instruction set; <code>1</code> signifies the
        <i>Advanced</i> instruction
        set; <code>2</code> signifies the <i>Super</i> instruction set.</td>
    </tr>
    <tr>
        <td><code>$mobile</code></td>
        <td><code>0</code> if the bot is immobile; <code>1</code>
        if the bot is mobile.</td>
    </tr>
    <tr>
        <td><code>$fields</code></td>
        <td>The number of rows on the board.</td>
    </tr>
</table>

### See also

- [Instruction Sets](##ref-instrset)
- [Banks](##ref-banks)
- [Mobility](##ref-mobility)
- [Active](##ref-active)

~ref-parameters-writable
## Writable parameters

Most writable parameters begin with `#`. The only exception is the writable
parameter `%active`.

### List of all writable parameters

<table>
    <tr>
        <td><code>#1</code> ... <code>#20</code></td>
        <td>These local registers store arbitrary integer values</td>
    </tr>
    <tr>
        <td><code>#active</code></td>
        <td>This is a special register: setting <code>#active</code> to 0 or negative deactivates the bot.</td>
    </tr>
    <tr>
        <td><code>%active</code></td>
        <td>A reference to the forward bot's <code>#active</code> register.
        <code>%active</code> is the only remote parameter that is writable. Setting <code>%active</code> to 0 or negative
            deactivates the forward bot. In contrast, setting <code>%active</code> to a positive value activates the forward bot.</td>
    </tr>
</table>

### See also

- [Forward bot](##ref-forward-bot)
- [Remote Access](##remote-access)
- [Active](##ref-active)

~ref-cycles
## Steps and Cycles

Simulations progress one step at a time.

Each step, each bot on the board executes a single cycle. The bots are
executed in order from oldest bot to youngest bot.

Every instruction requires a certain number of cycles before it executes.

For example the [<tt>turn</tt> instruction](##ref-turn) requires 5 cycles.
On its 5th cycle, the <tt>turn</tt> operation actually goes into effect.

**TODO**: Linkify

~ref-active
## Active

For any given bot, at any given moment: the bot is either *active* or
*deactivated*.

If a bot is active, then it will execute a [cycle](##ref-cycles)
during its turn during the current step.

If a bot is deactivated, then it will not execute anything -- until it becomes
activated.

For a bot to become activated, its `#active` register must be set to 1 or
greater. Note: a bot cannot activate itself since it cannot execute. To
activate a deactivated bot, another but must set `#active` by using
[Remote Access](##remote-access).


~robocom
## RoboCom

RoboCom is no longer actively maintained but its website is archived
[here](http://robocom.rrobek.de/). Here's a [screenshot](##robocomscreenshot).

Robo Dojo only implements the "Original" instruction set from RoboCom.

Robo Dojo uses the "Classic" timing model from RoboCom.

I have strove to make the Robo Dojo simulation semantics identical to RoboCom --
however [I could not create a cycle-for-cycle duplicate of RoboCom](##compatibility).

Nevertheless, Robo Dojo and RoboCom have [similar](##rdrc-battles) semantics.

~4way
## Screenshot of a four-way battle

<img src="img/battle.png">

- Each tear-drop shape is a different bot
- If a bot contains a circle of a different color, that means the bot
  is infected
- If a bot has a white line through it, that means the bot is deactivated


~guide
## Guide

Through 8 case studies, this guide teaches you the essentials of Robo Dojo
programming. This guide assumes you are already a programmer.

1. [Hello World](##guide-hello)
2. [Replication](##replication)
3. [Diamond](##diamond)
4. [Infection & Disinfection](##blue-red-programs)
5. [Super Diamond](##defeating-diamond)
6. [Bank Jumper](##bank-jumping)
7. [Prototype Virus](##eng-virus)
8. [Empty Banks](##empty-banks)

[Review](##review)

~review
## Review

You have now learned all 12 Robo Dojo instructions.

1. `add #a, b`
2. `bjump a, b`
3. `comp a, b`
4. `create a, b, c`
5. `jump a`
6. `move`
7. `scan #a`
8. `set #a, b`
9. `sub #a, b`
10. `tapout`
11. `trans a, b`
12. `turn a`

You have also learned about Auto Reboot, Data Hunger, and some design patterns
for Robo Dojo programs.

You can progress your skills by studying the [Reference Manual](##ref) and 
other Robo Dojo programs (which you can access via [Robo Dojo](http://robodojo.club)
itself).

~empty-banks
## Empty Banks

[Bank Jumper](##bank-jumping) taught us that we can't get away with just
attacking the first bank -- since our opponent may be operating out of any
bank.

So now let's build a program that can defeat Bank Jumper variants operating out
of any bank.

The idea is to fork Bank Jumper and replace its `@foe` routine with the
following pseudo code:

    for (int i = 1; i <= 50; i++) {
        transfer [empty bank] to bank i
        if (forward cell does not contain opponent) {
            break
        }
    } 

The routine causes [Data Hunger](##empty-banks-data-hunger) in the opponent,
which causes the opponent to tap out.

To implement the new `@foe` routine, we need to learn the
[add instruction](##empty-banks-add).

Here is the [complete program](##empty-banks-program).

~empty-banks-add
## <tt>add</tt> and <tt>sub</tt>

`add #a, b` adds `#a` and `b` together, then stores the result in `#a`.

`sub #a, b` subtracts `b` from `#a`, then stores the result in `#a`.

~empty-banks-data-hunger
## Data Hunger

- Let's say an opponent bot is running on bank *n*.
- If we overwrite bank *n* with an empty bank, then the opponent will perform
  an [Auto Reboot](##auto-reboot)
- The Auto Reboot causes the opponent to restart at bank 1, instruction 1
- If bank 1 is empty, then the opponent taps out due to "Data Hunger"

**TODO**: Link to auto reboot

~empty-banks-program
## Complete program

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,3,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        set %active, 1
        turn 1
        jump @start

        @foe
        set #2, 1

        @foe-loop
        comp #2, 51
        jump @foe-infect

        ; end of loop
        jump @start

        @foe-infect
        trans 2, #2
        set %active, 1

        add #2, 1
        
        scan #3
        comp #3, 1
        jump @start
        jump @foe-loop


    


~eng-virus
## Prototype Virus

We are going to fork Bank Jumper, and modify it to use a virus to defeat
Super Diamond.

### Sections

- [Why use viruses?](##why-virus)
- [Timing concerns](##eng-virus-timing)
- [Prototype Virus 1](##eng-virus-prototype)
- [Prototype Virus 2](##eng-virus-prototype-2)
- [Prototype Virus 3](##eng-virus-prototype-3)

~eng-virus-prototype-3
## Prototype Virus 3

PV3 modifies PV2 so that sometimes it taps out opponents.
Thus, it is able to both infect and eliminate Super Diamond bots.

In PV2 the `@foe` section of the program infects opponents with the virus.

For PV3, we rewrite `@foe` as follows:

    If (#20 == 0) {
        #20 = 1
        infect with tapout malware
    } else {
        #20 = 0
        infect with virus
    }

Here's the [actual code](##pv3-foe) for the `@foe` section, and here's the
[complete program](##pv3-program).

PV3 successfully completely eliminates Super Diamond.

~pv3-foe
## Actual code for `@foe` section of PV3

    @foe

    comp #20, 0
    jump @infect-with-virus

    set #20, 1
    trans 5,1
    jump @restart

    @infect-with-virus
    ; LOOK HERE: Transfer self-destruct malware to the opponent
    set #20, 0
    trans 4,1

    @restart
    ; Make sure the opponent is active, so it can execute the foe bank
    set %active, 1
    jump @start

~pv3-program
## Complete program

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,5,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        trans 5,5
        set %active, 1
        turn 1
        jump @start

        @foe
        
        comp #20, 0
        jump @infect-with-virus

        set #20, 1
        trans 5,1
        jump @restart
        
        @infect-with-virus
        ; LOOK HERE: Transfer self-destruct malware to the opponent
        set #20, 0
        trans 4,1
        
        @restart
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start

    bank foe ; 4
        trans 1,1
        turn 1
        bjump 3, 1
        
    bank tapout; 5
        tapout



~eng-virus-prototype-2
## Prototype Virus 2

PV2 modifies PV1 so that it heals from self-infection.

We replace the old payload with a new payload:

    bank foe
        trans 1,1
        turn 1
        bjump 3, 1

Here is the [complete program](##pv2-program).

For Super Diamond bots, the new payload behaves [similar](##pv2-sd-comparison) to the old payload.

For PV2 bots, the new payload behaves [differently](##pv2-payload-comparison) compared to the old payload. 

Consequently, PV2 bots don't become stuck and they completely surround the 
infected Super Diamond bots ([screenshot](##pv2-screenshot)).

But, the PV2 bots still don't have a mechanism to tap out the infected Super
Diamond bots.

~pv2-screenshot
## PV2 screenshot

<img src="img/pv-2.png">

~pv2-sd-comparison
## PV2 payload on Super Diamond bots

Super Diamond bots never have a third bank, so when they execute `bjump 3,1`
it causes a [Auto Reboot](##pv2-auto-reboot), which results in the bot
restarting at bank 1.

Therefore, the new payload does not affect the way Super Diamond bots execute
the new virus payload (except for timing, since `bjump` takes up a few cycles).

~pv2-payload-comparison
## PV2 payload on PV2 bots

Since bank 3 is the main bank for PV2 bots, when a PV2 bot executes the virus 
payload it causes the PV2 bot to resume its execution as normal.

~pv2-auto-reboot
## Auto Reboot

There are [many events](##autoreboot) that can trigger an Auto Reboot.

Among them, if a bot executes a `bjump` instruction to a bank that doesn't
exist, then the bot will Auto Reboot.

A bot reboots by jumping to the first instruction of the first bank.

If the first bank is empty, then the bot taps out from "Data Hunger."

**TODO**: Link to Auto Reboot.

~pv2-program
## PV2 Program

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,4,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        set %active, 1
        turn 1
        jump @start

        @foe
        ; LOOK HERE: Transfer self-destruct malware to the opponent
        trans 4,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start

    bank foe ; 4
        trans 1,1
        turn 1
        bjump 3, 1

~eng-virus-prototype
## Prototype Virus 1

Recall, [Super Diamond](##defeating-diamond) and [Bank Jumper](##bank-jumping)
defeat individual bots by infecting them with tapout malware:

    bank foe
        tapout

We are going to replace that payload with a simple virus:

    bank foe
        trans 1,1
        turn 1

Here's the [complete program](##pv1-program).

This virus spreads quickly because its execution time is small compared to
the disinfection routine. Here are some snapshots of Prototype Virus 1 taking
on Super Diamond:
[1](##pv-1-a),
[2](##pv-1-b), 
[3](##pv-1-c), 
[4](##pv-1-d), 
[5](##pv-1-e), 
[6](##pv-1-f), 
[7](##pv-1-g), 
[8](##pv-1-h), 

Unfortunately, the virus is so effective it ends up
[infecting Blue's bots](##pv1-self-infect) at the frontier.

Furthermore, Prototype Virus 1 has no mechanism to tapout its opponent;
it merely spreads a virus.

~pv1-program
## PV1 Program

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,4,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        set %active, 1
        turn 1
        jump @start

        @foe
        ; LOOK HERE: Transfer self-destruct malware to the opponent
        trans 4,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start

    bank foe ; 4
        trans 1,1
        turn 1

~pv1-self-infect
## Self infection

At first glance, it might not seem possible that Prototype Virus 1
could end up infecting its own bots -- since PV1 spreads its virus through
bank 1 and PV1 operates out of bank 3.

A little [debugging](##debugging) shows what happens:

1. A PV1 bot creates a child bot, and transfers bank 1 to bank 1
2. An infected bot infects the child
3. The PV1 parent then transfers the rest of the banks to the child
4. The parent activates the child
5. The child executes the virus bank

**TODO**: link to debugging

~pv-1-a
## Step 0

<img src="img/pv-1-a.png" >

~pv-1-b
## Step 1229

<img src="img/pv-1-b-1229.png" >

Blue and Red make contact.

~pv-1-c
## Step 1279

<img src="img/pv-1-c-1279.png" >

Blue infects two Red bots with the virus.

~pv-1-d
## Step 1307

<img src="img/pv-1-d-1307.png" >

The virus begins to spread.

~pv-1-e
## Step 1387

<img src="img/pv-1-e-1387.png" >

~pv-1-f
## Step 1460

<img src="img/pv-1-f-1460.png" >

The Red team is completely infected with the virus.

~pv-1-g
## Step 4037

<img src="img/pv-1-g-4037.png" >

The Blue team appears to be closing in on Red.

~pv-1-h
## Step 13,634

<img src="img/pv-1-h-13634.png" >

Blue is stuck because its frontier bots are infected with the virus.

~eng-virus-timing
## Timing concerns
Up until now, minor timing issues haven't played a role in the development of
our programs. But viruses must be quick, in order to overwhelm disinfection.

~why-virus
## Why use viruses?

It usually takes at least several hundred cycles to create a new bot.

On the other hand,
it only takes several tens of cycles to infect a bot with a small code bank.

So, commandeering a bot is more efficient than creating a bot by an order of
magnitude.


~bank-jumping
## Bank Jumper

We can develop a program that defeats Super Diamond, by forking Super Diamond
and making a few small changes.

Basically, we use the <tt>[bjump](##bank-jumping-bjump)</tt> instruction to jump away from the first
bank. Since Super Diamond attacks the first bank, it easily evades Super
Diamond's attack.

The Bank Jumper program looks like [this](##bank-jumping-program).

In general, if you jump to arbitrary banks, its more difficult to attack you
since the attacker doesn't know which bank to hit. Of course, the attacker
can target every bank, but that leads to a slower attack.

~bank-jumping-bjump
## bjump

The `bjump` instruction jumps from one bank to another.

It takes two parameters:

1. The bank number to jump to
2. The instruction number (within that bank) to jump to

### Example usage
    
    bank one
    bjump 3, 1

    bank two

    bank three
    move

This causes the bot to jump to the first instruction of the third bank.

~bank-jumping-program
## Bank Jumper program

We point out the differences from Super Diamond by putting "LOOK HERE" in the
comments.

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,4,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        set %active, 1
        turn 1
        jump @start

        @foe
        ; LOOK HERE: Transfer self-destruct malware to the opponent
        trans 4,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start

    bank foe ; 4
        tapout

~wave
## Wave Virus



Bank Jumper modified to use virus instead of tapout

    bank launcher ; 1

        ; jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,4,0

        ; Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        set %active, 1
        turn 1
        jump @start

        @foe
        ; LOOK HERE: Transfer wave virus to the opponent
        trans 4,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start
        
    bank wave-virus ;4
        turn 1
        trans 1,1
        bjump 3,1

Wave Virus with submission hold

    bank launcher ; 1

        ; LOOK HERE: jump to bank 3 to evade Super Diamond's attack
        bjump 3,1

    bank 2

    bank main ; 3

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,5,0

        ; LOOK HERE: Disinfect / initialize new bot
        trans 1,1
        trans 3,3
        trans 4,4
        trans 5,5
        set %active, 1
        turn 1
        jump @start

        @foe
        ; LOOK HERE: Transfer self-destruct virus to the opponent
        trans 4,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        trans 5,1
        jump @start

    bank virus ;4
        trans 1,1
        turn 1
        bjump 3,1
        
    bank tapout ;5
        tapout


        
    
    
    


        

~defeating-diamond
## Super Diamond

Let's build a program that defeats the Diamond program.

The general idea is to infect Diamond bots with malware that causes the 
infected bots to "tap out" -- i.e. exit the board.

The [pseudo code for our Super Diamond program](##defeating-diamond-pseudo)
is straightforward and simple.

To implement it, we need to learn about [registers](##defeating-diamond-registers)
and four new instructions:

- [tapout](##defeating-diamond-tapout)
- [scan](##defeating-diamond-scan)
- [comp](##defeating-diamond-comp)
- [jump](##defeating-diamond-jump)

Putting it all together, here is the [Super Diamond program](##Super-diamond-program).

The battle should look something like [this](##Super-diamond-battle).

~defeating-diamond-tapout
## <tt>tapout</tt>

When executed, the `tapout` instruction causes the bot to exit the board.

~defeating-diamond-scan
## <tt>scan</tt>

The `scan` instruction looks at the cell in front of the bot.

`scan variable` sets `variable` to either 0, 1, or 2, depending on what is 
in front of the bot.

- `variable` = 0 if the front cell is empty
- `variable` = 1 if the front cell contains an opponent
- `variable` = 2 if the front cell contains a friend

### Example usage

`scan #1` sets `#1` to either 0, 1, or 2.

~defeating-diamond-registers
## Registers

Bots can read and write registers.

Each bot has 20 registers: `#1` ... `#20`

### Example usage

    set #1, 0
    turn #1

This program segment sets `#1` equal to zero. Then `turn #1` is equivalent to 
`turn 0`.

~defeating-diamond-comp
## <tt>comp</tt>

The `comp a, b` instruction compares `a` and `b`.

If `a` and `b` are equal, then the following instruction is skipped.

Otherwise, the following instruction is executed as normal.

### Example usage

    comp #1, 0
    turn 1
    move

If `#1` == 0, then the bot does not turn.

Otherwise, the bot does turn.

~defeating-diamond-jump
## <tt>jump</tt>

Example usage:

    @foo
    turn 1
    move

    jump @foo

The bot turns and moves in an endless loop.

~Super-diamond-battle
## Super Diamond battle

1. [Diamond (red) starts out with more bots](##ad-battle-1), since it's executing fewer instructions
2. [Diamond begins to experience tapouts](##ad-battle-2)
3. [Super Diamond catches up with Diamond](##ad-battle-3)
4. [Super Diamond overwhelms Diamond](##ad-battle-4)
5. [Super Diamond wins](##ad-battle-5)

~ad-battle-1
<img src="img/anti-diamond-1.png">
~ad-battle-2
<img src="img/anti-diamond-2.png">
The red squares indicate a Red bot has just tapped out there.
~ad-battle-3
<img src="img/anti-diamond-3.png">
~ad-battle-4
<img src="img/anti-diamond-4.png">
~ad-battle-5
<img src="img/anti-diamond-5.png">

~Super-diamond-program
## Super Diamond program

    bank main

        @start

        ; Register #1 = "empty", or "opponent", or "friend" 
        scan #1

        ; if "opponent" goto @foe
        ; else goto @friend-or-empty
        comp #1, 1
        jump @friend-or-empty
        jump @foe


        @friend-or-empty

        ; if "friend" skip the follow create instruction
        comp #1, 2
        create 2,2,0

        ; Disinfect / initialize new bot
        trans 1,1
        trans 2,2
        set %active, 1
        turn 1
        jump @start

        @foe
        ; Transfer tapout-malware to the opponent
        trans 2,1
        ; Make sure the opponent is active, so it can execute the foe bank
        set %active, 1
        jump @start

    bank foe
        tapout



~defeating-diamond-pseudo
## Pseudo code for Super Diamond

    bank main

        if (forward cell is empty)
            clone self
            turn
        else if (forward cell is an opponent)
            infect the opponent with tapout malware
        else if (forward cell is friendly)
            disinfect friend
            turn

    bank self-destruct
        self destruct

~blue-red-programs
## Infection & Disinfection

You can launch different-colored bots on the board, each color with a different
program.

To edit the program for a color, select a color from the
[color editor drop-down menu](##color-dropdown).

For this experiment:

- Set the Blue bot program to the [Replication program](##replication)
- Set the Red bot program to the [Diamond program](##diamond)

The result should look like [this](##infection-screenshot-1), or
[this](##infection-screenshot-2), [this](##infection-screenshot-3), or
maybe [this](##infection-screenshot-4).

The Red bots overwhelm the Blue bots, and
[infect them with Red's code banks](##infection-explanation).

Sometimes [Blue bots infect Red bots](##infection-screenshot-4), but Red bots quickly
[disinfect themselves](##disinfection).

~disinfection
## Red bots quickly disinfect themselves

This is because
every Red bot is more-or-less surrounded by other Red bots who are constantly
propagating their Red banks. Thus, when a Red bot becomes infected, its
neighboring Red bots disinfect the infected Red bot.

~infection-explanation
## Infection explanation

Each Blue bot has a [Red circle inside it](##blue-infected-picture),
which indicates each Blue bot
is executing a bank that originated from Red's program.

This happens because Red and Blue indiscriminately transfer their first bank 
to the bots in front of them.

So, when a Blue bot is infected with a Red bank, the Blue bot begins to turn
as it reproduces, propagating Red's bank along the way.

~blue-infected-picture
## An infected blue bot

<img src="img/infected-bluebot.png">

~infection-screenshot-4
## Infection Screenshot 4

<img src="img/infection-screenshot-4.png">

Notice the Red bot that has been infected by Blue.

~infection-screenshot-1
## Infection Screenshot 1

<img src="img/infection-screenshot-1.png">

~infection-screenshot-2
## Infection Screenshot 2

<img src="img/infection-screenshot-2.png">

~infection-screenshot-3
## Infection Screenshot 3

<img src="img/infection-screenshot-3.png">


~color-dropdown
## Color editor drop-down menu

<img src="img/color-dropdown.png">

~diamond
## Diamond

Adding a `turn` instruction to the Replication program builds a cluster of
bots in a diamond shape.

    bank main
    create 2,1,0
    trans 1,1
    set %active, 1
    turn 1

[Screenshot](##guide-diamond-screenshot)

### How it works

- [The <tt>turn</tt> instruction](##guide-diamond-turn)

~guide-diamond-turn
## The <tt>turn</tt> instruction

The `turn` instruction takes one parameter.

- If the parameter is 0, the bot turns to the left (rotates -90°).
- If the parameter is anything else, the bot turns to the right (rotates 90°).


~guide-diamond-screenshot
## Screenshot

<img src="img/diamond.png">

~replication
## Replication

The following program causes your bot to clone itself, then its child clones
itself, and so on:

    bank main
    create 2,1,0
    trans 1,1
    set %active, 1

[Screenshot](##guide-repl-screenshot)

### How it works

- [The <tt>create</tt> instruction](##guide-repl-create)
- [The <tt>trans</tt> instruction](##guide-repl-trans)
- [The <tt>set</tt> instruction](##guide-repl-set)

~guide-repl-screenshot
## Screenshot

<img src="img/replicate.png">

~guide-repl-create
## The <tt>create</tt> instruction

The `create` instruction builds a new bot in front of the builder.

There are three parameters:

1. [Instruction set](##guide-repl-create-instrset)
2. [Number of banks](##guide-repl-create-numbanks)
3. [Mobility](##guide-repl-create-mobility)

The `create` instruction is the [slowest instruction](##guide-repl-create-slow).

During the `create` instruction's last cycle, if the forward cell is empty, then
the child will be created in front of the parent. The child bot will have empty
banks and will be "inactive" (which is why
we need to execute the `trans` and `set` instructions after `create`). 

Thus, `create 2,1,0` creates an immobile bot with the Super instruction set and
one empty bank.

~guide-repl-create-slow
## Slowest instruction

Typically, the `create` instruction requires at least several hundred cycles.

- The greater the instruction set, the more cycles are required
- The more banks, the more cycles required
- If the child is to be mobile, the more cycles required


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

The `trans` instruction uploads one bank from one bot to another.

There are two parameters:

1. Source bank number
2. Destination bank number

During the `trans` instruction's last cycle, if the forward cell is occupied,
then the bot copies its Source bank into the forward bot's Destination bank,
overwriting any bank that might already be there.

Thus `trans 1,1` copies the <tt>main</tt> bank from parent to child.

~guide-repl-set
## The <tt>set</tt> instruction

The `set` instruction assigns a value to a variable.

There are two parameters:

1. Variable destination
2. Source value

Thus, `set %active, 1` assigns the value `1` to the variable `%active`.

`%active` is a special variable: setting it to `1` (or greater)
causes the forward bot to become activated.


**TODO**: Link to article on variables and constants

~guide-hello
## Hello World

The following program causes your bot to move across the board:

    bank main
    move

- [How to run your program](##guide-hello-run)
- [Screenshot](##guide-hello-screenshot)

### How it works

- [Editors](##guide-hello-editor)
- [Execution model](##guide-hello-execution)
- [Compile button](##guide-hello-compile)
- [The <tt>bank</tt> directive](##guide-hello-bank)
- [The <tt>move</tt> instruction](##guide-hello-move)
- [Auto Reboot](##guide-hello-autoreboot)

~guide-hello-screenshot
## Screenshot

<img src="img/hello-world.png">

~guide-hello-autoreboot
## Auto Reboot

Whenever a bot reaches the end of its bank, the bot Auto Reboots -- which means
the bot's execution starts over at the first instruction of the first bank.

In our Hello World program, Auto Reboot causes the bot to repeatedly execute
the move instruction.

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

To see how closely Robo Dojo matches RoboCom semantics, I ran a
Tournament of programs using [programs from the July Package](##july-package).

The [results](##rdrc-battles-details) show that Robo Dojo semantics are similar
to RoboCom semantics: of the 38 matches in the tournament, Robo Dojo and RoboCom produced different
victors only four times.

~rdrc-battles-details
## Tournament results details

Here's [how to read the results](##results-table). The brackets are little
[wonky](##brackets), but that's OK.

### Round 1

    Match                               Robo Dojo   RoboCom  Footnotes
    --------------------------------------------------------------------
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

### Round 2

    Match                                 Robo Dojo   RoboCom  Footnotes
    ----------------------------------------------------------------------
    Alien3.4 vs. Alpha                    7-0-1       6-0-1     *
    Conciler vs. Delusion 3.7             0-7-0       0-7-0
    Flooder 1.0 vs. Goody2                1-2-4      1-0-13     * + -
    Goodymorph vs. HotBot V2              0-2-5       0-2-5
    LastHopeC vs. Lauf und Mine           7-0-0       7-0-0
    The Masterkiller 1 vs. TheMob.NextG2  5-0-2       6-0-1
    The Overkiller vs. Rainman            7-0-0       7-0-0
    Seuche3 vs. Sweeper                   2-0-5       1-0-6
    Test 3-1 vs. Tom Himself              7-0-0       7-0-0
    Zwei Arten (bye)                      .....       .....

### Round 3

    Match                                 Robo Dojo   RoboCom  Footnotes
    ----------------------------------------------------------------------
    Alien3.4 (bye)                        .....       .....
    Delusion 3.7 (bye)                    .....       .....
    Goody2 (bye)                          .....       .....
    HotBot V2 (bye)                       .....       .....
    LastHopeC (bye)                       .....       .....
    The Masterkiller (bye)                .....       .....
    The Overkiller vs. Seuche3            1-7-1      12-0-2    * + -
    Test 3-1 vs. Zwei Arten               3-4-0       4-3-0        -

### Round 4

    Match                                 Robo Dojo   RoboCom  Footnotes
    ----------------------------------------------------------------------
    Alien3.4 vs. Delusion 3.7            11-3-0       2-4-1    * + -
    Goody2 vs. HotBot V2                  0-7-0       0-7-0
    LastHopeC vs. The Masterkiller        0-1-7       0-2-5    *
    The Overkiller vs. Test 3-1           7-0-0       7-0-0

### Round 5

    Match                                 Robo Dojo   RoboCom  Footnotes
    ----------------------------------------------------------------------
    Alien 3.4 vs. HotBot V2               0-7-0       0-7-0                     
    The Masterkiller vs. The Overkiller   6-0-1       3-0-4

### Round 6

    Match                                 Robo Dojo   RoboCom  Footnotes
    ----------------------------------------------------------------------
    HotBot V2 vs. The Masterkiller        0-4-3      0-14-0    * +

### Champion

    The Masterkiller  

~brackets
## Note on brackets

I forgot about setting up byes until Round 2. In contrast to typical tournament
brackets, I added the byes in Round 2 and 3. For our purposes,
it doesn't really matter.

~results-table
## Reading the results

For each program pairing, we ran a match under Robo Dojo and a match under
RoboCom. A match consists of seven or more bouts -- where a bout is a
single program execution that ends once one color has been eliminated from the 
board, or the bout times out at 80,000 steps.

The results of a match are denoted <tt>A-B-C</tt>, where:

- <tt>A</tt> is the number of times the *first* program won a bout
- <tt>B</tt> is the number of times the *second* program won a bout
- <tt>C</tt> the number of times the programs tied


### Footnotes

<tt>\*</tt>, <tt>\+</tt>, and <tt>\-</tt> denote "unusual" matches.

<tt>\*</tt> denotes an [overtime match](##overtime-match).

<tt>\+</tt> denotes an overtime match where overtime times-out after 14 bouts.

<tt>\-</tt> denotes a match where the [winners are different](##different-winners)
in Robo Com and Robo Dojo.

~different-winners
## Winners are different
In this situation, we flip a coin to decide which program moves ahead in the
tournament.

~overtime-match
## Overtime Matches
Say the Robo Dojo result for a match is 0-2-5,
but the RoboCom result is 0-0-7. We then wonder: "is it even possible for
the second player to win in this match in Robo Com?"" In such situations, we
go into overtime, which means retrying the match in RoboCom until either:
(a) the second player scores a win (which definitely answers out question), or
(b) the second player never scores a win and we reach a total of 14 matches.

~july-package
## July Package

The [July Package](http://robocom.rrobek.de//download/julypack.zip)
contains a corpus of 50 RoboCom Classic programs, released in 1998.

We selected a subset of these programs for out tournament, based on the following
criteria:

Several programs appeared under different version numbers. For example,
the package includes
both <tt>Alien1.4</tt> and <tt>Alien3.4</tt>/. For such program families,
we only include the latest program (excluding the earlier programs in the family.
)

Lastly, we excluded <tt>Activator5</tt> because it fails to compile under 
Robo Dojo.

**TODO**: Explain failed compilation

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