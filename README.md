# Compass Tracker
Compass tracker plugin for Spigot servers

The idea for this plugin comes from [Dream's manhunt videos](https://www.youtube.com/watch?v=3tH4dyOPZnY&feature=youtu.be), however this is written in mind for my own manhunt events.
The basic idea of this plugin, is that you have two groups of players in a Minecraft server: one is the trackers, and the other is the tracked, which I refer to as a target. There can be multiple trackers and multiple targets, and targets are also able to track other targets.

## Basic use:
If you are not given a compass automatically, use `/ctr give`.

To add players as a target, use `/ctr target [player name]`.

You should be able to right click while holding the compass to cycle through the available targets, but you can also use `/ctr track [player name]` to track someone.

This plugin also works in the nether and the end with lodestone compasses! Use `/ctr environment [on|off] [overworld|nether|end]` to allow or disallow tracking in that environment. Only the overworld is enabled by default.

## All commands:
`/ctr give` Give yourself a compass

`/ctr give [player name]` Give the specified player a compass

`/ctr target [target name]` Set the specified player as a target (Warning: You can add names not in the server.)

`/ctr removetarget [target name]` Stop the specified player from being a target

`/ctr targetlist` List the current targets

`/ctr track [target name]` Track the specified target with your compass

`/ctr track [target name] [player name]` Have someone else's compass track the specified target

`/ctr who` Print to chat your current target

`/ctr who [player name]` Print to chat the specified player's target

`/ctr environment [boolean]` Allow or disallow tracking in the current environment

`/ctr environment [boolean] [environment name]` Allow or disallow tracking in the specified environment

`/ctr environmentlist` List the trackable environments

`/ctr tickrate [update period in ticks]` Sets the update rate of the compasses. Default is 60.

`/ctr autogive [boolean]` Sets if we automatically give players compasses. Default is true.

`/ctr autotarget [boolean]` Sets if we automatically add players who join as a target. Default is false.

## Permissions
`ctrack.give.self` Can give a compass to themself (default: everyone)

`ctrack.give` Can give a compass to anyone (default: op)

`ctrack.target` Can manage targets (default: op)

`ctrack.autogive` Can manage auto giving and auto taking compasses (default: op)

`ctrack.autotarget` Can manage automatically setting people who join to targets (default: op)

`ctrack.environment` Can manage permitted environments (default: op)

`ctrack.tickrate` Can manage compass update rate (default: op)

## todo
- remember allowed environments
- remember targets
- clear targets
- clear all tracker compasses
- give everyone tracker compass
- on autotarget turn on, make everyone in server target
- add everyone as targets
- add intentional target location error
- target/tracker teams; ex. team a can track anyone from team b or c, team b can track anyone from team a, team c cannot track anyone, etc
- document code
- make functions intended to work on specific structures/objects part of those structures/objects (move from static global to static object)
- create a general player identifier structure for ease of grabbing player objects or names when you may have one or the other