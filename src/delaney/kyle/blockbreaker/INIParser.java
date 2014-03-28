package delaney.kyle.blockbreaker;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class INIParser {
	public final void load(InputStream in) {
		beforeLoad();
		Scanner sc = new Scanner(in);
		String line;
		
		try {
			while(true) {
				line = sc.nextLine().trim();
				
				while(line.endsWith("\\")) {
					line = line.substring(0, line.length()-1) + sc.nextLine();
				}
				
				if(line.startsWith("[")) {
					onGroup(line.substring(1, line.indexOf("]")));
					continue;
				}
				
				String[] tokens = line.split("[\\s=:]+");
				
				String key = null;
				
				for(String token : tokens) {
					if(key == null) {
						key = token;
						continue;
					}
					
					try {
						int i = Integer.parseInt(token);
						onInt(i);
						continue;
					} catch(NumberFormatException e) {}
					
					onString(token);
				}
			}
		} catch(NoSuchElementException e) {}
		afterLoad();
	}
	
	protected void onGroup(String s) {}
	protected void onInt(int i) {}
	protected void onString(String s) {}
	
	protected void beforeLoad() {}
	protected void afterLoad() {}
}
