/*  BEGIN_ALDB_CONF
 *
 *  # Additional Alloy sig scopes to specify.
 *  additionalSigScopes:
 *    Int: 6
 *
 *  END_ALDB_CONF
 */

sig State {
    i: Int
}

pred init[s: State] {
    s.i = 0 || s.i = 1
}

pred next[s, s': State] {
    s'.i = plus[s.i, 2]
}
