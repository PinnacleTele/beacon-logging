package demo;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.UUID;

public class Test {

	static String format="yyyy-MM-dd HH:mm:ss.SSS";
	
	static String recv_time=" 2024-02-07 10:56:50.673";
	static String cs="2024-02-07 10:57:05.557";
	public static void main(String[] args) throws ParseException, UnsupportedEncodingException {
		/*
		System.out.println("Hai");
		
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);

       System.out.println(recv_time); 

       System.out.println(sdf.parse(recv_time).getTime()); ;

       System.out.println(sdf.format(new Date(sdf.parse(recv_time).getTime()))); ;
       
       
       System.out.println(cs); 

       System.out.println(sdf.parse(cs).getTime()); ;

       System.out.println(sdf.format(new Date(sdf.parse(cs).getTime()))); 
       
       System.out.println(sdf.parse(cs).getTime()-sdf.parse(recv_time).getTime()); ;

       
       day(null);
		
		System.out.println(URLEncoder.encode(" DN:'id:2170216805068145032000 sub:001 dlvrd:001 submit date:240208000506 done date:240208000841 stat:UNDELIV err:699 Text:OLELEARN \r\n'","UTF-8"));
     
		String st="\0";
		System.out.println(URLEncoder.encode(st,"UTF-8"));

		st=st.replaceAll("\0", "The");
		System.out.println(URLEncoder.encode(st,"UTF-8"));
		System.out.println(st);
*/
	    UUID uuid = UUID.randomUUID();
        System.out.println("Random UUID: " + uuid.toString());
	}

	
	public static void day(String[] args) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        
        // Get the day of the year
        int dayOfYear = currentDate.getDayOfYear();
        
        System.out.println("Day of the year: " + dayOfYear);
    }
}
