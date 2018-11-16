/* File: CPU.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file handles Instructions
 */

package RISCVSimulator;


public class CPU {
    int pc = 0;                 // Program counter
    static int[] reg = new int[32];              // RISC-V registers x0 to x31
    public Instruction[] program;
    private Memory memory;

    public CPU(Memory mem, Instruction[] program) {
        this.memory = mem;
        this.program = program;
        reg[2] = memory.getArray().length - 1;
    }

    // Executes one instruction
    public void executeInstruction(){
        switch(program[pc].opcode){
            // R-type instructions
            case 0b0110011: // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
                rType(program[pc]);
                break;

            // J-type instruction
            case 0b1101111: //JAL
                jumpType(program[pc]);
                break;
            // I-type instructions
            case 0b1100111: // JALR
                jumpType(program[pc]);
                break;
            case 0b0000011: // LB / LH / LW / LBU / LHU
                iTypeLoad(program[pc], memory);
                break;
            case 0b0010011: // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
                iTypeInteger(program[pc]);
                break;
            case 0b0001111: // FENCE / FENCE.I
                iTypeFence(program[pc]);
                break;
            case 0b1110011: // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
                iTypeStatus(program);
                break;

            //S-type instructions
            case 0b0100011: //SB / SH / SW
                sType(program[pc], memory);
                break;

            //B-type instructions
            case 0b1100011: // BEQ / BNE / BLT / BGE / BLTU / BGEU
                bType(program[pc]);
                break;

            //U-type instructions
            case 0b0110111: //LUI
            case 0b0010111: //AUIPC
                uType(program[pc]);
                break;
        }
        reg[0] = 0; // x0 must always be 0
    }

    // JAL and JALR
    private void jumpType(Instruction inst){
        int imm = 0;
        switch(inst.opcode){
            case 0b1101111: //JAL
                imm = inst.immJ; // Used for printing
                reg[inst.rd] = (pc+1)*4; // Store address of next instruction in bytes
                pc += imm/4;
                break;
            case 0b1100111: //JALR
                imm = inst.immI; // Used for printing
                reg[inst.rd] = (pc+1)*4;
                pc = ((reg[inst.rs1] + imm) & 0xFFFFFFFE)/4;
                break;
        }
        pc++;
    }

    // R-type instructions: ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
    private void rType(Instruction inst) {
        // Used in all instructions, defined to reduce line length
        int rd = inst.rd;
        int rs1 = inst.rs1;
        int rs2 = inst.rs2;

        switch(inst.funct3){
            case 0b000: // ADD / SUB
                switch(inst.funct7){
                    case 0b0000000: // ADD
                        reg[rd] = reg[rs1] + reg[rs2];
                        break;
                    case 0b0100000: // SUB
                        reg[rd] = reg[rs1] - reg[rs2];
                        break;
                }
                break;
            case 0b001: // SLL
                reg[rd] = reg[rs1] << reg[rs2];
                break;
            case 0b010: // SLT
                if (reg[rs1] < reg[rs2])
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b011: // SLTU
                if (Integer.toUnsignedLong(reg[rs1]) < Integer.toUnsignedLong(reg[rs2]))
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b100: // XOR
                reg[rd] = reg[rs1] ^ reg[rs2];
                break;
            case 0b101: // SRL / SRA
                switch(inst.funct7){
                    case 0b0000000: // SRL
                        reg[rd] = reg[rs1] >>> reg[rs2];
                        break;
                    case 0b0100000: // SRA
                        reg[rd] = reg[rs1] >> reg[rs2];
                        break;
                }
                break;
            case 0b110: // OR
                reg[rd] = reg[rs1] | reg[rs2];
                break;
            case 0b111: // AND
                reg[rd] = reg[rs1] & reg[rs2];
                break;
        }
        pc++;
    }

    // I-type load instructions: LB / LH / LW / LBU / LHU
    private void iTypeLoad(Instruction inst, Memory mem) {
        int rd = inst.rd;
        int addr = reg[inst.rs1] + inst.immI; // Byte address

        switch(inst.funct3){
            // This assumes properly aligned addresses in all scenarios. LH / LW wont work properly if misaligned.
            case 0b000: // LB
                reg[rd] = mem.getByte(addr);
                break;
            case 0b001: // LH
                reg[rd] = mem.getHalfWord(addr);
                break;
            case 0b010: // LW
                reg[rd] = mem.getWord(addr);
                break;
            case 0b100: // LBU
                reg[rd] = mem.getByte(addr) & 0xFF; //Remove sign bits
                break;
            case 0b101: // LHU
                reg[rd] = mem.getHalfWord(addr) & 0xFFFF;
                break;
            default:
                break;
        }
        pc++;
    }

