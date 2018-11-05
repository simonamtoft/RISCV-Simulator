import java.io.*;

public class RISCVsimulator {

    static int pc;

    public static void main(String[] args) throws IOException {
        pc = 0;

        // Start reading from binary file
        String path = "addlarge.bin";
        File inFile = new File(path);
        DataInputStream bitstream = new DataInputStream(new FileInputStream(inFile));

        // Input instructions in array from binary file
        int[] instruction = new int[(int) inFile.length()/4];
        for (int i = 0; i < instruction.length; i++) {
            instruction[i] = (Integer.reverseBytes(bitstream.readInt())); // Bitstream is opposite order than RISC-V shows instructions.
        }
        bitstream.close();

        System.out.println(String.format("0x%08X",instruction[0]));
    }

    private static int addi() {
        return 0;
    }

}
