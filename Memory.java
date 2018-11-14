package RISCVSimulator;

public class Memory {
    private byte[] memory;

    public Memory(int MEMORY_SIZE_IN_BYTES){
        this.memory = new byte[MEMORY_SIZE_IN_BYTES];
    }

    public void store_byte_to_mem(int addr, byte data){
        memory[addr] = data;
    }

    public void store_halfword_to_mem(int addr, short data){
        memory[addr]    = (byte) (data & 0x00FF);
        memory[addr+1]  = (byte) (data &0xFF00);
    }

    public void store_word_to_mem(int addr, int data){
        memory[addr]    = (byte) (data & 0x000000FF);
        memory[addr+1]  = (byte) (data & 0x0000FF00);
        memory[addr+2]  = (byte) (data & 0x00FF0000);
        memory[addr+3]  = (byte) (data & 0xFF000000);
    }

    public byte get_byte_from_mem(int addr){
        return memory[addr];
    }

    public short get_halfword_from_mem(int addr){
        return (short) ((memory[addr+1] << 8) | (memory[addr]));
    }

    public int get_word_from_mem(int addr){
        return ((memory[addr+3] << 8) | (memory[addr+2] << 16) | (memory[addr+1] << 8) | (memory[addr]));
    }

    public String get_string_from_mem(int addr){
        String returnValue = "";
        int i = 0;
        while(memory[addr+i] != 0){
            returnValue += (char) (memory[addr+i]);
        }
        return returnValue;
    }

    public byte[] getMemory() {
        return memory;
    }
}
