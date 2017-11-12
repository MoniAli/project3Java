package fileSystems;

import java.io.IOException;
import java.util.ArrayList;

public class inode {

	int file_start;
	int index;
	int size;
	String type;
	byte[] pointers = new byte[26];
	int fp;
	int cur_block = 0;
	String name;
	
	public inode(String name) throws IOException{
		this.name = name;
		file_start = (int) utils.reader.getFilePointer();
		utils.reader.readShort();
		type = Integer.toHexString(utils.reader.readShort());
		size = utils.reader.readShort();
		for (int i = 0; i < pointers.length; i++){
			pointers[i] = utils.reader.readByte();
		}
		index = get_index(file_start);
		fp = 0;
	}
	
	public boolean update_size(){
		try {
			utils.reader.seek(file_start + 4);
			utils.reader.writeShort(size + 20);
			return true;
		} catch (IOException e) {
			return false;
		}	
	}
	
	public byte get_byte() throws IOException{
		if (fp == (pointers[cur_block] * 512)){
			if (pointers[cur_block + 1] == 0) return '\0';
			fp = 0;
			cur_block += 1;
		}
		if (cur_block == 26) return '\0';
		
		utils.reader.seek((pointers[cur_block] * 512) + fp);
		fp++;
		System.out.println(utils.reader.getFilePointer());
		return utils.reader.readByte();
	}
	
	
	public void print(){
		System.out.println(name);
		System.out.println(file_start);
		System.out.println(index);
		System.out.println(size);
		System.out.println(type);
		for (int i = 0; i < pointers.length; i++){
		System.out.print(pointers[i]);
		}
		System.out.println();
	}
	
	public int write_byte(byte b) throws IOException{
		if (cur_block == 25 && fp == 512) return 0;
		if (fp == 512){
			fp = 0;
			cur_block += 1;
			utils.reader.seek(160);
			while (utils.reader.readByte() != 0){
			}
			utils.reader.seek(utils.reader.getFilePointer() - 1);
			if (utils.reader.getFilePointer() == 406) return -1;
			utils.reader.writeByte(1);
			pointers[cur_block] = (byte) ((utils.reader.getFilePointer()/512) - 1);
			utils.reader.seek(index + 6 + cur_block);
			utils.reader.writeByte(pointers[cur_block]);
		}
		utils.reader.seek(pointers[cur_block] * 512 + fp);
		utils.reader.writeByte(b);
		fp += 1;
		return 1;
		
		
	}
	
	public void update_size(int size){
		this.size += size;
		this.update_size();
	}
	
	public boolean dir_contains(String fileName) throws IOException{
		int i = 0;
		while (i < 26 && pointers[i] != 0){
			int start = 0;
			utils.reader.seek(pointer_to_block(pointers[i]));
			while (start < 510){
				char[] str = new char[8];
				for (int j = 0; j < str.length; j++){
					str[j] += utils.reader.readChar();
				}
				String name = "";
				for (int k = 0; k < str.length; k++){
					if (str[k] != '\0'){
						name += str[k];
					}
				}
				if (name.equals(fileName)) return true;
				utils.reader.seek(utils.reader.getFilePointer() + 4);
				start += 20;
			}
			i++;
		}
		return false;
	}
	
	public int free_block() throws IOException{
		int i = 0; 
		while (pointers[i] != 0 && i < 26){
			utils.reader.seek(pointer_to_block(pointers[i]));
			for (int reads = 0; reads < 25; reads++){
				if (utils.reader.readChar() == '\0'){
					return (int)utils.reader.getFilePointer() - 2;
				}
				else{
					utils.reader.seek(utils.reader.getFilePointer() + 18);
				}
			}
			i++;
		}
		
		return -1;
		
		
	}
	
	public int get_index(int start){
		return (start - 512) / 32;
	}
	
	public boolean has_free_pointer(){
		for (int i = 0; i < pointers.length; i++){
			if (pointers[i] == 0){
				return true;
			}
		}
		return false;
	}
	public static int pointer_to_block(byte pointer){
		return pointer * 512;
	}
	
}
