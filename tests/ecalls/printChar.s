addi x10, x0, 11
lb x11, char
ecall 

addi x10, x0, 10
ecall

char:
.byte 'H'
