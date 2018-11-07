/* File: instHelper.java
 * The following file contains functions for getting the fields of the different instruction
 * types for the RV32I Base instruction set: R-, I-, S-, B-, U- and J-type.
 *
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 */

class instHelper {
    static int getOpcode(int instruction) {
        return instruction & 0x7F; // Returns 7 right-most bits of instruction
    }

    static int getRd(int instruction) {
        return (instruction >>> 7) & 0x1F; // Returns bits 11 to 7
    }

    static int getFunct3(int instruction) {
        return (instruction >>> 12) & 0x7; // Returns bits 14 to 12
    }

    static int getRs1 (int instruction) {
        return (instruction >>> 15) & 0x1F; // Returns bits 19 to 15
    }

    static int getRs2 (int instruction) {
        return (instruction >>> 20) & 0x1F; // Returns bits 24 to 20
    }

    static int getFunct7 (int instruction) {
        return (instruction >>> 25) & 0x7F; // Returns bits 31 to 25
    }

    static int getImmI (int instruction) {
        return (instruction >> 20); // Returns bits 31 to 20 (sign-extended to 32-bits)
    }

    static int getImmS (int instruction) {
        return getRd(instruction) | (getFunct7(instruction) << 5); // Returns bits 31 to 25 and 11 to 7
    }

    static int getImmB (int instruction) {
        return (((((instr >> 20) & 0xFFFFFFE0) | ((instr >>> 7) & 0x0000001F)) & 0xFFFFF7FE) | ((   (((instr >> 20) & 0xFFFFFFE0) | ((instr >>> 7) & 0x0000001F)) & 0x00000001) << 11));
    }

    static int getImmU(int instruction){
        return instruction & 0xFFFFF000; // Returns immediate
    }

    static int getImmJ(int instruction) {
        //return (((instr >> 20) & 0xFFF007FE) | ((instr >>> 9) & 0x00000800) | (instr & 0x000FF000));
        int b12to19 = (instruction>>12) & 0xFF; // Bits 12 to 19 of immediate (12 to 19 of instruction)
        int b11 = (instruction>>20) & 0x1;      // Bit 11 of immediate (20th bit of instruction)
        int b1to10 = (instruction>>21) & 0x3FF; // Bit 1 to 10 of immediate (21 to 30 of instruction)
        int b20 = (instruction>>31);            // Bit 20 of immediate (MSB of instruction)

        // Returns bits in the order: imm[20|10:1|11|19:12]
        return (b20 << 20 | b12to19 << 12 | b11 << 11 | b1to10 << 1);
    }
    static int getImmCSR(int instr){
        return (instr >>> 20); //Logical shift
    }
}
