/*
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 */
import java.io.*;

public class RISCVsimulator {
    static int pc;

    public static void main(String[] args) throws IOException {
        instHelper inst = new instHelper();
        pc = 0;
        // Start reading from binary file
        String path = "addlarge.bin";
        int[] program = getInstructions(path);

        /*for(int i = 0; i < program.length; i++){
            System.out.println(inst.getOpcode(program[i]));
            System.out.println(inst.getFunct3(program[i]));
            System.out.println(inst.getFunct7(program[i]));
        }*/
    }

    private static int addi() {
        return 0;
    }

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

}
