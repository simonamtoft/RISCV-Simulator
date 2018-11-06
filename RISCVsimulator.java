/* File: RISCVsimulator.java
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 * The following file simulates the RISC-V instructions set from a binary input file 
 */
import java.io.*;

public class RISCVsimulator {
    static int pc;
    static int[] reg = new int[32];
    
    public static void main(String[] args) throws IOException {
        pc = 0;                                 // Program counter 
        String path = "addlarge.bin";           // Path of binary file 
        int[] program = getInstructions(path);  // Read all instructions from binary file
        
        while (pc < program.length) {
            String str = String.format("Opcode: %02x Rd: %02x Rs1: %02x Rs2: %02x Funct3: %02x Funct7: %02x",
                    instHelper.getOpcode(program[pc]), instHelper.getRd(program[pc]), instHelper.getRs1(program[pc]),
                    instHelper.getRs2(program[pc]), instHelper.getFunct3(program[pc]), instHelper.getFunct7(program[pc]));
            System.out.println(str);
            executeInstruction(program[pc]);
            System.out.println("x"+instHelper.getRd(program[pc])+": " + reg[instHelper.getRd(program[pc])]);
            pc++;
        }
        
        System.out.println("Exiting...");
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
    
    private static int addi() {
        return 0;
    }
    private static void executeInstruction(int instruction){
        int opcode = instHelper.getOpcode(instruction); // Gets the opcode field of instruction
        
        // Funct3 and 7 is so often used that they will be found here, regardless of being used or not
        int funct3 = instHelper.getFunct3(instruction); // Gets the funct3 field of instruction
        int funct7 = instHelper.getFunct7(instruction); // Gets the funct7 field of instruction
        
        switch(opcode){
            // R-type instructions:
            // ADD / SUB / SLL / SLT / SLTU / XOR / SRL / SRA / OR / AND
            case 0b0110011:
                switch(funct3){
                    case 0b000: // ADD / SUB
                        switch(funct7){
                            case 0b0000000: // ADD
                                break;
                            case 0b0100000: // SUB
                                break;
                        }
                        break;
                    case 0b001: // SLL
                        break;
                    case 0b010: // SLT
                        break;
                    case 0b011: // SLTU
                        break;
                    case 0b100: // XOR
                        break;
                    case 0b101: // SRL / SRA
                        switch(funct7){
                            case 0b0000000: // SRL
                                break;
                            case 0b0100000: // SRA
                                break;
                        }
                        break;
                    case 0b110: // OR
                        break;
                    case 0b111: // AND
                        break;
                }
                break;
            // I-type instructions: 
            // JALR
            case 0b1100111:
                break;
            // LB / LH / LW / LBU / LHU
            case 0b0000011:
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
                break;
            // ADDI / SLTI / SLTIU / XORI / ORI / ANDI / SLLI / SRLI / SRAI
            case 0b0010011:
                switch(funct3){
                    case 0b000: // ADDI
                        reg[instHelper.getRd(instruction)] = reg[instHelper.getRs1(instruction)] + instHelper.getImmI(instruction);
                        break;
                    case 0b010: // SLTI
                        break;
                    case 0b011: // SLTIU
                        break;
                    case 0b100: // XORI
                        break;
                    case 0b110: // ORI
                        break;
                    case 0b111: // ANDI
                        break;
                    case 0b001: // SLLI
                        break;
                    case 0b101: // SRLI / SRAI
                        switch(funct7){
                            case 0b0000000: // SRLI
                                break;
                            case 0b0100000: // SRAI
                                break;
                        }
                        break;
                }
                break;
            // FENCE / FENCE.I
            case 0b0001111:
                switch(funct3){
                    case 0b000: // FENCE
                        break;
                    case 0b001: // FENCE.I
                        break;
                }
                break;
            // ECALL / EBREAK / CSRRW / CSRRS / CSRRC / CSRRWI / CSRRSI / CSRRCI
            case 0b1110011:
                switch(funct3){
                    case 0b000: // ECALL / EBREAK
                        switch(instHelper.getImmI(instruction)){
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
                break;
            //S-type
            //SB / SH / SW
            case 0b0100011:
                switch(funct3){
                    case 0b000: // SB
                        break;
                    case 0b001: // SH
                        break;
                    case 0b010: // SW
                        break;
                }
                break;
            //B-type
            //BEQ / BNE / BLT / BGE / BLTU / BGEU
            case 0b1100011:
                switch(funct3){
                    case 0b000: // BEQ
                        break;
                    case 0b001: // BNE
                        break;
                    case 0b100: // BLT
                        break;
                    case 0b101: // BGE
                        break;
                    case 0b110: //BLTU
                        break;
                    case 0b111: //BLGEU
                        break;
                }
                break;
            //U-type
            //LUI
            case 0b0110111:
                break;
            //AUIPC
            case 0b0010111:
                reg[instHelper.getRd(instruction)] = instHelper.getImmU(instruction);
                break;

            //J-type
            //JAL
            case 0b1101111:
                break;

        }
    }
}
