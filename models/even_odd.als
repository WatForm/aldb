/*  BEGIN_ALDB_CONF
 *
 *  # Additional Alloy sig scopes to specify.
 *  additionalSigScopes:
 *    Int: 6
 *    seq: 6
 *
 *  END_ALDB_CONF
 */

sig State {
    i: Int
}

pred init[s: State] {
    s.i = 0 || s.i = 1
}

pred next[s, sprime: State] {
    sprime.i = plus[s.i, 2]
}
