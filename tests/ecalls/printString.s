addi x10, x0, 4
la x11, string
ecall 

addi x10, x0, 10
ecall

string:
.string "Hej"
