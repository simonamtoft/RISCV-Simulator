/* File: Main.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file has a memory byte array and handles input and read from the array
 */
package RISCVSimulator;

import java.nio.charset.StandardCharsets;

class Memory {
    private byte[] memory;

    Memory(int MEMORY_SIZE_IN_BYTES){
        memory = new byte[MEMORY_SIZE_IN_BYTES];
    }

    void storeByte(int addr, int data){
        memory[addr] = (byte) (data & 0xFF);
    }

    void storeHalfword(int addr, int data){
        memory[addr]    = (byte) ((data & 0x00FF));
        memory[addr+1]  = (byte) ((data & 0xFF00) >>> 8);
    }

    void storeWord(int addr, int data){
        memory[addr]    = (byte) ((data & 0x000000FF));
        memory[addr+1]  = (byte) ((data & 0x0000FF00) >>> 8);
        memory[addr+2]  = (byte) ((data & 0x00FF0000) >>> 16);
        memory[addr+3]  = (byte) ((data & 0xFF000000) >>> 24);
    }

    public void storeString(int addr, String data){
        byte[] str = data.getBytes(StandardCharsets.US_ASCII);
        for(int i = 0; i < str.length; i++){
            memory[addr+i] = str[i];
        }
    }

    int getByte(int addr){
        return memory[addr];
    }

    int getHalfword(int addr){
        return (getByte(addr+1) << 8) | (getByte(addr) & 0xFF);
    }

    int getWord(int addr){
        return (getHalfword(addr+2) << 16) | (getHalfword(addr) & 0xFFFF);
    }

    public String getString(int addr){
        String returnValue = "";
        int i = 0;
        while(memory[addr+i] != 0){
            returnValue += (char) (memory[addr+i]);
        }
        return returnValue;
    }

    byte[] getMemory() {
        return memory;
    }
}
