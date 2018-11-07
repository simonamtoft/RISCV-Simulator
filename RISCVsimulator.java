/* File: RISCVsimulator.java
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 * The following file simulates the RISC-V instructions set from a binary input file
 */
import java.io.*;

public class RISCVsimulator {
    private static int pc;
    private static int[] reg = new int[32];

    public static void main(String[] args) throws IOException {
        pc = 0;                                 // Program counter
        String path = "tests\\addlarge.bin";           // Path of binary file
        int[] program = getInstructions(path);  // Read all instructions from binary file

        while (pc < program.length) {
            String str = String.format("Opcode: %02x Rd: %02x Rs1: %02x Rs2: %02x Funct3: %02x Funct7: %02x",
                    instHelper.getOpcode(program[pc]), instHelper.getRd(program[pc]), instHelper.getRs1(program[pc]),
                    instHelper.getRs2(program[pc]), instHelper.getFunct3(program[pc]), instHelper.getFunct7(program[pc]));
            System.out.println(str);
            executeInstruction(program[pc]);
            System.out.println("x"+instHelper.getRd(program[pc-1])+": " + reg[instHelper.getRd(program[pc-1])]); //PC is incremented by executeInstruction, so we need to use the previous value
        }

        endOfProgram(reg);
    }

    // Returns array of 32-bit instructions from input file given in 'path'
    private static int[] getInstructions(String path) throws IOException {
        File f = new File(path);
        int[] inst = new int[(int) f.length()/4];
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        for(int i = 0; i < inst.length; i++){
            inst[i] = Integer.reverseBytes(dis.readInt()); //Change endianness for easier bitwise manipulation
        }
        dis.close();
        return inst;
    }

    // Executes one instruction
    private static void executeInstruction(int instruction){
        int opcode = instHelper.getOpcode(instruction); // Gets the opcode field of instruction

        switch(opcode){
            // R-type instructions
            case 0b0110011: // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
                rType(instruction);
                break;
                
            //J-type instruction
            case 0b1101111: //JAL
                break;
                
            // I-type instructions
            case 0b1100111: // JALR
                break;
            case 0b0000011: // LB / LH / LW / LBU / LHU
                iTypeLoad(instruction);
                break;
            case 0b0010011: // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
                iTypeInteger(instruction);
                break;
            case 0b0001111: // FENCE / FENCE.I
                iTypeFence(instruction);
                break;
            case 0b1110011: // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
                iTypeStatus(instruction);
                break;

            //S-type instructions
            case 0b0100011: //SB / SH / SW
                sType(instruction);
                break;

            //B-type instructions
            case 0b1100011: // BEQ / BNE / BLT / BGE / BLTU / BGEU
                bType(instruction);
                break;

            //U-type instructions
            case 0b0110111: //LUI
            case 0b0010111: //AUIPC
                uType(instruction, opcode);
                break; 
        }
        reg[0] = 0; // x0 must always be 0
    }

