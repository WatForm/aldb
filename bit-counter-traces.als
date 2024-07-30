/*
   Automatically created via translation of a Dash model to Alloy
   on 2024-06-30 14:10:22
*/

open util/boolean
open util/traces[DshSnapshot] as DshSnapshot

/*******************************************************************************
 * Title: bit-counter.dsh
 * Authors: Jose Serna
 * Created: 14-04-2018
 * Last modified: 07-06-2023 Nancy Day
 *
 * Notes: Two bit counter model taken from Shahram's PhD thesis
 *
 *        Shahram Esmaeilsabzali. Perscriptive Semantics for Big-Step Modelling Languages.
 *        PhD thesis, University of Waterloo, David R. Cheriton School of Computer Science, 2011
 *        https://cs.uwaterloo.ca/~nday/pdf/theses/2011-esmaeilsabzali-phd-thesis.pdf
 *
 ******************************************************************************/

abstract sig DshStates {}
abstract sig Counter extends DshStates {} 
abstract sig DshScopes {}
one sig CounterScope extends DshScopes {} 
one sig Counter_Bit1Scope extends DshScopes {} 
abstract sig Counter_Bit1 extends Counter {} 
one sig Counter_Bit1_Bit11 extends Counter_Bit1 {} 
one sig Counter_Bit1_Bit12 extends Counter_Bit1 {} 
one sig Counter_Bit2Scope extends DshScopes {} 
abstract sig Counter_Bit2 extends Counter {} 
one sig Counter_Bit2_Bit21 extends Counter_Bit2 {} 
one sig Counter_Bit2_Bit22 extends Counter_Bit2 {} 

abstract sig Transitions {}
one sig Counter_Bit2_T4 extends Transitions {} 
one sig Counter_Bit1_T2 extends Transitions {} 
one sig Counter_Bit2_T3 extends Transitions {} 
one sig Counter_Bit1_T1 extends Transitions {} 

abstract sig DshEvents {}
abstract sig DshIntEvents extends DshEvents {} 
one sig Counter_Bit1_Tk1 extends DshIntEvents {} 
one sig Counter_Bit2_Done extends DshIntEvents {} 
abstract sig DshEnvEvents extends DshEvents {} 
one sig Counter_Tk0 extends DshEnvEvents {} 

sig DshSnapshot {
  dsh_sc_used0: set DshScopes,
  dsh_conf0: set DshStates,
  dsh_taken0: set Transitions,
  dsh_events0: set DshEvents,
  dsh_stable: one boolean/Bool
}

pred dsh_initial [
	s: one DshSnapshot] {
  ((s.dsh_conf0) = (Counter_Bit1_Bit11 + Counter_Bit2_Bit21)) &&
  ((s.dsh_sc_used0) = none) &&
  ((s.dsh_taken0) = none) &&
  (((s.dsh_events0) :> DshIntEvents) = none)
  (s.dsh_stable).boolean/isTrue
}

pred Counter_Bit2_T4_pre [
	s: one DshSnapshot] {
  some (Counter_Bit2_Bit22 & (s.dsh_conf0))
  !(CounterScope in (s.dsh_sc_used0))
  !(Counter_Bit2Scope in (s.dsh_sc_used0))
  !((s.dsh_stable).boolean/isTrue) &&
  (Counter_Bit1_Tk1 in (s.dsh_events0))
}


pred Counter_Bit2_T4_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_conf0) =
  (((s.dsh_conf0) -
      (Counter_Bit2_Bit21 + Counter_Bit2_Bit22)) +
     Counter_Bit2_Bit21)
  (sn.dsh_taken0) = Counter_Bit2_T4
  {(Counter_Bit2_Done.(Counter_Bit2.(sn.(s._nextIsStable))))=>
    (((sn.dsh_stable).boolean/isTrue) &&
       ((sn.dsh_sc_used0) = none) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            (((sn.dsh_events0) :> DshIntEvents) =
               Counter_Bit2_Done)
          else
            (((sn.dsh_events0) :> DshIntEvents) =
               (Counter_Bit2_Done +
                  ((s.dsh_events0) :> DshIntEvents)))}
        ))
  else
    (((sn.dsh_stable).boolean/isFalse) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            ((((sn.dsh_events0) :> DshIntEvents) =
                Counter_Bit2_Done) &&
               (((sn.dsh_events0) :> DshEnvEvents) =
                  ((s.dsh_events0) :> DshEnvEvents)) &&
               ((sn.dsh_sc_used0) = none))
          else
            (((sn.dsh_events0) =
                ((s.dsh_events0) + Counter_Bit2_Done)) &&
               ((sn.dsh_sc_used0) =
                  ((s.dsh_sc_used0) + Counter_Bit2Scope)))}
        ))}

}

pred Counter_Bit2_T4_enabledAfterStep [
	s: one DshSnapshot,
	sn: one DshSnapshot,
	sc0: DshStates,
	genEvs0: DshEvents] {
  some (Counter_Bit2_Bit22 & (sn.dsh_conf0))
  !((s.dsh_stable).boolean/isTrue) &&
  (Counter_Bit1_Tk1 in ((s.dsh_events0) + genEvs0))
}

