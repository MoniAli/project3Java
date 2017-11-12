package fileSystems;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.xml.bind.DatatypeConverter;


public class utils {
	
	static public boolean mounted = false;
	static public fileTable ft;
	static public RandomAccessFile reader;
	static public inode root = null;
	
	public static int format(String fileName) throws IOException{
		RandomAccessFile writer;

		writer = new RandomAccessFile(fileName, "rw");

		byte magic[] = DatatypeConverter.parseHexBinary("DEADBEEF");
		
		writer.write(magic);
		writer.writeInt(1);
		writer.writeInt(9);
		writer.writeInt(246);
		
		
		writer.writeByte(1);
		for (int i = 0; i < 143; i++){
			writer.writeByte(0);
		}
		
		writer.writeByte(1);
		
		for (int i = 0; i < 245; i++){
			writer.writeByte(0);
		}

		for (int i = 0; i < 106; i++){
			writer.writeByte(0);
		}

		writer.write(DatatypeConverter.parseHexBinary("1111"));
		writer.write(DatatypeConverter.parseHexBinary("3333"));
		writer.writeShort(0);
		
		writer.writeByte(10);
		for (int i = 0; i < 25; i++){
			writer.writeByte(0);
		}
		
		for (int i = 0; i < 143; i++){
			for (int j = 0; j < 32; j++){
				writer.writeByte(0);
			}
		}
		
		for (int i = 0; i < 246; i++){
			for (int j = 0; j < 512; j++){
				writer.writeByte(0);
			}
		}
		
		writer.close();
		return 0;

	}
	
	public static int mount(String fileName) throws IOException{
		if (mounted) return -1;
		if (!new File(fileName).exists()) return -1;
		reader = new RandomAccessFile(fileName, "rw");
		
		int magic_int = reader.readInt();
		String magic_str = "deadbeef";
		String file_str;
		file_str = Integer.toHexString(magic_int);
		
		if (!file_str.equals(magic_str)){
			reader.close();
			return -1;
		}
		
		mounted = true;
		ft = fileTable.getInstance();
		utils.reader.seek(512);
		root = new inode("root");
		
		return 0;
		
	}
	
}
