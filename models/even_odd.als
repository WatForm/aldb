sig State {
    i: Int
}

pred init[s: State] {
    s.i = 0 || s.i = 1
}

pred next[s, s': State] {
    s'.i = plus[s.i, 2]
}
