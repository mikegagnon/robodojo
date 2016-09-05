
* [Play Robo Dojo](http://robodojo.club)
* [Introduction](##intro)
* [Guide](##guide)
* [Reference](##ref)
* [Errata](##errata)
* [How to navigate this document](##navigate)

~navigate
## How to navigate this document

This is a [Sidenote](http://sidenote.io) document.

Sidenote opens links in new columns.

TODO

~intro
## Introduction

Robo Dojo is a clone of the 1998 game [RoboCom](http://robocom.rrobek.de/).
You play the game by programming bots to battle each other with home-brewed
viruses.

Here's a [screenshot](##4way) of a four-way battle.

Throughout the rest of this document, I assume you are a savvy programmer.

~4way
## Screenshot of a four-way battle

<img src="img/battle.png">

- Each tear-drop shape is a different bot
- If a bot contains a circle of a different color, that means the bot
  is infected
- If a bot has a white line through it, that means the bot is deactivated


~guide
## Guide

1. Hello World
2. Replication
3. 

~errata
## Errata

- [Compatibility with RoboCom](##compatibility)

~compatibility
## Compatibility with RoboCom

Robo Dojo is a reimplementation of the 1998 Windows game
[RoboCom](http://robocom.rrobek.de/). Here's a [screenshot](##robocomscreenshot).

I have strove to make the Robo Dojo simulation semantics identical to RoboCom --
however I could not create a cycle-for-cycle duplicate of RoboCom.

The problem is [threefold](##compat-threefold).

Here is one example of [mysterious RoboCom semantics](##compat-mystery).

~compat-threefold
## The problem is threefold

1. RoboCom is closed source
2. The RoboCom documentation is limited
3. Some of RoboCom's semantics are not documented

~compat-mystery
## Mysterious RoboCom semantics

I loaded up [two robots](##compat-mystery-2robots) from the [July Package](http://robocom.rrobek.de/?area=d_bots),
and placed them in the [same starting position](##compat-mystery-start) in both
Robo Dojo and RoboCom.

~compat-mystery-2robots
## Two robots

Activator3 and Alien 1.4

~compat-mystery-start
## Same starting position

<img src="img/compat-start-robodojo.png">

<img src="img/compat-start-robocom.png">





~robocomscreenshot
## RoboCom Screenshot

<img src="img/robocom-screenshot.png">