pred Counter_Bit2_T4 [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Counter_Bit2_T4_pre
  sn.(s.Counter_Bit2_T4_post)
}

pred Counter_Bit1_T2_pre [
	s: one DshSnapshot] {
  some (Counter_Bit1_Bit12 & (s.dsh_conf0))
  !(CounterScope in (s.dsh_sc_used0))
  !(Counter_Bit1Scope in (s.dsh_sc_used0))
  {((s.dsh_stable).boolean/isTrue)=>
    (Counter_Tk0 in ((s.dsh_events0) :> DshEnvEvents))
  else
    (Counter_Tk0 in (s.dsh_events0))}

}


pred Counter_Bit1_T2_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_conf0) =
  (((s.dsh_conf0) -
      (Counter_Bit1_Bit11 + Counter_Bit1_Bit12)) +
     Counter_Bit1_Bit11)
  (sn.dsh_taken0) = Counter_Bit1_T2
  {(Counter_Bit1_Tk1.(Counter_Bit1.(sn.(s._nextIsStable))))=>
    (((sn.dsh_stable).boolean/isTrue) &&
       ((sn.dsh_sc_used0) = none) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            (((sn.dsh_events0) :> DshIntEvents) =
               Counter_Bit1_Tk1)
          else
            (((sn.dsh_events0) :> DshIntEvents) =
               (Counter_Bit1_Tk1 +
                  ((s.dsh_events0) :> DshIntEvents)))}
        ))
  else
    (((sn.dsh_stable).boolean/isFalse) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            ((((sn.dsh_events0) :> DshIntEvents) =
                Counter_Bit1_Tk1) &&
               (((sn.dsh_events0) :> DshEnvEvents) =
                  ((s.dsh_events0) :> DshEnvEvents)) &&
               ((sn.dsh_sc_used0) = none))
          else
            (((sn.dsh_events0) =
                ((s.dsh_events0) + Counter_Bit1_Tk1)) &&
               ((sn.dsh_sc_used0) =
                  ((s.dsh_sc_used0) + Counter_Bit1Scope)))}
        ))}

}

pred Counter_Bit1_T2_enabledAfterStep [
	s: one DshSnapshot,
	sn: one DshSnapshot,
	sc0: DshStates,
	genEvs0: DshEvents] {
  some (Counter_Bit1_Bit12 & (sn.dsh_conf0))
  {((s.dsh_stable).boolean/isTrue)=>
    (!(Counter in sc0) &&
       !(Counter_Bit1 in sc0) &&
       (Counter_Tk0 in
          (((s.dsh_events0) :> DshEnvEvents) + genEvs0)))
  else
    (Counter_Tk0 in ((s.dsh_events0) + genEvs0))}

}

pred Counter_Bit1_T2 [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Counter_Bit1_T2_pre
  sn.(s.Counter_Bit1_T2_post)
}

pred Counter_Bit2_T3_pre [
	s: one DshSnapshot] {
  some (Counter_Bit2_Bit21 & (s.dsh_conf0))
  !(CounterScope in (s.dsh_sc_used0))
  !(Counter_Bit2Scope in (s.dsh_sc_used0))
  !((s.dsh_stable).boolean/isTrue) &&
  (Counter_Bit1_Tk1 in (s.dsh_events0))
}


pred Counter_Bit2_T3_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_conf0) =
  (((s.dsh_conf0) -
      (Counter_Bit2_Bit21 + Counter_Bit2_Bit22)) +
     Counter_Bit2_Bit22)
  (sn.dsh_taken0) = Counter_Bit2_T3
  {(none.(Counter_Bit2.(sn.(s._nextIsStable))))=>
    (((sn.dsh_stable).boolean/isTrue) &&
       ((sn.dsh_sc_used0) = none) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            (((sn.dsh_events0) :> DshIntEvents) = none)
          else
            (((sn.dsh_events0) :> DshIntEvents) =
               ((s.dsh_events0) :> DshIntEvents))}
        ))
  else
    (((sn.dsh_stable).boolean/isFalse) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            ((((sn.dsh_events0) :> DshIntEvents) = none) &&
               (((sn.dsh_events0) :> DshEnvEvents) =
                  ((s.dsh_events0) :> DshEnvEvents)) &&
               ((sn.dsh_sc_used0) = none))
          else
            ((sn.dsh_sc_used0) =
               ((s.dsh_sc_used0) + Counter_Bit2Scope))}
        ))}

}

pred Counter_Bit2_T3_enabledAfterStep [
	s: one DshSnapshot,
	sn: one DshSnapshot,
	sc0: DshStates,
	genEvs0: DshEvents] {
  some (Counter_Bit2_Bit21 & (sn.dsh_conf0))
  !((s.dsh_stable).boolean/isTrue) &&
  (Counter_Bit1_Tk1 in ((s.dsh_events0) + genEvs0))
}

pred Counter_Bit2_T3 [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Counter_Bit2_T3_pre
  sn.(s.Counter_Bit2_T3_post)
}

