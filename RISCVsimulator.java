/* File: RISCVsimulator.java
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 * The following file simulates the RISC-V instructions set from a binary input file
 */

import java.io.*;

public class RISCVsimulator extends guiHelper {
    private static int pc = 0;                          // Program counter
    private static int[] program;                       // Array of program instructions
    private static int[] reg = new int[32];             // Define register to be array of 32 elements (x0 to x31)
    private static int[] memory = new int[128*1000/4];  // 128KB of memory allocated.
    
    static guiHelper gui = new guiHelper();
    
    public static void main(String[] args) throws IOException {
        guiHelper.launch(args);
        String test = "addlarge";           // Name of test file
        String dir = "tests\\";             // Directory
        String path = dir+test+".bin";      // Path of binary file
        program = getInstructions(path);    // Read all instructions from binary file
        reg[2] = 128*1000-4;                // Initialize sp to last word in memory.

        System.out.println("Machine code \t Basic code");

        while (pc < program.length) {
            System.out.print(String.format("0x%08X",program[pc]) + "\t\t");
            executeInstruction(program[pc]);
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

            // J-type instruction
            case 0b1101111: //JAL
                jumpTypes(instruction, opcode);

                // I-type instructions
            case 0b1100111: // JALR
                jumpTypes(instruction, opcode);
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

    // JAL and JALR
    private static void jumpTypes(int instruction, int opcode){
        int Rd = instHelper.getRd(instruction), Rs = 0, Imm = 0;
        String type = "Unrecognized Opcode";
        switch(opcode){
            case 0b1101111: //JAL
                type = "JAL";
                Imm = instHelper.getImmJ(instruction);
                reg[Rd] = (pc+1)*4; // Store address of next instruction in bytes
                pc += Imm/4;
                break;
            case 0b1100111: //JALR
                type = "JALR";
                Rs = instHelper.getRs1(instruction);
                Imm = instHelper.getImmI(instruction);
                reg[Rd] = (pc+1)*4;
                pc = ((reg[Rs] + Imm) & 0xFFFFFFFE)/4;
                break;
        }
        System.out.println(String.format("%s x%02d 0x%x", type, Rd, Imm));
        pc++;
    }

    // R-type instructions: ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
    private static void rType(int instruction) {
        // Get fields
        int funct3 = instHelper.getFunct3(instruction);
        int funct7 = instHelper.getFunct7(instruction);
        int Rd = instHelper.getRd(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int Rs2 = instHelper.getRs2(instruction);
        String type = "Unrecognized Opcode";
        switch(funct3){
            case 0b000: // ADD / SUB
                switch(funct7){
                    case 0b0000000: // ADD
                        type = "ADD";
                        reg[Rd] = reg[Rs1] + reg[Rs2];
                        break;
                    case 0b0100000: // SUB
                        type = "SUB";
                        reg[Rd] = reg[Rs1] - reg[Rs2];
                        break;
                }
                break;
            case 0b001: // SLL
                type = "SLL";
                reg[Rd] = reg[Rs1] << reg[Rs2];
                break;
            case 0b010: // SLT
                type = "SLT";
                if (reg[Rs1] < reg[Rs2])
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b011: // SLTU
                type = "SLTU";
                if (Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(reg[Rs2]))
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b100: // XOR
                type = "XOR";
                reg[Rd] = reg[Rs1] ^ reg[Rs2];
                break;
            case 0b101: // SRL / SRA
                switch(funct7){
                    case 0b0000000: // SRL
                        type = "SRL";
                        reg[Rd] = reg[Rs1] >>> reg[Rs2];
                        break;
                    case 0b0100000: // SRA
                        type = "SRA";
                        reg[Rd] = reg[Rs1] >> reg[Rs2];
                        break;
                }
                break;
            case 0b110: // OR
                type = "OR";
                reg[Rd] = reg[Rs1] | reg[Rs2];
                break;
            case 0b111: // AND
                type = "AND";
                reg[Rd] = reg[Rs1] & reg[Rs2];
                break;
        }
        
        // replacing node in gui table here??
        // gui.replaceNode(Rd,String.format("%s",reg[Rd]));
        
        System.out.println(String.format("%s x%02d x%02d x%02d", type, Rd, Rs1, Rs2));
        pc++;
    }

    // I-type load instructions: LB / LH / LW / LBU / LHU
    private static void iTypeLoad(int instruction) {
        int funct3 = instHelper.getFunct3(instruction);
        int Rd = instHelper.getRd(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int ImmI = instHelper.getImmI(instruction);
        int addr = reg[Rs1] + ImmI; // Byte address
        String type;

        switch(funct3){
            // This assumes properly aligned addresses in all scenarios. LH / LW wont work properly if misaligned.
            case 0b000: // LB
                type = "LB";
                reg[Rd] = (byte) ((memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFF); //Get the entire word by dividing addr by 4. Find byte-offset by (addr & 0x3) and multiply by 8 for needed bitshift value.
                break;
            case 0b001: // LH
                type = "LH";
                reg[Rd] = (short) ((memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFFFF);
                System.out.println((addr >> 2) + " " + (addr&0x2));
                break;
            case 0b010: // LW
                type ="LW";
                reg[Rd] = memory[addr >> 2]; //Load entire word, does not work with misaligned address.
                break;
            case 0b100: // LBU
                type = "LBU";
                reg[Rd] = (memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFF; //Remove sign bits
                break;
            case 0b101: // LHU
                type = "LHU";
                reg[Rd] = (memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFFFF;
                break;
            default:
                type = "Unrecognized Opcode";
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, Rd, ImmI, Rs1));
        pc++;
    }

    // I-type integer instructions: ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
    private static void iTypeInteger(int instruction) {
        // Gets commonly used fields
        int Rd = instHelper.getRd(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int ImmI = instHelper.getImmI(instruction);
        int funct3 = instHelper.getFunct3(instruction);
        String type = "Unrecognized Opcode";
        switch(funct3){
            case 0b000: // ADDI
                type = "ADDI";
                reg[Rd] = reg[Rs1] + ImmI;
                break;
            case 0b010: // SLTI
                type = "SLTI";
                if(reg[Rs1] < ImmI)
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b011: // SLTIU
                type = "SLTIU";
                if(Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(ImmI))
                    reg[Rd] = 1;
                else
                    reg[Rd] = 0;
                break;
            case 0b100: // XORI
                type = "XORI";
                reg[Rd] = reg[Rs1] ^ ImmI;
                break;
            case 0b110: // ORI
                type = "ORI";
                reg[Rd] = reg[Rs1] | ImmI;
                break;
            case 0b111: // ANDI
                type = "ANDI";
                reg[Rd] = reg[Rs1] & ImmI;
                break;
            case 0b001: // SLLI
                type = "SLLI";
                reg[Rd] = reg[Rs1] << ImmI;
                break;
            case 0b101: // SRLI / SRAI
                int ShiftAmt = ImmI & 0x1F;
                int funct7 = instHelper.getFunct7(instruction);
                switch(funct7){
                    case 0b0000000: // SRLI
                        type = "SRLI";
                        reg[Rd] = reg[Rs1] >>> ShiftAmt;
                        break;
                    case 0b0100000: // SRAI
                        type = "SRAI";
                        reg[Rd] = reg[Rs1] >> ShiftAmt;
                        break;
                }
                break;
        }
        System.out.println(String.format("%s x%d x%d %d", type, Rd, Rs1, ImmI));
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
                                pc = program.length; // Sets program counter to end of program, to program loop
                                return;              // Exits 'iTypeStatus' function and returns to loop.
                            case 11:    // print_character
                                System.out.print((char) reg[11]);
                                break;
                            case 17:    // exit2
                                pc = program.length;
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
    private static void sType(int instruction) {
        int funct3 = instHelper.getFunct3(instruction); // Gets the funct3 field of instruction
        int Rs1 = instHelper.getRs1(instruction);
        int Rs2 = instHelper.getRs2(instruction);
        int ImmS = instHelper.getImmS(instruction);
        int addr = reg[Rs1] + ImmS;
        String type = "Unrecognized Opcode";
        switch(funct3){
            case 0b000: // SB
                type = "SB";
                memory[addr >> 2] &= ~(0xFF << ((addr & 0x3) << 3)); //Same logic as LB instruction. We clear the byte at the word-address + offset.
                memory[addr >> 2] |= (reg[Rs2] & 0xFF) << ((addr & 0x3) << 3); //Store byte from reg[Rs2] at word-address + offset
                break;
            case 0b001: // SH
                type = "SH";
                memory[addr >> 2] &= ~(0xFFFF << ((addr & 0x2) << 3)); //Same logic as LH instruction. We clear the half-word at the word-address + offset.
                memory[addr >> 2] |= (reg[Rs2] & 0xFFFF) << ((addr & 0x2) << 3); //Store half-word from reg[Rs2] at word-address + offset
                break;
            case 0b010: // SW
                type = "SW";
                memory[addr >> 2] = reg[Rs2];
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, Rs2, ImmS, Rs1));
        pc++;
    }

    // B-type instructions: BEQ / BNE / BLT / BGE / BLTU / BGEU
    private static void bType(int instruction) {
        int funct3 = instHelper.getFunct3(instruction);
        int Rs1 = instHelper.getRs1(instruction);
        int Rs2 = instHelper.getRs2(instruction);
        int ImmB = instHelper.getImmB(instruction) >> 2; //We're counting in words instead of bytes
        String type = "Unrecognized Opcode";
        switch(funct3){
            case 0b000: // BEQ
                type = "BEQ";
                pc += (reg[Rs1] == reg[Rs2])? ImmB : 1;
                break;
            case 0b001: // BNE
                type = "BNE";
                pc += (reg[Rs1] != reg[Rs2])? ImmB : 1;
                break;
            case 0b100: // BLT
                type = "BLT";
                pc += (reg[Rs1] < reg[Rs2])? ImmB : 1;
                break;
            case 0b101: // BGE
                type = "BGE";
                pc += (reg[Rs1] >= reg[Rs2])? ImmB : 1;
                break;
            case 0b110: //BLTU
                type = "BLTU";
                pc += (Integer.toUnsignedLong(reg[Rs1]) < Integer.toUnsignedLong(reg[Rs2]))? ImmB : 1;
                break;
            case 0b111: //BLGEU
                type = "BLGEU";
                pc += (Integer.toUnsignedLong(reg[Rs1]) >= Integer.toUnsignedLong(reg[Rs2]))? ImmB : 1;
                break;
        }
        System.out.println(String.format("%s x%d x%d %d", type, Rs1, Rs2, ImmB<<2));
    }

    // U-type instructions: LUI / AUIPC
    private static void uType(int instruction, int opcode){
        int Rd = instHelper.getRd(instruction);
        int ImmU = instHelper.getImmU(instruction);
        String type = "Unrecognized Opcode";
        switch(opcode){
            case 0b0010111: // AUIPC
                type = "AUIPC";
                reg[Rd] = pc*4 + ImmU; // As we count in 4 byte words
                break;
            case 0b0110111: // LUI
                type = "LUI";
                reg[Rd] = ImmU;
                break;
        }
        System.out.println(String.format("%s x%d %d", type, Rd, ImmU>>>12));
        pc++;
    }


    // Outputs registers x0 to x31 in file "output.bin", and prints them in console.
    private static void endOfProgram(int[] reg) throws IOException{

        // Output to binary file
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();

        // Print out register content of x0 to x31
        System.out.println("\nRegister contents:");
        for (int i = 0; i < reg.length; i++) {
            if (reg[i] != 0)
                System.out.println("x"+i+": " + reg[i]);
        }
    }
}
