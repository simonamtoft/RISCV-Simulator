/* File: CPU.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file handles Instructions
 */

package RISCVSimulator;


public class CPU {
    int pc = 0;                     // Program counter
    int prevPc;                     // Previous pc
    int[] reg = new int[32];        // RISC-V registers x0 to x31
    private Instruction[] program;  // Array of all program instructions
    private Memory memory;          // Memory byte array

    /**
     * Initializes a CPU with memory 'mem' and the instruction array 'program'
     * and sp (x2) to point at last memory address.
     */
    public CPU(Memory mem, Instruction[] program) {
        this.memory = mem;
        this.program = program;
        reg[2] = memory.getMemory().length - 1;
    }

    /**
     * Executes one instruction given by the instruction array program at index given by the program counter
     * Uses the opcode field of the instruction to determine which type of instruction it is and call that method.
     */
    public void executeInstruction(){
        prevPc = pc;
        Instruction instr = program[pc];
        switch(instr.opcode){
            // R-type instructions
            case 0b0110011: // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
                rType(instr);
                break;

            // J-type instruction
            case 0b1101111: //JAL
                reg[inst.rd] = (pc+1)<<2; // Store address of next instruction in bytes
                pc += inst.immJ>>2;
                break;
                
            // I-type instructions
            case 0b1100111: // JALR
                reg[inst.rd] = (pc+1)<<2;
                pc = ((reg[inst.rs1] + inst.immI) & 0xFFFFFFFE)>>2;
                break;
            case 0b0000011: // LB / LH / LW / LBU / LHU
                iTypeLoad(instr);
                break;
            case 0b0010011: // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
                iTypeInteger(instr);
                break;
            case 0b0001111: // FENCE / FENCE.I
                iTypeFence(instr);
                break;
            case 0b1110011: // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
                iTypeStatus(instr);
                break;

            //S-type instructions
            case 0b0100011: //SB / SH / SW
                sType(instr);
                break;

            //B-type instructions
            case 0b1100011: // BEQ / BNE / BLT / BGE / BLTU / BGEU
                bType(instr);
                break;

            //U-type instructions
            case 0b0110111: //LUI
            case 0b0010111: //AUIPC
                uType(instr);
                break;
        }
        reg[0] = 0; // x0 must always be 0
    }

    /**
     * Handles execution of r-Type instructions:
     * ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
     */
    private void rType(Instruction inst) {
        switch(inst.funct3){
            case 0b000: // ADD / SUB
                switch(inst.funct7){
                    case 0b0000000: // ADD
                        reg[inst.rd] = reg[inst.rs1] + reg[inst.rs2];
                        break;
                    case 0b0100000: // SUB
                        reg[inst.rd] = reg[inst.rs1] - reg[inst.rs2];
                        break;
                }
                break;
            case 0b001: // SLL
                reg[inst.rd] = reg[inst.rs1] << reg[inst.rs2];
                break;
            case 0b010: // SLT
                if (reg[inst.rs1] < reg[inst.rs2])
                    reg[inst.rd] = 1;
                else
                    reg[inst.rd] = 0;
                break;
            case 0b011: // SLTU
                if (Integer.toUnsignedLong(reg[inst.rs1]) < Integer.toUnsignedLong(reg[inst.rs2]))
                    reg[inst.rd] = 1;
                else
                    reg[inst.rd] = 0;
                break;
            case 0b100: // XOR
                reg[inst.rd] = reg[inst.rs1] ^ reg[inst.rs2];
                break;
            case 0b101: // SRL / SRA
                switch(inst.funct7){
                    case 0b0000000: // SRL
                        reg[inst.rd] = reg[inst.rs1] >>> reg[inst.rs2];
                        break;
                    case 0b0100000: // SRA
                        reg[inst.rd] = reg[inst.rs1] >> reg[inst.rs2];
                        break;
                }
                break;
            case 0b110: // OR
                reg[inst.rd] = reg[inst.rs1] | reg[inst.rs2];
                break;
            case 0b111: // AND
                reg[inst.rd] = reg[inst.rs1] & reg[inst.rs2];
                break;
        }
        pc++;
    }

    /**
     * Handles execution of i-Type load instructions:
     * LB / LH / LW / LBU / LHU
     */
    private void iTypeLoad(Instruction inst) {
        int addr = reg[inst.rs1] + inst.immI; // Byte address

        switch(inst.funct3){
            // This assumes properly aligned addresses in all scenarios. LH / LW wont work properly if misaligned.
            case 0b000: // LB
                reg[inst.rd] = memory.getByte(addr);
                break;
            case 0b001: // LH
                reg[inst.rd] = memory.getHalfWord(addr);
                break;
            case 0b010: // LW
                reg[inst.rd] = memory.getWord(addr);
                break;
            case 0b100: // LBU
                reg[inst.rd] = memory.getByte(addr) & 0xFF; //Remove sign bits
                break;
            case 0b101: // LHU
                reg[inst.rd] = memory.getHalfWord(addr) & 0xFFFF;
                break;
            default:
                break;
        }
        pc++;
    }