    // I-type integer instructions: ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
    private void iTypeInteger(Instruction inst) {
        int immI = inst.immI;
        int rs1 = inst.rs1;
        int rd = inst.rd;

        switch(inst.funct3){
            case 0b000: // ADDI
                reg[rd] = reg[rs1] + immI;
                break;
            case 0b010: // SLTI
                if(reg[rs1] < immI)
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b011: // SLTIU
                if(Integer.toUnsignedLong(reg[rs1]) < Integer.toUnsignedLong(immI))
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b100: // XORI
                reg[rd] = reg[rs1] ^ immI;
                break;
            case 0b110: // ORI
                reg[rd] = reg[rs1] | immI;
                break;
            case 0b111: // ANDI
                reg[rd] = reg[rs1] & immI;
                break;
            case 0b001: // SLLI
                reg[rd] = reg[rs1] << immI;
                break;
            case 0b101: // SRLI / SRAI
                int ShiftAmt = immI & 0x1F;
                switch(inst.funct7){
                    case 0b0000000: // SRLI
                        reg[rd] = reg[rs1] >>> ShiftAmt;
                        break;
                    case 0b0100000: // SRAI
                        reg[rd] = reg[rs1] >> ShiftAmt;
                        break;
                }
                break;
        }
        pc++;
    }

    // I-type status & call instructions: ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
    private void iTypeStatus(Instruction[] programInst) {
        switch(programInst[pc].funct3){
            case 0b000: // ECALL / EBREAK
                switch(programInst[pc].immI){
                    case 0b000000000000: // ECALL
                        System.out.println("ECALL "+ reg[10]);
                        switch (reg[10]) {
                            case 1:     // print_int
                                System.out.print(reg[11]);
                                break;
                            case 4:     // print_string
                                //System.out.print(memory[reg[11]]);
                                break;
                            case 9:     // sbrk
                                // not sure if we can do this?
                                break;
                            case 10:    // exit
                                pc = programInst.length; // Sets program counter to end of program, to program loop
                                return;              // Exits 'iTypeStatus' function and returns to loop.
                            case 11:    // print_character
                                System.out.print((char) reg[11]);
                                break;
                            case 17:    // exit2
                                pc = programInst.length;
                                System.out.println("Return code: " + reg[11]); // Prints a1 (should be return?)
                                return;
                            default:
                                System.out.println("ECALL " + reg[10] + " not implemented");
                                break;
                        }
                    case 0b000000000001: // EBREAK
                        break;
                }
                break;
            case 0b001: // CSRRW
                break;
            case 0b010: // CSRRS
                break;
            case 0b011: // CSRRC
                break;
            case 0b101: // CSRRWI
                break;
            case 0b110: // CSRRSI
                break;
            case 0b111: // CSRRCI
                break;
        }
        pc++;
    }

    // S-type instructions: SB / SH / SW
    private void sType(Instruction inst, Memory mem) {
        int addr = reg[inst.rs1] + inst.immS;
        switch(inst.funct3){
            case 0b000: // SB
                mem.storeByte(addr,(byte) reg[inst.rs2]);
                break;
            case 0b001: // SH
                mem.storeHalfWord(addr, (short) reg[inst.rs2]);
                break;
            case 0b010: // SW
                mem.storeWord(addr, reg[inst.rs2]);
                break;
        }
        pc++;
    }

    // B-type instructions: BEQ / BNE / BLT / BGE / BLTU / BGEU
    private void bType(Instruction inst) {
        int ImmB = inst.immB >> 2; //We're counting in words instead of bytes
        switch(inst.funct3){
            case 0b000: // BEQ
                pc += (reg[inst.rs1] == reg[inst.rs2])? ImmB : 1;
                break;
            case 0b001: // BNE
                pc += (reg[inst.rs1] != reg[inst.rs2])? ImmB : 1;
                break;
            case 0b100: // BLT
                pc += (reg[inst.rs1] < reg[inst.rs2])? ImmB : 1;
                break;
            case 0b101: // BGE
                pc += (reg[inst.rs1] >= reg[inst.rs2])? ImmB : 1;
                break;
            case 0b110: //BLTU
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) < Integer.toUnsignedLong(reg[inst.rs2]))? ImmB : 1;
                break;
            case 0b111: //BLGEU
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) >= Integer.toUnsignedLong(reg[inst.rs2]))? ImmB : 1;
                break;
        }
    }

    // U-type instructions: LUI / AUIPC
    private void uType(Instruction inst){
        switch(inst.opcode){
            case 0b0010111: // AUIPC
                reg[inst.rd] = pc*4 + inst.immU; // As we count in 4 byte words
                break;
            case 0b0110111: // LUI
                reg[inst.rd] = inst.immU;
                break;
        }
        pc++;
    }

    // I-type fence instructions: FENCE / FENCE.I
    private void iTypeFence(Instruction program) {
        switch(program.funct3){
            case 0b000: // FENCE
                break;
            case 0b001: // FENCE.I
                break;
        }
        pc++;
    }
}