pred Counter_Bit1_T1_pre [
	s: one DshSnapshot] {
  some (Counter_Bit1_Bit11 & (s.dsh_conf0))
  !(CounterScope in (s.dsh_sc_used0))
  !(Counter_Bit1Scope in (s.dsh_sc_used0))
  {((s.dsh_stable).boolean/isTrue)=>
    (Counter_Tk0 in ((s.dsh_events0) :> DshEnvEvents))
  else
    (Counter_Tk0 in (s.dsh_events0))}

}


pred Counter_Bit1_T1_post [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_conf0) =
  (((s.dsh_conf0) -
      (Counter_Bit1_Bit11 + Counter_Bit1_Bit12)) +
     Counter_Bit1_Bit12)
  (sn.dsh_taken0) = Counter_Bit1_T1
  {(none.(Counter_Bit1.(sn.(s._nextIsStable))))=>
    (((sn.dsh_stable).boolean/isTrue) &&
       ((sn.dsh_sc_used0) = none) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            (((sn.dsh_events0) :> DshIntEvents) = none)
          else
            (((sn.dsh_events0) :> DshIntEvents) =
               ((s.dsh_events0) :> DshIntEvents))}
        ))
  else
    (((sn.dsh_stable).boolean/isFalse) &&
       ({((s.dsh_stable).boolean/isTrue)=>
            ((((sn.dsh_events0) :> DshIntEvents) = none) &&
               (((sn.dsh_events0) :> DshEnvEvents) =
                  ((s.dsh_events0) :> DshEnvEvents)) &&
               ((sn.dsh_sc_used0) = none))
          else
            ((sn.dsh_sc_used0) =
               ((s.dsh_sc_used0) + Counter_Bit1Scope))}
        ))}

}

pred Counter_Bit1_T1_enabledAfterStep [
	s: one DshSnapshot,
	sn: one DshSnapshot,
	sc0: DshStates,
	genEvs0: DshEvents] {
  some (Counter_Bit1_Bit11 & (sn.dsh_conf0))
  {((s.dsh_stable).boolean/isTrue)=>
    (!(Counter in sc0) &&
       !(Counter_Bit1 in sc0) &&
       (Counter_Tk0 in
          (((s.dsh_events0) :> DshEnvEvents) + genEvs0)))
  else
    (Counter_Tk0 in ((s.dsh_events0) + genEvs0))}

}

pred Counter_Bit1_T1 [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  s.Counter_Bit1_T1_pre
  sn.(s.Counter_Bit1_T1_post)
}

pred _nextIsStable [
	s: one DshSnapshot,
	sn: one DshSnapshot,
	sc0: DshStates,
	genEvs0: DshEvents] {
  !(genEvs0.(sc0.(sn.(s.Counter_Bit2_T4_enabledAfterStep))))
  !(genEvs0.(sc0.(sn.(s.Counter_Bit1_T2_enabledAfterStep))))
  !(genEvs0.(sc0.(sn.(s.Counter_Bit2_T3_enabledAfterStep))))
  !(genEvs0.(sc0.(sn.(s.Counter_Bit1_T1_enabledAfterStep))))
}

pred dsh_small_step [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  { (sn.(s.Counter_Bit2_T4)) ||
    (sn.(s.Counter_Bit1_T2)) ||
    (sn.(s.Counter_Bit2_T3)) ||
    (sn.(s.Counter_Bit1_T1)) ||
    (!({ (s.Counter_Bit2_T4_pre) ||
           (s.Counter_Bit1_T2_pre) ||
           (s.Counter_Bit2_T3_pre) ||
           (s.Counter_Bit1_T1_pre) }) &&
       (sn.(s.dsh_stutter))) }
}

pred dsh_stutter [
	s: one DshSnapshot,
	sn: one DshSnapshot] {
  (sn.dsh_stable) = (s.dsh_stable)
  (sn.dsh_conf0) = (s.dsh_conf0)
  (sn.dsh_sc_used0) = (s.dsh_sc_used0)
  (sn.dsh_taken0) = none
  ((sn.dsh_events0) :> DshIntEvents) = none
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
  DshSnapshot | (((s.dsh_conf0) = (sn.dsh_conf0)) &&
                   ((s.dsh_sc_used0) = (sn.dsh_sc_used0)) &&
                   ((s.dsh_taken0) = (sn.dsh_taken0)) &&
                   ((s.dsh_events0) = (sn.dsh_events0)) &&
                   ((s.dsh_stable) = (sn.dsh_stable))) =>
                  (s = sn))
}

pred dsh_strong_no_stutter {
  (all s: DshSnapshot | { (s = DshSnapshot/first) ||
                          !((s.dsh_taken0) = none) })
}

pred dsh_enough_operations {
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Counter_Bit2_T4))
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Counter_Bit1_T2))
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Counter_Bit2_T3))
  (some s: one
  DshSnapshot,sn: one
  DshSnapshot | sn.(s.Counter_Bit1_T1))
}

pred dsh_single_event {
  (all s: one
  DshSnapshot | lone
  ((s.dsh_events0) :> DshEnvEvents))
}