    /**
     * Handles execution of I-type integer instructions:
     * ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
     */
    private void iTypeInteger(Instruction inst) {
        switch(inst.funct3){
            case 0b000: // ADDI
                reg[inst.rd] = reg[inst.rs1] + inst.immI;
                break;
            case 0b010: // SLTI
                if(reg[inst.rs1] < inst.immI)
                    reg[inst.rd] = 1;
                else
                    reg[inst.rd] = 0;
                break;
            case 0b011: // SLTIU
                if(Integer.toUnsignedLong(reg[inst.rs1]) < Integer.toUnsignedLong(inst.immI))
                    reg[inst.rd] = 1;
                else
                    reg[inst.rd] = 0;
                break;
            case 0b100: // XORI
                reg[inst.rd] = reg[inst.rs1] ^ inst.immI;
                break;
            case 0b110: // ORI
                reg[inst.rd] = reg[inst.rs1] | inst.immI;
                break;
            case 0b111: // ANDI
                reg[inst.rd] = reg[inst.rs1] & inst.immI;
                break;
            case 0b001: // SLLI
                reg[inst.rd] = reg[inst.rs1] << inst.immI;
                break;
            case 0b101: // SRLI / SRAI
                int ShiftAmt = inst.immI & 0x1F;
                switch(inst.funct7){
                    case 0b0000000: // SRLI
                        reg[inst.rd] = reg[inst.rs1] >>> ShiftAmt;
                        break;
                    case 0b0100000: // SRAI
                        reg[inst.rd] = reg[inst.rs1] >> ShiftAmt;
                        break;
                }
                break;
        }
        pc++;
    }


    /**
     * Handles the I-type fence instructions:
     * FENCE / FENCE.I
     */
    private void iTypeFence(Instruction inst) {
        switch(inst.funct3){
            case 0b000: // FENCE
                break;
            case 0b001: // FENCE.I
                break;
        }
        pc++;
    }

    /**
     * Handles execution of i-Type ECALL and status instructions:
     * ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
     */
    private void iTypeStatus(Instruction inst) {
        switch(inst.funct3){
            case 0b000: // ECALL / EBREAK
                switch(inst.immI){
                    case 0b000000000000: // ECALL
                        switch (reg[10]) {
                            case 1:     // print_int
                                //System.out.print(reg[11]);
                                break;
                            case 4:     // print_string
                                //System.out.print(memory[reg[11]]);
                                break;
                            case 9:     // sbrk
                                // not sure if we can do this?
                                break;
                            case 10:    // exit
                                pc = program.length; // Sets program counter to end of program, to program loop
                                return;              // Exits 'iTypeStatus' function and returns to loop.
                            case 11:    // print_character
                                System.out.print((char) reg[11]);
                                break;
                            case 17:    // exit2
                                pc = program.length;
                                //System.out.println("Return code: " + reg[11]); // Prints a1 (should be return?)
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

    /**
     * Handles the S-type instructions:
     * SB / SH / SW
     */
    private void sType(Instruction inst) {
        int addr = reg[inst.rs1] + inst.immS;
        switch(inst.funct3){
            case 0b000: // SB
                memory.storeByte(addr,(byte) reg[inst.rs2]);
                break;
            case 0b001: // SH
                memory.storeHalfWord(addr, (short) reg[inst.rs2]);
                break;
            case 0b010: // SW
                memory.storeWord(addr, reg[inst.rs2]);
                break;
        }
        pc++;
    }

    /**
     * Handles the B-type instructions:
     * BEQ / BNE / BLT / BGE / BLTU / BGEU
     */
    private void bType(Instruction inst) {
        int ImmB = inst.immB >> 2; //We're counting in words instead of bytes
        switch(inst.funct3){
            case 0b000: // BEQ
                pc += (reg[inst.rs1] == reg[inst.rs2]) ? ImmB : 1;
                break;
            case 0b001: // BNE
                pc += (reg[inst.rs1] != reg[inst.rs2]) ? ImmB : 1;
                break;
            case 0b100: // BLT
                pc += (reg[inst.rs1] < reg[inst.rs2]) ? ImmB : 1;
                break;
            case 0b101: // BGE
                pc += (reg[inst.rs1] >= reg[inst.rs2]) ? ImmB : 1;
                break;
            case 0b110: //BLTU
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) < Integer.toUnsignedLong(reg[inst.rs2])) ? ImmB : 1;
                break;
            case 0b111: //BLGEU
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) >= Integer.toUnsignedLong(reg[inst.rs2])) ? ImmB : 1;
                break;
        }
    }

    /**
     * Handles the U-type instructions:
     * LUI / AUIPC
     */
    private void uType(Instruction inst){
        switch(inst.opcode){
            case 0b0010111: // AUIPC
                reg[inst.rd] = (pc << 2) + inst.immU; // As we count in 4 byte words
                break;
            case 0b0110111: // LUI
                reg[inst.rd] = inst.immU;
                break;
        }
        pc++;
    }
}
