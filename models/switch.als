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

pred next[s, sprime: State] {
    s.a = On implies sprime.a = Off
    s.a = Off implies sprime.a = On
    s.b = On implies sprime.b = Off
    s.b = Off implies sprime.b = On
}
