lui s0 239437
addi s0 s0 -467
sw s0 100(x0)
lb t0 100(x0)
lb t1 101(x0)
lb t2 102(x0)
lb t3 103(x0)
lh t4 100(x0)
lh t5 102(x0)
lh t6 101(x0)
lui s2 2
addi s2 s2 2040
addi s1 x0 50
sw s2 -23(s1)
lw s3 -23(s1)
addi a0 x0 10
ecall