    // R-type instructions: ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
    private static void rType(int instruction) {
        // Get fields
        int funct3 = instHelper.getFunct3(instruction);
        int funct7 = instHelper.getFunct7(instruction);
        int Rd = instHelper.getRd(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int Rs2 = instHelper.getRs2(instruction);

        switch(funct3){
            case 0b000: // ADD / SUB
                switch(funct7){
                    case 0b0000000: // ADD
                        reg[Rd] = reg[Rs1] + reg[Rs2];
                        break;
                    case 0b0100000: // SUB
                        reg[Rd] = reg[Rs1] - reg[Rs2];
                        break;
                }
                break;
            case 0b001: // SLL
                reg[Rd] = reg[Rs1] << reg[Rs2];
                break;
            case 0b010: // SLT
                if (reg[Rs1] < reg[Rs2])
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b011: // SLTU
                if (Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(reg[Rs2]))
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b100: // XOR
                reg[Rd] = reg[Rs1] ^ reg[Rs2];
                break;
            case 0b101: // SRL / SRA
                switch(funct7){
                    case 0b0000000: // SRL
                        reg[Rd] = reg[Rs1] >>> reg[Rs2];
                        break;
                    case 0b0100000: // SRA
                        reg[Rd] = reg[Rs1] >> reg[Rs2];
                        break;
                }
                break;
            case 0b110: // OR
                reg[Rd] = reg[Rs1] | reg[Rs2];
                break;
            case 0b111: // AND
                reg[Rd] = reg[Rs1] & reg[Rs2];
                break;
        }
        pc++;
    }

    // I-type load instructions: LB / LH / LW / LBU / LHU
    private static void iTypeLoad(int instruction) {
        int funct3 = instHelper.getFunct3(instruction); // Gets the funct3 field of instruction
        switch(funct3){
            case 0b000: // LB
                break;
            case 0b001: // LH
                break;
            case 0b010: // LW
                break;
            case 0b100: // LBU
                break;
            case 0b101: // LHU
                break;
        }
        pc++;
    }

    // I-type integer instructions: ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
    private static void iTypeInteger(int instruction) {
        // Gets commonly used fields
        int Rd = instHelper.getRd(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int ImmI = instHelper.getImmI(instruction);
        int funct3 = instHelper.getFunct3(instruction);

        switch(funct3){
            case 0b000: // ADDI
                reg[Rd] = reg[Rs1] + ImmI;
                break;
            case 0b010: // SLTI
                if(reg[Rs1] < ImmI)
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b011: // SLTIU
                // REG RS1 SKAL VÆRE UNSIGNED???
                if(Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(ImmI))
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b100: // XORI
                reg[Rd] = reg[Rs1] ^ ImmI;
                break;
            case 0b110: // ORI
                reg[Rd] = reg[Rs1] | ImmI;
                break;
            case 0b111: // ANDI
                reg[Rd] = reg[Rs1] & ImmI;
                break;
            case 0b001: // SLLI
                reg[Rd] = reg[Rs1] << ImmI;
                break;
            case 0b101: // SRLI / SRAI
                int ShiftAmt = ImmI & 0x1F;
                int funct7 = instHelper.getFunct7(instruction);
                switch(funct7){
                    case 0b0000000: // SRLI
                        reg[Rd] = reg[Rs1] >>> ShiftAmt;
                        break;
                    case 0b0100000: // SRAI
                        reg[Rd] = reg[Rs1] >> ShiftAmt;
                        break;
                }
                break;
        }
        pc++;
    }

    // I-type fence instructions: FENCE / FENCE.I
    private static void iTypeFence(int instruction) {
        int funct3 = instHelper.getFunct3(instruction);
        switch(funct3){
            case 0b000: // FENCE
                break;
            case 0b001: // FENCE.I
                break;
        }
        pc++;
    }

    // I-type status & call instructions: ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
    private static void iTypeStatus(int instruction) {
        int ImmI;
        int funct3 = instHelper.getFunct3(instruction);
        switch(funct3){
            case 0b000: // ECALL / EBREAK
                ImmI = instHelper.getImmI(instruction);
                switch(ImmI){
                    case 0b000000000000: // ECALL
                        break;
                    case 0b000000000001: // EBREAK
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
    private static void sType(int instruction) {
        int funct3 = instHelper.getFunct3(instruction); // Gets the funct3 field of instruction
        switch(funct3){
            case 0b000: // SB
                break;
            case 0b001: // SH
                break;
            case 0b010: // SW
                break;
        }
    }

    // B-type instructions: BEQ / BNE / BLT / BGE / BLTU / BGEU
    private static void bType(int instruction) {
        int funct3 = instHelper.getFunct3(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int Rs2 = instHelper.getRs2(instruction);
        int ImmB = instHelper.getImmB(instruction) / 4; //We're counting in words instead of bytes

        switch(funct3){
            case 0b000: // BEQ
                pc += (reg[Rs1] == reg[Rs2]) ? ImmB : 1;
                break;
            case 0b001: // BNE
                pc += (reg[Rs1] != reg[Rs2]) ? ImmB : 1;
                break;
            case 0b100: // BLT
                pc += (reg[Rs1] < reg[Rs2]) ? ImmB : 1;
                break;
            case 0b101: // BGE
                pc += (reg[Rs1] >= reg[Rs2]) ? ImmB : 1;
                break;
            case 0b110: //BLTU
                pc += (Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(reg[Rs2])) ? ImmB : 1;
                break;
            case 0b111: //BLGEU
                pc += (Integer.toUnsignedLong(reg[Rs1]) >= Integer.toUnsignedLong(reg[Rs2])) ? ImmB : 1;
                break;
        }
    }
    
    // U-type instructions: LUI / AUIPC
    private static void uType(int instruction, int opcode){
        int Rd = instHelper.getRd(instruction);
        int ImmU = instHelper.getImmU(instruction);
        switch(opcode){
            case 0b0010111: // AUIPC
                reg[Rd] = pc;
            case 0b0110111: // LUI
                reg[Rd] += ImmU;
                break;
        }
        pc++;
    }
    
    // Prints the contents of the registers x0 to x31
    private static void printRegisterContent(int reg[]) {
        for (int i = 0; i < reg.length; i++) {
            if (reg[i] != 0)
                System.out.println("x"+i+": " + reg[i]);
        }
    }

    // Outputs registers x0 to x31 in file "output.bin", and prints them in console.
    private static void endOfProgram(int[] reg) throws IOException{
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(val);
        }
        dos.close();

        printRegisterContent(reg);
        System.out.println("Exiting...");
    }
}
