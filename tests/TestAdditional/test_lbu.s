addi s0 x0 102
sb s0 100(x0)
lbu t0 100(x0)
lui s1 526344
addi s1 s1 128
addi s2 x0 39
sb s1 24(s2)
lbu t1 24(s2)
lui s3 324853
addi s3 s3 -177
addi s4 x0 103
sb s3 291(s4)
lbu t2 291(s4)
addi a0 x0 10
ecall
