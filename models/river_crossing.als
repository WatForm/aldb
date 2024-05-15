/* Adapted from the tutorial River Crossing Model: http://alloytools.org/tutorials/online/frame-RC-1.html */

/* Farmer and his possessions are objects. */
abstract sig Object { eats: set Object }
one sig Farmer, Fox, Chicken, Grain extends Object {}

/* Defines what eats what and the farmer is not around. */
fact { eats = Fox->Chicken + Chicken->Grain}

/* Stores the objects at near and far side of river. */
sig State { near, far: set Object }

/* In the initial state, all objects are on the near side. */
pred init[s:State] {
  s.near = Object && no s.far
}

/* At most one item to move from 'from' to 'to' */
pred crossRiver [from, fromprime, to, toprime: set Object] {
  one x: from | {
    fromprime = from - x - Farmer - fromprime.eats
    toprime = to + x + Farmer
  }
}

pred next[s, sprime:  State] {
  Farmer in s.near =>
    crossRiver [s.near, sprime.near, s.far, sprime.far]
  else
    crossRiver [s.far, sprime.far, s.near, sprime.near]
}
