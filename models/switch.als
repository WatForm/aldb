abstract sig Switch {}

one sig On, Off extends Switch {}

sig State {
    a: Switch,
    b: Switch
}

pred init[s: State] {
    s.a = On
    s.b = Off
}

pred next[s, s': State] {
    s.a = On implies s'.a = Off
    s.a = Off implies s'.a = On
    s.b = On implies s'.b = Off
    s.b = Off implies s'.b = On
}
