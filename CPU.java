/* File: CPU.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file handles Instructions
 */

package RISCVSimulator;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CPU {
    static int pc;                 // Program counter
    static int[] reg;              // RISC-V registers x0 to x31


    public CPU() {
        pc = 0;
        reg = new int[32];
    }

    // Adds instructions to program array
    public static void getInstructions(String path, Instruction[] programInst) throws IOException {
        File f = new File(path);
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        programInst = new Instruction[(int) f.length()/4];
        for(int i = 0; i < programInst.length; i++){
            programInst[i] = new Instruction(Integer.reverseBytes(dis.readInt()));
        }
        dis.close();
    }

    // Executes one instruction
    public static void executeInstruction(Instruction[] programInst, Memory mem){
        switch(programInst[pc].opcode){
            // R-type instructions
            case 0b0110011: // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
                rType(programInst[pc]);
                break;

            // J-type instruction
            case 0b1101111: //JAL
                jumpType(programInst[pc]);
                break;
            // I-type instructions
            case 0b1100111: // JALR
                jumpType(programInst[pc]);
                break;
            case 0b0000011: // LB / LH / LW / LBU / LHU
                iTypeLoad(programInst[pc], mem);
                break;
            case 0b0010011: // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
                iTypeInteger(programInst[pc]);
                break;
            case 0b0001111: // FENCE / FENCE.I
                iTypeFence(programInst[pc]);
                break;
            case 0b1110011: // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
                iTypeStatus(programInst);
                break;

            //S-type instructions
            case 0b0100011: //SB / SH / SW
                sType(programInst[pc], mem);
                break;

            //B-type instructions
            case 0b1100011: // BEQ / BNE / BLT / BGE / BLTU / BGEU
                bType(programInst[pc]);
                break;

            //U-type instructions
            case 0b0110111: //LUI
            case 0b0010111: //AUIPC
                uType(programInst[pc]);
                break;
        }
        reg[0] = 0; // x0 must always be 0
    }

    // JAL and JALR
    private static void jumpType(Instruction inst){
        int imm = 0;
        String type = "opcode??";
        switch(inst.opcode){
            case 0b1101111: //JAL
                type = "JAL";
                imm = inst.immJ; // Used for printing
                reg[inst.rd] = (pc+1)*4; // Store address of next instruction in bytes
                pc += imm/4;
                break;
            case 0b1100111: //JALR
                type = "JALR";
                imm = inst.immI; // Used for printing
                reg[inst.rd] = (pc+1)*4;
                pc = ((reg[inst.rs1] + imm) & 0xFFFFFFFE)/4;
                break;
        }
        System.out.println(String.format("%s x%02d 0x%x", type, inst.rd, imm));
        pc++;
    }

    // R-type instructions: ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
    private static void rType(Instruction inst) {
        String type = "rType??";

        // Used in all instructions, defined to reduce line length
        int rd = inst.rd;
        int rs1 = inst.rs1;
        int rs2 = inst.rs2;

        switch(inst.funct3){
            case 0b000: // ADD / SUB
                switch(inst.funct7){
                    case 0b0000000: // ADD
                        type = "ADD";
                        reg[rd] = reg[rs1] + reg[rs2];
                        break;
                    case 0b0100000: // SUB
                        type = "SUB";
                        reg[rd] = reg[rs1] - reg[rs2];
                        break;
                }
                break;
            case 0b001: // SLL
                type = "SLL";
                reg[rd] = reg[rs1] << reg[rs2];
                break;
            case 0b010: // SLT
                type = "SLT";
                if (reg[rs1] < reg[rs2])
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b011: // SLTU
                type = "SLTU";
                if (Integer.toUnsignedLong(reg[rs1]) < Integer.toUnsignedLong(reg[rs2]))
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b100: // XOR
                type = "XOR";
                reg[rd] = reg[rs1] ^ reg[rs2];
                break;
            case 0b101: // SRL / SRA
                switch(inst.funct7){
                    case 0b0000000: // SRL
                        type = "SRL";
                        reg[rd] = reg[rs1] >>> reg[rs2];
                        break;
                    case 0b0100000: // SRA
                        type = "SRA";
                        reg[rd] = reg[rs1] >> reg[rs2];
                        break;
                }
                break;
            case 0b110: // OR
                type = "OR";
                reg[rd] = reg[rs1] | reg[rs2];
                break;
            case 0b111: // AND
                type = "AND";
                reg[rd] = reg[rs1] & reg[rs2];
                break;
        }
        System.out.println(String.format("%s x%02d x%02d x%02d", type, rd, rs1, rs2));
        pc++;
    }

    // I-type load instructions: LB / LH / LW / LBU / LHU
    private static void iTypeLoad(Instruction inst, Memory mem) {
        int rd = inst.rd;
        int addr = reg[inst.rs1] + inst.immI; // Byte address
        String type;

        switch(inst.funct3){
            // This assumes properly aligned addresses in all scenarios. LH / LW wont work properly if misaligned.
            case 0b000: // LB
                type = "LB";
                reg[rd] = mem.getByte(addr);
                break;
            case 0b001: // LH
                type = "LH";
                reg[rd] = mem.getHalfWord(addr);
                break;
            case 0b010: // LW
                type = "LW";
                reg[rd] = mem.getWord(addr);
                break;
            case 0b100: // LBU
                type = "LBU";
                reg[rd] = mem.getByte(addr) & 0xFF; //Remove sign bits
                break;
            case 0b101: // LHU
                type = "LHU";
                reg[rd] = mem.getHalfWord(addr) & 0xFFFF;
                break;
            default:
                type = "Load??";
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, rd, inst.immI, inst.rs1));
        pc++;
    }

    // I-type integer instructions: ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
    private static void iTypeInteger(Instruction inst) {
        String type = "iType??";

        int immI = inst.immI;
        int rs1 = inst.rs1;
        int rd = inst.rd;

        switch(inst.funct3){
            case 0b000: // ADDI
                type = "ADDI";
                reg[rd] = reg[rs1] + immI;
                break;
            case 0b010: // SLTI
                type = "SLTI";
                if(reg[rs1] < immI)
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b011: // SLTIU
                type = "SLTIU";
                if(Integer.toUnsignedLong(reg[rs1]) < Integer.toUnsignedLong(immI))
                    reg[rd] = 1;
                else
                    reg[rd] = 0;
                break;
            case 0b100: // XORI
                type = "XORI";
                reg[rd] = reg[rs1] ^ immI;
                break;
            case 0b110: // ORI
                type = "ORI";
                reg[rd] = reg[rs1] | immI;
                break;
            case 0b111: // ANDI
                type = "ANDI";
                reg[rd] = reg[rs1] & immI;
                break;
            case 0b001: // SLLI
                type = "SLLI";
                reg[rd] = reg[rs1] << immI;
                break;
            case 0b101: // SRLI / SRAI
                int ShiftAmt = immI & 0x1F;
                switch(inst.funct7){
                    case 0b0000000: // SRLI
                        type = "SRLI";
                        reg[rd] = reg[rs1] >>> ShiftAmt;
                        break;
                    case 0b0100000: // SRAI
                        type = "SRAI";
                        reg[rd] = reg[rs1] >> ShiftAmt;
                        break;
                }
                break;
        }
        System.out.println(String.format("%s x%d x%d %d", type, rd, rs1, immI));
        pc++;
    }

    // I-type status & call instructions: ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
    private static void iTypeStatus(Instruction[] programInst) {
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
    private static void sType(Instruction inst, Memory mem) {
        int addr = reg[inst.rs1] + inst.immS;
        String type = "sType??";
        switch(inst.funct3){
            case 0b000: // SB
                type = "SB";
                mem.storeByte(addr,(byte) reg[inst.rs2]);
                break;
            case 0b001: // SH
                type = "SH";
                mem.storeHalfWord(addr, (short) reg[inst.rs2]);
                break;
            case 0b010: // SW
                type = "SW";
                mem.storeWord(addr, reg[inst.rs2]);
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, inst.rs2, inst.immS, inst.rs1));
        pc++;
    }

    // B-type instructions: BEQ / BNE / BLT / BGE / BLTU / BGEU
    private static void bType(Instruction inst) {
        int ImmB = inst.immB >> 2; //We're counting in words instead of bytes
        String type = "bType??";
        switch(inst.funct3){
            case 0b000: // BEQ
                type = "BEQ";
                pc += (reg[inst.rs1] == reg[inst.rs2])? ImmB : 1;
                break;
            case 0b001: // BNE
                type = "BNE";
                pc += (reg[inst.rs1] != reg[inst.rs2])? ImmB : 1;
                break;
            case 0b100: // BLT
                type = "BLT";
                pc += (reg[inst.rs1] < reg[inst.rs2])? ImmB : 1;
                break;
            case 0b101: // BGE
                type = "BGE";
                pc += (reg[inst.rs1] >= reg[inst.rs2])? ImmB : 1;
                break;
            case 0b110: //BLTU
                type = "BLTU";
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) < Integer.toUnsignedLong(reg[inst.rs2]))? ImmB : 1;
                break;
            case 0b111: //BLGEU
                type = "BLGEU";
                pc += (Integer.toUnsignedLong(reg[inst.rs1]) >= Integer.toUnsignedLong(reg[inst.rs2]))? ImmB : 1;
                break;
        }
        System.out.println(String.format("%s x%d x%d %d", type, inst.rs1, inst.rs2, ImmB<<2));
    }

    // U-type instructions: LUI / AUIPC
    private static void uType(Instruction inst){
        String type = "uType??";
        switch(inst.opcode){
            case 0b0010111: // AUIPC
                type = "AUIPC";
                reg[inst.rd] = pc*4 + inst.immU; // As we count in 4 byte words
                break;
            case 0b0110111: // LUI
                type = "LUI";
                reg[inst.rd] = inst.immU;
                break;
        }
        System.out.println(String.format("%s x%d %d", type, inst.rd, inst.immU>>>12));
        pc++;
    }
    
    // I-type fence instructions: FENCE / FENCE.I
    private static void iTypeFence(Instruction program) {
        switch(program.funct3){
            case 0b000: // FENCE
                break;
            case 0b001: // FENCE.I
                break;
        }
        pc++;
    }
}
