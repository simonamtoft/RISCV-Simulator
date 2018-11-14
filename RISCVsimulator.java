/* File: RISCVsimulator.java
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 * The following file simulates the RISC-V instructions set from a binary input file
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;

public class RISCVsimulator extends Application {
    private static int pc = 0;                          // Program counter
    private static int[] program;                       // Array of program instructions
    private static int[] reg = new int[32];             // Define register to be array of 32 elements (x0 to x31)
    private static int[] memory = new int[128*1000/4];  // 128KB of memory allocated.

    // Defined here so other methods can manipulate them
    private static Text val1, val2, val3, val4, val5, val6, val7, val8, val9, val10, val11, val12, val13, val14, val15, val16;
    private static Text val17, val18,val19, val20, val21, val22, val23, val24, val25, val26, val27, val28, val29, val30, val31;
    private static GridPane gridReg;
    private static Stage window;

    public static void main(String[] args) throws Exception {
        initGridReg();                              // Initialize gui reg values text
        String test = "addlarge";               // Name of test file
        String dir = "tests\\";                 // Directory
        String path = dir+test+".bin";          // Path of binary file
        program = getInstructions(path);        // Read all instructions from binary file
        reg[2] = 128*1000-4;                    // Initialize sp to last word in memory.
        replaceNode(2, ""+reg[2]);

        System.out.println("Machine code \t Basic code");

        while (pc < program.length) {
            System.out.print(String.format("0x%08X",program[pc]) + "\t\t");
            executeInstruction(program[pc]);
        }
        outToBin(reg);
        launch(args);
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
        int Rd = instHelper.getRd(instruction);

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
        replaceNode(Rd,""+reg[Rd]);
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
                memory[addr >> 2] &= ~(0xFF << ((addr & 0x3) << 3));            // Same logic as LB instruction. We clear the byte at the word-address + offset.
                memory[addr >> 2] |= (reg[Rs2] & 0xFF) << ((addr & 0x3) << 3);  // Store byte from reg[Rs2] at word-address + offset
                break;
            case 0b001: // SH
                type = "SH";
                memory[addr >> 2] &= ~(0xFFFF << ((addr & 0x2) << 3));           // Same logic as LH instruction. We clear the half-word at the word-address + offset.
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


    // Outputs registers x0 to x31 to file "output.bin"
    private static void outToBin(int[] reg) throws IOException{
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();
    }

    // Initialize register GridPane
    private static void initGridReg() {
        // Define Text elements for all x0 to x31 values:
        val1 = new Text("0");
        val2 = new Text("0");
        val3 = new Text("0");
        val4 = new Text("0");
        val5 = new Text("0");
        val6 = new Text("0");
        val7 = new Text("0");
        val8 = new Text("0");
        val9 = new Text("0");
        val10 = new Text("0");
        val11 = new Text("0");
        val12 = new Text("0");
        val13 = new Text("0");
        val14 = new Text("0");
        val15 = new Text("0");
        val16 = new Text("0");
        val17 = new Text("0");
        val18 = new Text("0");
        val19 = new Text("0");
        val20 = new Text("0");
        val21 = new Text("0");
        val22 = new Text("0");
        val23 = new Text("0");
        val24 = new Text("0");
        val25 = new Text("0");
        val26 = new Text("0");
        val27 = new Text("0");
        val28 = new Text("0");
        val29 = new Text("0");
        val30 = new Text("0");
        val31 = new Text("0");

        // Setup register grid
        gridReg = new GridPane();
        gridReg.setPadding(new Insets(10,10,10,10));
        gridReg.setVgap(2);
        gridReg.setHgap(5);
        gridReg.setAlignment(Pos.CENTER);
        gridReg.setMinSize(250,200);

        // Column titles
        Text regHead = new Text("Register");
        Text valHead = new Text("Value");

        // Add Text to GridPane
        gridReg.add(regHead,0,0);
        gridReg.add(valHead,1,0);
        gridReg.add(val1,1,2);
        gridReg.add(val2,1,3);
        gridReg.add(val3,1,4);
        gridReg.add(val4,1,5);
        gridReg.add(val5,1,6);
        gridReg.add(val6,1,7);
        gridReg.add(val7,1,8);
        gridReg.add(val8,1,9);
        gridReg.add(val9,1,10);
        gridReg.add(val10,1,11);
        gridReg.add(val11,1,12);
        gridReg.add(val12,1,13);
        gridReg.add(val13,1,14);
        gridReg.add(val14,1,15);
        gridReg.add(val15,1,16);
        gridReg.add(val16,1,17);
        gridReg.add(val17,1,18);
        gridReg.add(val18,1,19);
        gridReg.add(val19,1,20);
        gridReg.add(val20,1,21);
        gridReg.add(val21,1,22);
        gridReg.add(val22,1,23);
        gridReg.add(val23,1,24);
        gridReg.add(val24,1,25);
        gridReg.add(val25,1,26);
        gridReg.add(val26,1,27);
        gridReg.add(val27,1,28);
        gridReg.add(val28,1,29);
        gridReg.add(val29,1,30);
        gridReg.add(val30,1,31);
        gridReg.add(val31,1,32);
        gridReg.add(new Text("0"),1,1); // x0 doesn't change

        // Write x0 to x31 in the Register column
        for (int i = 0; i < 32; i++) {
            gridReg.add(new Text("x"+i),0,i+1);
        }
    }

    public void start(Stage stage) throws Exception {
        window = new Stage();

        // Setup scene
        Scene scene = new Scene(gridReg);
        window.setTitle("RISC-V simulator");
        window.setScene(scene);
        window.show();
    }

    private static void replaceNode(int index, String text) {
        switch(index) {
            case 1:
                gridReg.getChildren().remove(val1);
                val1.setText(text);
                gridReg.add(val1,1,2);
                break;
            case 2:
                gridReg.getChildren().remove(val2);
                val2.setText(text);
                gridReg.add(val2,1,3);
                break;
            case 3:
                gridReg.getChildren().remove(val3);
                val3.setText(text);
                gridReg.add(val3,1,4);
                break;
            case 4:
                gridReg.getChildren().remove(val4);
                val4.setText(text);
                gridReg.add(val4,1,5);
                break;
            case 5:
                gridReg.getChildren().remove(val5);
                val5.setText(text);
                gridReg.add(val5,1,6);
                break;
            case 6:
                gridReg.getChildren().remove(val6);
                val6.setText(text);
                gridReg.add(val6,1,7);
                break;
            case 7:
                gridReg.getChildren().remove(val7);
                val7.setText(text);
                gridReg.add(val7,1,8);
                break;
            case 8:
                gridReg.getChildren().remove(val8);
                val8.setText(text);
                gridReg.add(val8,1,9);
                break;
            case 9:
                gridReg.getChildren().remove(val9);
                val9.setText(text);
                gridReg.add(val9,1,10);
                break;
            case 10:
                gridReg.getChildren().remove(val10);
                val10.setText(text);
                gridReg.add(val10,1,11);
                break;
            case 11:
                gridReg.getChildren().remove(val11);
                val11.setText(text);
                gridReg.add(val11,1,12);
                break;
            case 12:
                gridReg.getChildren().remove(val12);
                val12.setText(text);
                gridReg.add(val12,1,13);
                break;
            case 13:
                gridReg.getChildren().remove(val13);
                val13.setText(text);
                gridReg.add(val13,1,14);
                break;
            case 14:
                gridReg.getChildren().remove(val14);
                val14.setText(text);
                gridReg.add(val14,1,15);
                break;
            case 15:
                gridReg.getChildren().remove(val15);
                val15.setText(text);
                gridReg.add(val15,1,16);
                break;
            case 16:
                gridReg.getChildren().remove(val16);
                val16.setText(text);
                gridReg.add(val16,1,17);
                break;
            case 17:
                gridReg.getChildren().remove(val17);
                val17.setText(text);
                gridReg.add(val17,1,18);
                break;
            case 18:
                gridReg.getChildren().remove(val18);
                val18.setText(text);
                gridReg.add(val18,1,19);
                break;
            case 19:
                gridReg.getChildren().remove(val19);
                val19.setText(text);
                gridReg.add(val19,1,20);
                break;
            case 20:
                gridReg.getChildren().remove(val20);
                val20.setText(text);
                gridReg.add(val20,1,21);
                break;
            case 21:
                gridReg.getChildren().remove(val21);
                val21.setText(text);
                gridReg.add(val21,1,22);
                break;
            case 22:
                gridReg.getChildren().remove(val22);
                val22.setText(text);
                gridReg.add(val22,1,23);
                break;
            case 23:
                gridReg.getChildren().remove(val23);
                val23.setText(text);
                gridReg.add(val23,1,24);
                break;
            case 24:
                gridReg.getChildren().remove(val24);
                val24.setText(text);
                gridReg.add(val24,1,25);
                break;
            case 25:
                gridReg.getChildren().remove(val5);
                val25.setText(text);
                gridReg.add(val25,1,26);
                break;
            case 26:
                gridReg.getChildren().remove(val26);
                val26.setText(text);
                gridReg.add(val26,1,27);
                break;
            case 27:
                gridReg.getChildren().remove(val27);
                val27.setText(text);
                gridReg.add(val27,1,28);
                break;
            case 28:
                gridReg.getChildren().remove(val28);
                val28.setText(text);
                gridReg.add(val28,1,29);
                break;
            case 29:
                gridReg.getChildren().remove(val29);
                val29.setText(text);
                gridReg.add(val29,1,30);
                break;
            case 30:
                gridReg.getChildren().remove(val30);
                val30.setText(text);
                gridReg.add(val30,1,31);
                break;
            case 31:
                gridReg.getChildren().remove(val31);
                val31.setText(text);
                gridReg.add(val31,1,32);
                break;
            default:
                break;
        }
    }
}
