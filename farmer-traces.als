/*
   Automatically created via translation of a Dash model to Alloy
   on 2024-07-02 13:36:39
*/

open util/boolean
open util/traces[DshSnapshot] as DshSnapshot

/*******************************************************************************
 * Title: farmer.dsh
 * Authors: Jose Serna
 * Created: 2018-06-11
 * Last modified: 
 * 2023-06-07 Nancy Day slight syntax changes for newdash
 *
 * Notes: Adaptation to DASH from the original model available in the Alloy's
 *        libraries
 *
 ******************************************************************************/

abstract sig Object {
    eats: set Object
}
one sig Chicken, Farmer, Fox, Grain extends Object {}

fact eating {
    eats = Fox -> Chicken + Chicken -> Grain
}


abstract sig Transitions {}
one sig Puzzle_near2far extends Transitions {} 
one sig Puzzle_far2near extends Transitions {} 

sig DshSnapshot {
  dsh_taken0: set Transitions,
  Puzzle_near: set Object,
  Puzzle_far: set Object
}

pred dsh_initial [
	s: one DshSnapshot] {
  ((s.dsh_taken0) = none) &&
  ((s.Puzzle_near) = Object) &&
  no
  s.Puzzle_far
}

pred Puzzle_near2far_pre [
	s: one DshSnapshot] {
  Farmer in s.Puzzle_near
}


pred Puzzle_near2far_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  { ((one x: (s.Puzzle_near) - Farmer | ((sn.Puzzle_near) =
                                         ((((s.Puzzle_near)
                                              - Farmer) - x)
                                            -
                                            ((sn.Puzzle_near).eats))) &&
                                        ((sn.Puzzle_far) =
                                           (((s.Puzzle_far)
                                               + Farmer) + x)))) ||
    ((sn.Puzzle_near) =
       (((s.Puzzle_near) - Farmer) - ((sn.Puzzle_near).eats))) &&
      ((sn.Puzzle_far) = ((s.Puzzle_far) + Farmer)) }
  (sn.dsh_taken0) = Puzzle_near2far
}

pred Puzzle_near2far [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Puzzle_near2far_pre
  sn.(s.Puzzle_near2far_post)
}

pred Puzzle_far2near_pre [
	s: one DshSnapshot] {
  Farmer in s.Puzzle_far
}


pred Puzzle_far2near_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  { ((one x: (s.Puzzle_far) - Farmer | ((sn.Puzzle_far) =
                                        ((((s.Puzzle_far) -
                                             Farmer) - x) -
                                           ((sn.Puzzle_far).eats))) &&
                                       ((sn.Puzzle_near) =
                                          (((s.Puzzle_near)
                                              + Farmer) + x)))) ||
    ((sn.Puzzle_far) =
       (((s.Puzzle_far) - Farmer) - ((sn.Puzzle_far).eats))) &&
      ((sn.Puzzle_near) = ((s.Puzzle_near) + Farmer)) }
  (sn.dsh_taken0) = Puzzle_far2near
}

pred Puzzle_far2near [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Puzzle_far2near_pre
  sn.(s.Puzzle_far2near_post)
}

pred dsh_small_step [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  { (sn.(s.Puzzle_near2far)) ||
    (sn.(s.Puzzle_far2near)) ||
    (!({ (s.Puzzle_near2far_pre) || (s.Puzzle_far2near_pre) }) &&
       (sn.(s.dsh_stutter))) }
}

pred dsh_stutter [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_taken0) = none
  (sn.Puzzle_near) = (s.Puzzle_near)
  (sn.Puzzle_far) = (s.Puzzle_far)
}

fact dsh_traces_fact {
  DshSnapshot/first.dsh_initial
  {some
  DshSnapshot/back=>
    ((all s: DshSnapshot | (s.DshSnapshot/next).(s.dsh_small_step)))
  else
    ((all s: one
    (DshSnapshot - DshSnapshot/last) | (s.DshSnapshot/next).(s.dsh_small_step)))}

}

fact allSnapshotsDifferent {
  (all s: one
  DshSnapshot,sn: one
  DshSnapshot | (((s.dsh_taken0) = (sn.dsh_taken0)) &&
                   ((s.Puzzle_near) = (sn.Puzzle_near)) &&
                   ((s.Puzzle_far) = (sn.Puzzle_far))) =>
                  (s = sn))
}

pred dsh_strong_no_stutter {
  (all s: DshSnapshot | { (s = DshSnapshot/first) ||
                          !((s.dsh_taken0) = none) })
}

pred dsh_enough_operations {
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Puzzle_near2far))
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Puzzle_far2near))
}



