package fileSystems;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class dir {
	
	public static int dir_create(String pathName) throws IOException{
		//If nothings mounted or we don't have absolute path, return -1
		if (pathName.length() == 0) return -1;
		if (pathName.charAt(0) != '/') return -1;
		
		
		//Split all the path names, if we don't have any '/' or an actual path, return -1
		String[] paths = pathName.split("/");
		if (paths.length <= 1) return -1;
		String name = paths[paths.length - 1];
		
		
		//Lets start at the first inode( root directory ) and start finding inodes
		utils.reader.seek(512);
		inode direct = find_inode(Arrays.copyOfRange(paths, 0, paths.length - 1), utils.root);
		if (direct == null) return -1;
		//If the directory is full, we can't add anything to it
		if (direct.size == 26*512) return -1;
		if (direct.dir_contains(name)) return -1;
		
		//Lets find out where our new inode and datablock will go!
		int new_inode_index = -1;
		int new_datablock_index = -1;
		
		utils.reader.seek(16);
		byte next;
		do{
			next = utils.reader.readByte();
			new_inode_index++;
			if (new_inode_index > 144) return -1;
		} while (next != 0);
		if (new_inode_index == -1) return -1;
				
				
		utils.reader.seek(160);
		
		do {
			next = utils.reader.readByte();
			new_datablock_index++;
			if (new_datablock_index > 246) return -1;

		} while (next != 0);
		
		if (new_datablock_index == -1) return -1;
		
		
		//At this point we can assume we'll end up creating that inode and data block (update superblock)
		utils.reader.seek(16 + new_inode_index);
		utils.reader.writeByte(1);
		utils.reader.seek(160 + new_datablock_index);
		utils.reader.writeByte(1);
		int spot = direct.free_block();

		if (!direct.update_size()) return -1;
		if (spot == -1) return -1;
		

		
		if (name.length() > 8) return -1;
		
		int bytes_written = name.length() * 2;
		utils.reader.seek(spot);
		byte[] a = name.getBytes();
		for (int i = 0; i < a.length; i++){
			utils.reader.writeChar(a[i]);
		}
		//utils.reader.writeChars(name);
		while (bytes_written < 16){
			utils.reader.writeByte(0);
			bytes_written++;
		}
		utils.reader.writeInt(new_inode_index);
		
		utils.reader.seek(inode_offset(new_inode_index));
		utils.reader.write(DatatypeConverter.parseHexBinary("1111"));
		utils.reader.write(DatatypeConverter.parseHexBinary("3333"));
		utils.reader.writeShort(0);
		utils.reader.writeByte(new_datablock_index + 10);
		for (int i = 0; i < 25; i++){
			utils.reader.writeByte(0);
		}
		
		
		return 0;
		
	}
	
	public static int dir_unlink(String pathName) throws IOException{		
		if (pathName.equals("/")) return -1;
		String[] paths = pathName.split("/");
		
		inode current = find_inode(Arrays.copyOfRange(paths, 0, paths.length - 1), utils.root);
		inode actual = find_inode(paths, utils.root);
		if (actual.size != 0) return -1;
		if (!actual.type.equals("3333")) return -1;
		
		String fileName = paths[paths.length - 1];
		int i = 0;
		boolean found = false;
		int inode_index = 0;
		while (i < 26 && current.pointers[i] != 0 && !found){
			int start = 0;
			utils.reader.seek(inode.pointer_to_block(current.pointers[i]));
			while (start < 500){
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
				if (name.equals(fileName)){
					inode_index = utils.reader.readInt();
					utils.reader.seek(utils.reader.getFilePointer() - 20);
					for (int l = 0; l < 20; l++){
						utils.reader.writeByte(0);
					}
					found = true;
					break;	
				}
				utils.reader.seek(utils.reader.getFilePointer() + 4);
				start += 20;
			}
			i++;
		}
		if (!found) return -1;
		

		utils.reader.seek(inode_offset(inode_index));
		for (int k = 0; k < 32; k++){
			utils.reader.writeByte(0);
		}
		
		utils.reader.seek(inode_index + 16);
		utils.reader.writeByte(0);
		
		current.update_size(current.size - 20);
		return 0;
	}
	
	public static int dir_read(String pathName, String[] buffer, int size) throws IOException{
		if (!utils.mounted) return -1;
		String[] paths = pathName.split("/");
		inode current = find_inode(Arrays.copyOfRange(paths, 0, paths.length), utils.root);
		//if (current.size/20 < size) return -1;

		int buf = 0;
		
		int i = 0;
		while (i < 26 && current.pointers[i] != 0){
			int start = 0;
			utils.reader.seek(inode.pointer_to_block(current.pointers[i]));
			while (start < 500){
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
				if (!name.equals("")){
					int inode_index;
					inode_index = utils.reader.readInt();
					buffer[buf] = name + " " + inode_index;
					buf++;
					name = "";
				}else{
				utils.reader.seek(utils.reader.getFilePointer() + 4);
				}
				start += 20;
			}
			i++;
		}
		
		
		return current.size / 20;
		
		
	}
	
	//GETS THE INODE THAT CONTAINS WHAT YOU'RE LOOKING FOR
	public static inode find_inode(String[] paths, inode node) throws IOException{
		if (paths.length < 2){
			return node;
		}
		else{
			String looking_for = paths[1];
			if (looking_for.length() > 16) return null;
			if (!node.type.equals("3333")) return null;
			int i = 0;
			while (node.pointers[i] != 0){
				utils.reader.seek(inode.pointer_to_block(node.pointers[i]));
				int j = 0;
				while (j < 510){
					char[] str = new char[8];
					for (int r = 0; r < str.length; r++){
						str[r] += utils.reader.readChar();
					}
					String name = "";
					for (int k = 0; k < str.length; k++){
						if (str[k] != '\0'){
							name += str[k];
						}
					}
					
					if (name.equals(looking_for)){
						int next = utils.reader.readInt();
						utils.reader.seek(inode_offset(next));
						inode now = new inode(looking_for);
						return find_inode(Arrays.copyOfRange(paths, 1, paths.length), now);
					}
					utils.reader.seek(utils.reader.getFilePointer() + 4);
					j += 20;
				}
				
				i++;
			}
			
			
		}
		return null;
		
	}
	
	
	
	
	
	public static int inode_offset(int inode_number){
		return 512 + (inode_number * 32);
	}
	
	public static int datablock_offset(int datablock_number){
		return 512 * (datablock_number + 10);
	}
	
}
