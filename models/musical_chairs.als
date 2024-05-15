/* Authors: Sabria Farheen, Nancy A. Day, Amirhossein Vakili, Ali Abbassi
 * Date: October 1, 2017
 */

/*  BEGIN_ALDB_CONF
 *
 *  # Name of the transition relation in the Alloy model.
 *  transitionRelationName: trans
 *
 *  # Additional Alloy sig scopes to specify.
 *  additionalSigScopes:
 *    Chair: 3
 *    Player: 4
 *
 *  END_ALDB_CONF
 */

open util/integer

//***********************STATE SPACE*************************//
sig Chair, Player {}
abstract sig Mode {}
one sig start, walking, sitting, end extends Mode {}

sig State {
  // current players
  players: set Player,
  //current chairs
  chairs: set Chair,
  // current chair player relation
  occupied: set Chair -> set Player,
  // current state of game, should always be 1
  mode : set Mode
}

//*****************INITIAL STATE CONSTRAINTS********************//

pred init [s:State] {
    s.mode = start
    #s.players > 1
    #s.players = (#s.chairs).plus[1]
    // force all Chair and Player to be included
    s.players = Player
    s.chairs = Chair
    s.occupied = none -> none
}

//**********************TRANSITION CONSTRAINTS***********************//
pred pre_music_starts [s: State] {
  #s.players > 1
  s.mode = start
}
pred post_music_starts [s, sprime: State] {
  sprime.players = s.players
  sprime.chairs = s.chairs
  // no one is sitting after music starts
  sprime.occupied = none -> none
  sprime.mode= walking
}
pred music_starts [s, sprime: State] {
  pre_music_starts[s]
  post_music_starts[s,sprime]
}

pred pre_music_stops [s: State] {
  s.mode = walking
}
pred post_music_stops [s, sprime: State] {
  sprime.players = s.players
  sprime.chairs = s.chairs
  // no other chair/player than chairs/players
  sprime.occupied in sprime.chairs -> sprime.players
  // forcing occupied to be total and
  //each chair mapped to only one player
  all c:sprime.chairs | one c.(sprime.occupied)
  // each "occupying" player is sitting on one chair
  all p:Chair.(sprime.occupied) | one sprime.occupied.p
  sprime.mode = sitting
}
pred music_stops [s, sprime: State] {
  pre_music_stops[s]
  post_music_stops[s,sprime]
}

pred pre_eliminate_loser [s: State] {
  s.mode = sitting
}
pred post_eliminate_loser [s, sprime: State] {
  // loser is the player in the game not in the range of occupied
  sprime.players = Chair.(s.occupied)
  #sprime.chairs = (#s.chairs).minus[1]
  sprime.mode = start
}
pred eliminate_loser [s, sprime: State] {
  pre_eliminate_loser[s]
  post_eliminate_loser[s,sprime]
}

pred pre_declare_winner [s: State] {
  #s.players = 1
  s.mode = start
}
pred post_declare_winner [s, sprime: State] {
  sprime.players = s.players
  sprime.chairs = s.chairs
  sprime.mode = end
}
pred declare_winner [s, sprime: State] {
  pre_declare_winner[s]
  post_declare_winner[s,sprime]
}

pred pre_end_loop [s: State] {
  s.mode = end
}
pred post_end_loop [s, sprime: State] {
  sprime.mode = end
  sprime.players = s.players
  sprime.chairs = s.chairs
  sprime.occupied = s.occupied
}
pred end_loop [s, sprime: State] {
  pre_end_loop[s]
  post_end_loop[s,sprime]
}

// helper to define valid transitions
pred trans [s,sprime: State] {
    music_starts[s,sprime] or
    music_stops[s,sprime] or
    eliminate_loser[s,sprime] or
    declare_winner[s,sprime] or
    end_loop[s,sprime]
}
