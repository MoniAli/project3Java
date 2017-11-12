package fileSystems;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class file {
	
	public static int file_create(String pathName) throws IOException{
		
		//Little finesse, it's the same as creating a directory but instead we write "2222",
		//So lets just make the directory, the overwrite the inode's "3333" with "2222"
		if (dir.dir_create(pathName) == -1) return -1;
		utils.reader.seek(utils.reader.getFilePointer() - 30);
		utils.reader.write(DatatypeConverter.parseHexBinary("2222"));
		utils.reader.seek(utils.reader.getFilePointer() - 4);
		return 0;
	}
	
	public static int file_unlink(String pathName) throws IOException{
		String[] paths = pathName.split("/");
		String fileName = paths[paths.length - 1];
		
		inode current = dir.find_inode(Arrays.copyOfRange(paths, 0, paths.length - 1), utils.root);
		
		inode actual = dir.find_inode(paths, utils.root);
		if (!actual.type.equals("2222")) return -1;
		current.print();
		int i = 0;
		int inode_index = 0;
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
				if (name.equals(fileName)){
					inode_index = utils.reader.readInt();
					utils.reader.seek(utils.reader.getFilePointer() - 20);
					for (int v = 0; v < 20; v++){
					utils.reader.writeByte(0);
					}
					utils.reader.seek(16 + inode_index);
					utils.reader.writeByte(0);
					
					break;
				}
				utils.reader.seek(utils.reader.getFilePointer() + 4);
				start += 20;
			}
			i++;
		}
		
		
		return 0;
	}
	
	public static int file_write(int fd, byte[] buffer, int write_amount) throws IOException{
		if (!utils.ft.already_open(fd)) return -1;
		if (fd > 32 || fd < 0) return -1;
		inode current = utils.ft.get_inode(fd);
		
		int counter = 0;
		for (int i = 0; i < write_amount; i++){
			counter += current.write_byte(buffer[i]);
		}
		current.update_size(counter);
		if (counter + 1 == write_amount) return write_amount;
		return -1;
		
	}
	
	public static int file_read(int fd, byte[] buffer, int read_amount) throws IOException{
		if (!utils.ft.already_open(fd)) return -1;
		if (fd > 32 || fd < 0) return -1;
		inode current = utils.ft.get_inode(fd);
		
		for (int i = 0; i < read_amount; i++){
			byte b = current.get_byte();
			buffer[i] = b;
			System.out.println("WE JUST READ " +  b);
		}
		
		return 0;
		
	}
	
	public static int file_seek(int fd, int offset){
		if (!utils.ft.already_open(fd)) return -1;
		if (fd > 32 || fd < 0) return -1;
		inode current = utils.ft.get_inode(fd);
		
		if (offset > current.size) return -1;
		
		current.fp = (offset % 512);
		current.cur_block = (offset / 512);
		
		return offset;
		
	}
	
	public static int file_open(String pathName) throws IOException{
		if (pathName.equals("/")) return -1;
		String paths[] = pathName.split("/");
		if (paths.length == 0) return -1;
		inode f = dir.find_inode(paths, utils.root);
		if (f == null) return -1;
		
		return utils.ft.openFile(f);
	}
	
	
	public static int file_close(int fd) throws IOException{
		int result = utils.ft.closeFile(fd);
		return result;
	}

}
