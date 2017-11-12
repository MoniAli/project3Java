package fileSystems;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Runner {

	static Scanner in = new Scanner(System.in);
	static final String f_success = "The file successfully formatted";
	static final String f_fail = "The file failed to format";
	static final String m_success = "The file successfully mounted";
	static final String m_fail = "The file failed to mount";
	static final String d_success = "The directory was successfully created";
	static final String d_fail = "The directory failed to be created";
	static final String remove_fail = "The directory failed to be removed";
	static final String remove_s = "The directory was successfully removed";
	static final String file_m_success = "The file was successfully made";
	static final String file_m_fail = "The file failed to be made";
	static final String file_r_success = "The file was successfully removed";
	static final String file_r_fail = "The file failed to be removed";
	
	public static void main(String[] args) {
		int selection;
		String fileName;
		while(true){
		
			selection = menu();
			switch(selection){
			case 1:
				System.out.println("What file would you like to format?");
				String f_result;
				String format_file;
				
				format_file = in.nextLine();
				try{
				utils.format(format_file);
				f_result = f_success;
				}
				catch (Exception e){
					f_result = f_fail;
				}
				System.out.println(f_result);
				break;
			case 2:
				System.out.println("What file would you like to mount?");
				String m_result;
				fileName = in.nextLine();
				try{
					m_result = (utils.mount(fileName) == 0) ? m_success : m_fail;
				}
				catch(Exception e){
					m_result = m_fail;
				}
				System.out.println(m_result);
				break;
			case 3:	
				if (!utils.mounted){
					System.out.println(d_fail);
					break;
				}
				String d_result;
				try {
					System.out.println("What is the file path");
					String name = in.nextLine();
					d_result = (dir.dir_create(name) == 0) ? d_success : d_fail;
				} catch (IOException e) {
					d_result = d_fail;
					e.printStackTrace();
				}
				System.out.println(d_result);
				break;
			case 4:
				if (!utils.mounted){
					System.out.println(remove_fail);
					break;
				}
				System.out.println("What directory would you like to remove?");
				String name = in.nextLine();
				try {
					System.out.println(dir.dir_unlink(name));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5:
				System.out.println("What is the directory you'd like to list the contents of?");
				String pathName = in.nextLine();
				String[] buffer = new String[100];
				int size = 100;
				try {
					if (dir.dir_read(pathName, buffer, size) == -1){
						System.out.println("The directory contents failed to be found");
					}
				} catch (IOException e) {
					System.out.println("The directory contents failed to be found");
				}
				for (int i = 0; i < size; i++)
				{
					if (buffer[i] != null){
						System.out.println(buffer[i]);
					}
;				}
				break;
			case 6:
				System.out.println("What file would you like to create?");
				String file_to_make = in.nextLine();
				String file_m_result;
				try {
					file_m_result = (file.file_create(file_to_make) == 0) ? file_m_success : file_m_fail;
				} catch (IOException e) {
					file_m_result = file_m_fail;
				}
				System.out.println(file_m_result);
				break;
			case 7:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				System.out.println("What file would you like to delete?");
				String file_to_remove = in.nextLine();
				String file_r_result;
				try {
					file_r_result = (file.file_unlink(file_to_remove) == 0) ? file_r_success : file_r_fail;
				} catch (IOException e) {
					file_r_result = file_r_fail;
				}
				System.out.println(file_r_result);
				break;
			case 8:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				System.out.println("What file would you like to open?");
				String filePath = in.nextLine();
				try {
					int result = file.file_open(filePath);
					if (result == -1) System.out.println("The file failed to open");
					else System.out.println("The file opened with file descripter "  + result);
				} catch (IOException e) {
					System.out.println("The file failed to open");
				}
				break;
			case 9:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				int descriptor = -1;
				do{
					System.out.println("What file would you like to read from?");
					String file_to_read = in.nextLine();
					try{
						descriptor = Integer.parseInt(file_to_read);
						break;
					}
					catch (NumberFormatException e){
						System.out.println("Please try again, or enter -1 if you would no longer like to read a file");
					}
				}while(true);
				System.out.println("Where would you like that to end up?");
				String f_name = in.nextLine();
				int toRead = -1;
				do{
					System.out.println("How many bytes would you like to read?");
					String bytes_to_read = in.nextLine();
					try{
						toRead = Integer.parseInt(bytes_to_read);
						if (toRead > -1){
							break;
						}
					}
					catch (NumberFormatException e){
						System.out.println("Please try again");
					}
				}while(true);
				byte[] buf = new byte[toRead];
				try {
					if (file.file_read(descriptor, buf, toRead) == -1) System.out.println("Failed to read");
					RandomAccessFile fd = new RandomAccessFile(f_name, "rw");
					fd.write(buf);
					fd.close();
				} catch (IOException e1) {
					System.out.println("Failed to read file");
				}
				
				break;
			case 10:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				int file_d = -1;
				do{
					System.out.println("What file would you like to write to?");
					String file_to_write = in.nextLine();
					try{
						file_d = Integer.parseInt(file_to_write);
						break;
					}
					catch (NumberFormatException e){
						System.out.println("Please try again, or enter -1 if you would no longer like to write a file");
					}
				}while(true);
				System.out.println("Where would you like that to come from?");
				String w_name = in.nextLine();
				int amount = -1;
				do{
					System.out.println("How many bytes would you like to write?");
					String prompt_amount = in.nextLine();
					try{
						amount = Integer.parseInt(prompt_amount);
						if (amount > -1){
							break;
						}
					}
					catch (NumberFormatException e){
						System.out.println("Please try again");
					}
				}while(true);
				byte[] input = new byte[amount];
				try {
					
					
					RandomAccessFile fd = new RandomAccessFile(w_name, "rw");
					fd.read(input, 0, amount);
					fd.close();
					for (int i = 0; i < input.length; i++){
						System.out.println(input[i]);
					}
					file.file_write(file_d, input, amount);
				} catch (IOException e1) {
					System.out.println("Failed to read file");
				}
				
				break;
			case 11:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				int f = -1;
				do{
					System.out.println("What file would you like to seek in?");
					String file_to_seek = in.nextLine();
					try{
						f = Integer.parseInt(file_to_seek);
						break;
					}
					catch (NumberFormatException e){
						System.out.println("Please try again, or enter -1 if you would no longer like to seek in a file");
					}
				}while(true);
				
				int loc = -1;
				do {
					System.out.println("Where would you like to seek to?");
					String spot_str = in.nextLine();
					try{
						loc = Integer.parseInt(spot_str);
						break;
					}
					catch (NumberFormatException e){
						System.out.println("Please try again, or enter -1 if you would no longer like to seek");
					}
				}while (true);
				
				if (f != -1 && loc != -1){
					if (file.file_seek(f, loc) == -1){
						System.out.println("The file failed to properly seek");
					}
					else{
						System.out.println("The file successfully seeked to the location");
					}
				}
				
				break;
			case 12:
				if (!utils.mounted) {
					System.out.println("You need to mount a file system first");
					break;
				}
				int fd = -1;
				do{
					System.out.println("What file would you like to close?");
					String file_to_close = in.nextLine();
					try{
						fd = Integer.parseInt(file_to_close);
						break;
					}
					catch (NumberFormatException e){
						System.out.println("Please try again, or enter -1 if you would no longer like to close a file");
					}
				}while(true);
				
				try {
					file.file_close(fd);
				} catch (IOException e) {
					System.out.println("The file failed to close");
				}
				break;
			case 14:
				try {
					utils.reader.close();
				} catch (Exception e) {
				}
				return;
			default:
				break;
			}
			
		}
	}
	
	public static int menu(){
		String input;
		int converted;
		while (true){
			System.out.println("Menu of options:");
			System.out.println("    1) Format a file system");
			System.out.println("    2) Mount a file system");
			System.out.println("    3) Create a directory");
			System.out.println("    4) Remove a directory");
			System.out.println("    5) List the contents of a directory");
			System.out.println("    6) Create a file");
			System.out.println("    7) Remove a file");
			System.out.println("    8) Open a file");
			System.out.println("    9) Read from a file");
			System.out.println("   10) Write to a file");
			System.out.println("   11) Seek to a location in a file");
			System.out.println("   12) Close a file");
			System.out.println("   14) Exit the program");
			input = in.nextLine();
			try{
			converted = Integer.parseInt(input);
			if (converted > 0 && converted <= 14) break;
			else{ System.out.println("Invalid input, please try again");}
			}
			catch(NumberFormatException e){
				System.out.println("Invalid input, please try again");
			}
		}
		return converted;
	}

}
