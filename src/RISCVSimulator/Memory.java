/* File: Main.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file has a memory byte array and handles input and read from the array
 */
package RISCVSimulator;
import java.nio.charset.StandardCharsets;

public class Memory {
    private byte[] memory;

    public Memory(int byteSize){
        memory = new byte[byteSize];
    }

    public void storeByte (int addr, byte data) {
        memory[addr] = data;
    }

    public void storeHalfWord(int addr, short data) {
        memory[addr]    = (byte) (data & 0x00FF);
        memory[addr+1]  = (byte) (data &0xFF00);
    }

    public void storeWord(int addr, int data) {
        memory[addr]    = (byte) (data & 0x000000FF);
        memory[addr+1]  = (byte) (data & 0x0000FF00);
        memory[addr+2]  = (byte) (data & 0x00FF0000);
        memory[addr+3]  = (byte) (data & 0xFF000000);
    }
    
    public void storeString(int addr, String data){
        byte[] str = data.getBytes(StandardCharsets.US_ASCII);
        for(int i = 0; i < str.length; i++){
            memory[addr+i] = str[i];
        }
    }

    public byte getByte (int addr) {
        return memory[addr];
    }

    public short getHalfWord(int addr){
        return (short) ((memory[addr+1] << 8) | (memory[addr]));
    }

    public int getWord(int addr){
        return ((memory[addr+3] << 24) | (memory[addr+2] << 16) | (memory[addr+1] << 8) | (memory[addr]));
    }

    public String getString(int addr){
        String returnValue = "";
        int i = 0;
        while(memory[addr+i] != 0){
            returnValue += (char) (memory[addr+i]);
        }
        return returnValue;
    }

    public byte[] getArray() {
        return memory;
    }

}
