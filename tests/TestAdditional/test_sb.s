addi s0 x0 102
sb s0 100(x0)
lb t0 100(x0)
addi s2 x0 102
addi s1 x0 60
sb s2 -23(s1)
lb t1 -23(s1)
addi a0 x0 10
ecall
