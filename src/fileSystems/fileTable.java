package fileSystems;

import java.io.IOException;

public class fileTable {
	
	inode[] entries;
	
	private fileTable(){
		entries = new inode[32];
	}
	
	public static fileTable getInstance(){
		if (utils.ft == null){
			return new fileTable();
		}
		return utils.ft;
	}
	
	public int openFile(inode node) throws IOException{
		if (!node.type.equals("2222")) return -1;
		utils.reader.seek(node.file_start);
		int index = next_open();
		if (index == -1) return -1;
		if (already_open(node.name)) return -1;
		entries[index] = node;
		print_open_files();
		return index;
	}
	
	public int closeFile(int fd){
		if (fd >= entries.length || fd <= -1) return -1;
		if (entries[fd] != null) {
			entries[fd] = null;
			return 0;
		}
		return -1;
	}
	
	private void print_open_files(){
		for (int i = 0; i < entries.length; i++){
			if (entries[i] != null){
				entries[i].print();
			}
		}
	}
	
	public boolean already_open(String name){
		for (int i = 0; i < entries.length; i++){
			if (entries[i] != null){
				if (entries[i].name.equals(name)) return true;
			}
		}
		return false;
	}
	
	public inode get_inode(int fd){
		return entries[fd];
	}
	
	public boolean already_open(int fd) {
		return (entries[fd] != null);
	}
	
	private int next_open(){
		int index = -1;
		for (int i = 0; i < entries.length; i++){
			if (entries[i] == null)
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	
}
