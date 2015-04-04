package haven;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translate {
    static public String get(String a) {
	if (Config.translate)
	    return instance.translate(a);
	return a;
    }
    static Translate instance = new Translate();
    HashMap<String, String> trans = new HashMap<String, String>();
    HashSet<String> later = new HashSet<String>();
    PrintWriter late = null;
    public Translate () {
	load();
    }
    public void load() {
	trans.clear();
	later.clear();
	try {
	    BufferedReader r = new BufferedReader(new FileReader("trans.txt"));
	    while (true) {
		String a = r.readLine();
		String b = r.readLine();
		if (b == null)
		    break;
		trans.put(a, b);
	    }
	    r.close();
	    late = new PrintWriter(new FileWriter("late.txt", false));
	    r = new BufferedReader(new FileReader("later.txt"));
	    while (true) {
		String a = r.readLine();
		if (a == null)
		    break;
		later.add(a);
	    }
	    r.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    static final Pattern patt = Pattern.compile("[a-zA-Z][a-zA-Z '&-,]*[a-zA-Z]");
    public String translate(String str) {
	if (str.length() <= 1)
	    return str;
	StringBuffer sb = new StringBuffer();
	Matcher m = patt.matcher(str);
	while (m.find()) {
	    String a = m.group();
	    if (a.length() <= 1)
		continue;
	    String b = trans.get(a);
	    if (b == null) {
		if (!later.contains(a)) {
		    later.add(a);
		    late.println(a);
		    late.flush();
		}
		b = a;
	    }
	    m.appendReplacement(sb, b);
	}
	m.appendTail(sb);
	return sb.toString(); 
    }
    
}
