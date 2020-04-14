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
pred crossRiver [from, from', to, to': set Object] {
  one x: from | {
    from' = from - x - Farmer - from'.eats
    to' = to + x + Farmer
  }
}

pred next[s, s':  State] {
  Farmer in s.near =>
    crossRiver [s.near, s'.near, s.far, s'.far]
  else
    crossRiver [s.far, s'.far, s.near, s'.near]
}
