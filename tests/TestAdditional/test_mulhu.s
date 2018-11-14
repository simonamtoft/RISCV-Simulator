addi a0 x0 12
addi a1 x0 27
mulhu s0 a0 a1
mulhu s1 a1 a0
addi a0 x0 -31
addi a1 x0 756
mulhu s2 a0 a1
mulhu s3 a1 a0
addi a0 x0 -31
addi a1 x0 -756
mulhu s4 a0 a1
mulhu s5 a1 a0
lui a0 1048568
addi a0 a0 437
lui a1 1048402
addi a1 a1 348
mulhu s6 a0 a1
mulhu s7 a1 a0
lui a0 5213
addi a0 a0 -1127
lui a1 1026026
addi a1 a1 557
mulhu s8 a0 a1
mulhu s9 a1 a0
addi a0 x0 10
ecall
