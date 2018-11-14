/* File: Instruction.java
 * The following file contains one instruction and all the fields
 * Made by: Marc Sun Bog & Simon Amtoft Pedersen
 */

package RISCVSimulator;

public class Instruction {
    int opcode, rd, rs1, rs2, funct3, funct7, immI, immS, immB, immU, immJ, instruction;

    public Instruction(int instruction) {
        this.instruction = instruction; 
        this.opcode = instruction & 0x7F;
        this.rd = (instruction >>7) & 0x1F;      // Returns bits 11 to 7
        this.funct3 = (instruction>>12) & 0x7;   // Returns bits 14 to 12
        this.funct7 = (instruction>>25) & 0x7F;  // Returns bits 31 to 25
        this.rs1 = (instruction >>15) & 0x1F;    // Returns bits 19 to 15
        this.rs2 = (instruction>>20) & 0x1F;     // Returns bits 24 to 20
        this.immI = (instruction>>20);           // Returns bits 31 to 20
        this.immS = rd | (funct7<<5); // Returns bits 31 to 25 and 11 to 7
        this.immB = getImmB(instruction);
        this.immU = instruction & 0xFFFFF000;
        this.immJ = getImmJ(instruction);
    }

    private int getImmB (int instruction) {
        int b11 = (instruction>>7) & 0x1;       // 11'th bit of immediate (7th bit of instruction)
        int b1to4 = (instruction>>8) & 0xF;     // Bits 1 to 4 of immediate (8 to 11 of instruction)
        int b5to10 = (instruction>>25) & 0x1F;  // Bits 5 to 10 of immediate (25 to 30 of instruction)
        int b12 = (instruction>>31) & 0x1;      // Bit 12 of immediate (MSB of instruction)

        // Returns bits in the order: imm[12|10:5|4:1|11]
        return b11 << 11 | b1to4 << 1 | b5to10 << 5 | b12 << 12;
    }

    private int getImmJ(int instruction) {
        int b12to19 = (instruction>>12) & 0xFF; // Bits 12 to 19 of immediate (12 to 19 of instruction)
        int b11 = (instruction>>20) & 0x1;      // Bit 11 of immediate (20th bit of instruction)
        int b1to10 = (instruction>>21) & 0x3FF; // Bit 1 to 10 of immediate (21 to 30 of instruction)
        int b20 = (instruction>>31);            // Bit 20 of immediate (MSB of instruction)

        // Returns bits in the order: imm[20|10:1|11|19:12]
        return (b20 << 20 | b12to19 << 12 | b11 << 11 | b1to10 << 1);
    }
}
