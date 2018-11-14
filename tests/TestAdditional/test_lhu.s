lui s0 3
addi s0 s0 1014
sh s0 100(x0)
lhu t0 100(x0)
lui s1 526344
addi s1 s1 128
addi s2 x0 39
sh s1 24(s2)
lhu t1 24(s2)
lui s3 324853
addi s3 s3 -177
addi s4 x0 103
sh s3 291(s4)
lhu t2 291(s4)
addi a0 x0 10
ecall
