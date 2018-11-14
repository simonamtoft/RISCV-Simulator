lui s0 13
addi s0 s0 -467
sh s0 100(x0)
lb t0 100(x0)
lb t1 101(x0)
lui s2 2
addi s2 s2 2040
addi s1 x0 50
sh s2 -23(s1)
lh t1 -23(s1)
addi a0 x0 10
ecall
