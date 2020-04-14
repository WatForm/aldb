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
pred post_music_starts [s, s': State] {
  s'.players = s.players
  s'.chairs = s.chairs
  // no one is sitting after music starts
  s'.occupied = none -> none
  s'.mode= walking
}
pred music_starts [s, s': State] {
  pre_music_starts[s]
  post_music_starts[s,s']
}

pred pre_music_stops [s: State] {
  s.mode = walking
}
pred post_music_stops [s, s': State] {
  s'.players = s.players
  s'.chairs = s.chairs
  // no other chair/player than chairs/players
  s'.occupied in s'.chairs -> s'.players
  // forcing occupied to be total and
  //each chair mapped to only one player
  all c:s'.chairs | one c.(s'.occupied)
  // each "occupying" player is sitting on one chair
  all p:Chair.(s'.occupied) | one s'.occupied.p
  s'.mode = sitting
}
pred music_stops [s, s': State] {
  pre_music_stops[s]
  post_music_stops[s,s']
}

pred pre_eliminate_loser [s: State] {
  s.mode = sitting
}
pred post_eliminate_loser [s, s': State] {
  // loser is the player in the game not in the range of occupied
  s'.players = Chair.(s.occupied)
  #s'.chairs = (#s.chairs).minus[1]
  s'.mode = start
}
pred eliminate_loser [s, s': State] {
  pre_eliminate_loser[s]
  post_eliminate_loser[s,s']
}

pred pre_declare_winner [s: State] {
  #s.players = 1
  s.mode = start
}
pred post_declare_winner [s, s': State] {
  s'.players = s.players
  s'.chairs = s.chairs
  s'.mode = end
}
pred declare_winner [s, s': State] {
  pre_declare_winner[s]
  post_declare_winner[s,s']
}

pred pre_end_loop [s: State] {
  s.mode = end
}
pred post_end_loop [s, s': State] {
  s'.mode = end
  s'.players = s.players
  s'.chairs = s.chairs
  s'.occupied = s.occupied
}
pred end_loop [s, s': State] {
  pre_end_loop[s]
  post_end_loop[s,s']
}

// helper to define valid transitions
pred trans [s,s': State] {
    music_starts[s,s'] or
    music_stops[s,s'] or
    eliminate_loser[s,s'] or
    declare_winner[s,s'] or
    end_loop[s,s']
}
