import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class processor {
    static int pc = 0;                          // Program counter
    static int[] program;                       // Array of program instructions
    static int[] reg = new int[32];             // Define register to be array of 32 elements (x0 to x31)
    private static int[] memory = new int[512]; // 2048 bytes of memory allocated.

    private static int opcode, rs1, rs2, funct3, funct7, immI, immS, immB, immU, immJ;
    static int rd;

    // Executes one instruction
    public static void executeInstruction(){
        int instruction = program[pc];
        processor.getFields(instruction); // Get fields for current instruction

        switch(opcode){
            // R-type instructions
            case 0b0110011: // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
                rType();
                break;

            // J-type instruction
            case 0b1101111: //JAL
                jumpType();
                break;
                // I-type instructions
            case 0b1100111: // JALR
                jumpType();
                break;
            case 0b0000011: // LB / LH / LW / LBU / LHU
                iTypeLoad();
                break;
            case 0b0010011: // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
                iTypeInteger();
                break;
            case 0b0001111: // FENCE / FENCE.I
                iTypeFence();
                break;
            case 0b1110011: // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
                iTypeStatus();
                break;

            //S-type instructions
            case 0b0100011: //SB / SH / SW
                sType();
                break;

            //B-type instructions
            case 0b1100011: // BEQ / BNE / BLT / BGE / BLTU / BGEU
                bType();
                break;

            //U-type instructions
            case 0b0110111: //LUI
            case 0b0010111: //AUIPC
                uType();
                break;
        }
        reg[0] = 0; // x0 must always be 0
    }

    // Puts instructions from input bin file into 'program' array
    public static void getInstructions(String path) throws IOException {
        File f = new File(path);
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        program = new int[(int) f.length()/4];
        for(int i = 0; i < program.length; i++){
            program[i] = Integer.reverseBytes(dis.readInt()); //Change endianness for easier bitwise manipulation
        }
        dis.close();
    }

    // Get all fields of instruction
    private static void getFields(int instruction) {
        opcode = instruction & 0x7F;
        rd = (instruction >>7) & 0x1F;      // Returns bits 11 to 7
        funct3 = (instruction>>12) & 0x7;   // Returns bits 14 to 12
        funct7 = (instruction>>25) & 0x7F;  // Returns bits 31 to 25
        rs1 = (instruction >>15) & 0x1F;    // Returns bits 19 to 15
        rs2 = (instruction>>20) & 0x1F;     // Returns bits 24 to 20
        immI = (instruction>>20);           // Returns bits 31 to 20
        immS = rd | (funct7<<5); // Returns bits 31 to 25 and 11 to 7
        immB = getImmB(instruction);
        immU = instruction & 0xFFFFF000;
        immJ = getImmJ(instruction);
    }

    private static int getImmB (int instruction) {
        int b11 = (instruction>>7) & 0x1;       // 11'th bit of immediate (7th bit of instruction)
        int b1to4 = (instruction>>8) & 0xF;     // Bits 1 to 4 of immediate (8 to 11 of instruction)
        int b5to10 = (instruction>>25) & 0x1F;  // Bits 5 to 10 of immediate (25 to 30 of instruction)
        int b12 = (instruction>>31) & 0x1;      // Bit 12 of immediate (MSB of instruction)

        // Returns bits in the order: imm[12|10:5|4:1|11]
        return b11 << 11 | b1to4 << 1 | b5to10 << 5 | b12 << 12;
    }

    private static int getImmJ(int instruction) {
        int b12to19 = (instruction>>12) & 0xFF; // Bits 12 to 19 of immediate (12 to 19 of instruction)
        int b11 = (instruction>>20) & 0x1;      // Bit 11 of immediate (20th bit of instruction)
        int b1to10 = (instruction>>21) & 0x3FF; // Bit 1 to 10 of immediate (21 to 30 of instruction)
        int b20 = (instruction>>31);            // Bit 20 of immediate (MSB of instruction)

        // Returns bits in the order: imm[20|10:1|11|19:12]
        return (b20 << 20 | b12to19 << 12 | b11 << 11 | b1to10 << 1);
    }

    // JAL and JALR
    private static void jumpType(){
        int imm = 0;
        String type = "opcode??";
        switch(opcode){
            case 0b1101111: //JAL
                type = "JAL";
                reg[rd] = (pc+1)*4; // Store address of next instruction in bytes
                pc += immJ/4;
                imm = immJ; // Used for printing
                break;
            case 0b1100111: //JALR
                type = "JALR";
                reg[rd] = (pc+1)*4;
                pc = ((reg[rs1] + immI) & 0xFFFFFFFE)/4;
                imm = immI; // Used for printing
                break;
        }
        System.out.println(String.format("%s x%02d 0x%x", type, rd, imm));
        pc++;
    }

    // R-type instructions: ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
    private static void rType() {
        String type = "rType??";
        switch(funct3){
            case 0b000: // ADD / SUB
                switch(funct7){
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
                switch(funct7){
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
    private static void iTypeLoad() {
        int addr = reg[rs1] + immI; // Byte address
        String type;

        switch(funct3){
            // This assumes properly aligned addresses in all scenarios. LH / LW wont work properly if misaligned.
            case 0b000: // LB
                type = "LB";
                reg[rd] = (byte) ((memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFF); //Get the entire word by dividing addr by 4. Find byte-offset by (addr & 0x3) and multiply by 8 for needed bitshift value.
                break;
            case 0b001: // LH
                type = "LH";
                reg[rd] = (short) ((memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFFFF);
                System.out.println((addr >> 2) + " " + (addr&0x2));
                break;
            case 0b010: // LW
                type ="LW";
                reg[rd] = memory[addr >> 2]; //Load entire word, does not work with misaligned address.
                break;
            case 0b100: // LBU
                type = "LBU";
                reg[rd] = (memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFF; //Remove sign bits
                break;
            case 0b101: // LHU
                type = "LHU";
                reg[rd] = (memory[addr >> 2] >> ((addr & 0x3) << 3)) & 0xFFFF;
                break;
            default:
                type = "Unrecognized Opcode";
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, rd, immI, rs1));
        pc++;
    }

    // I-type integer instructions: ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
    private static void iTypeInteger() {
        String type = "iType??";
        switch(funct3){
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
                switch(funct7){
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

    // I-type fence instructions: FENCE / FENCE.I
    private static void iTypeFence() {
        switch(funct3){
            case 0b000: // FENCE
                break;
            case 0b001: // FENCE.I
                break;
        }
        pc++;
    }

    // I-type status & call instructions: ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
    private static void iTypeStatus() {
        switch(funct3){
            case 0b000: // ECALL / EBREAK
                switch(immI){
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
    private static void sType() {
        int addr = reg[rs1] + immS;
        String type = "sType??";
        switch(funct3){
            case 0b000: // SB
                type = "SB";
                memory[addr >> 2] &= ~(0xFF << ((addr & 0x3) << 3));            // Same logic as LB instruction. We clear the byte at the word-address + offset.
                memory[addr >> 2] |= (reg[rs2] & 0xFF) << ((addr & 0x3) << 3);  // Store byte from reg[Rs2] at word-address + offset
                break;
            case 0b001: // SH
                type = "SH";
                memory[addr >> 2] &= ~(0xFFFF << ((addr & 0x2) << 3));           // Same logic as LH instruction. We clear the half-word at the word-address + offset.
                memory[addr >> 2] |= (reg[rs2] & 0xFFFF) << ((addr & 0x2) << 3); //Store half-word from reg[Rs2] at word-address + offset
                break;
            case 0b010: // SW
                type = "SW";
                memory[addr >> 2] = reg[rs2];
                break;
        }
        System.out.println(String.format("%s x%d %d(x%d)", type, rs2, immS, rs1));
        pc++;
    }

    // B-type instructions: BEQ / BNE / BLT / BGE / BLTU / BGEU
    private static void bType() {
        int ImmB = immB >> 2; //We're counting in words instead of bytes
        String type = "bType??";
        switch(funct3){
            case 0b000: // BEQ
                type = "BEQ";
                pc += (reg[rs1] == reg[rs2])? ImmB : 1;
                break;
            case 0b001: // BNE
                type = "BNE";
                pc += (reg[rs1] != reg[rs2])? ImmB : 1;
                break;
            case 0b100: // BLT
                type = "BLT";
                pc += (reg[rs1] < reg[rs2])? ImmB : 1;
                break;
            case 0b101: // BGE
                type = "BGE";
                pc += (reg[rs1] >= reg[rs2])? ImmB : 1;
                break;
            case 0b110: //BLTU
                type = "BLTU";
                pc += (Integer.toUnsignedLong(reg[rs1]) < Integer.toUnsignedLong(reg[rs2]))? ImmB : 1;
                break;
            case 0b111: //BLGEU
                type = "BLGEU";
                pc += (Integer.toUnsignedLong(reg[rs1]) >= Integer.toUnsignedLong(reg[rs2]))? ImmB : 1;
                break;
        }
        System.out.println(String.format("%s x%d x%d %d", type, rs1, rs2, ImmB<<2));
    }

    // U-type instructions: LUI / AUIPC
    private static void uType(){
        String type = "uType??";
        switch(opcode){
            case 0b0010111: // AUIPC
                type = "AUIPC";
                reg[rd] = pc*4 + immU; // As we count in 4 byte words
                break;
            case 0b0110111: // LUI
                type = "LUI";
                reg[rd] = immU;
                break;
        }
        System.out.println(String.format("%s x%d %d", type, rd, immU>>>12));
        pc++;
    }

    public static void printMachineCode() {
        System.out.print(String.format("0x%08X", program[pc]) + "\t\t");
    }
}
