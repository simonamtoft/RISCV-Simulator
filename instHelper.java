/* File: instHelper.java
 * The following file contains functions for getting the fields of the different instruction
 * types for the RV32I Base instruction set: R-, I-, S-, B-, U- and J-type.
 *
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 */

class instHelper {
    static int getOpcode(int inst) {
        return inst & 0x7F; // Returns 7 right-most bits of instruction
    }

    static int getRd(int inst) {
        return (inst >>> 7) & 0x1F; // Returns bits 11 to 7
    }

    static int getFunct3(int inst) {
        return (inst >>> 12) & 0x7; // Returns bits 14 to 12
    }

    static int getRs1 (int inst) {
        return (inst >>> 15) & 0x1F; // Returns bits 19 to 15
    }

    static int getRs2 (int inst) {
        return (inst >>> 20) & 0x1F; // Returns bits 24 to 20
    }

    static int getFunct7 (int inst) {
        return (inst >>> 25) & 0x7F; // Returns bits 31 to 25
    }

    static int getImmI (int inst) {
        return (inst >> 20); // Returns bits 31 to 20 (sign-extended to 32-bits)
    }

    static int getImmS (int inst) {
        return getRd(instruction) | (getFunct7(instruction) << 5); // Returns bits 31 to 25 and 11 to 7
    }

    static int getImmB (int inst) {
        return (((((inst >> 20) & 0xFFFFFFE0) | 
                  ((inst >>> 7) & 0x0000001F)) & 0xFFFFF7FE) | 
                (((((inst >> 20) & 0xFFFFFFE0) | 
                   ((inst >>> 7) & 0x0000001F)) & 0x00000001) << 11));
    }

    static int getImmU(int inst){
        return inst & 0xFFFFF000; // Returns immediate
    }

    static int getImmJ(int inst) {
        return (((inst >> 20) & 0xFFF007FE) | ((inst >>> 9) & 0x00000800) | (inst & 0x000FF000));
    }
    static int getImmCSR(int inst){
        return (inst >>> 20); //Logical shift
    }
}
