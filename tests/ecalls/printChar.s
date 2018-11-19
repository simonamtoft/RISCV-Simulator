addi x10, x0, 11
auipc x11, char
lb x11, 0(x11)
ecall 

char:
.byte 'H'
