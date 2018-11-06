/*
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 */
import java.io.*;

public class RISCVsimulator {
    static int pc;

    public static void main(String[] args) throws IOException {
        instField inst = new instField();
        pc = 0;
        // Start reading from binary file
        String path = "addlarge.bin";
        int[] program = getInstructions(path);
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



}
