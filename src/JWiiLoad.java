
import java.util.prefs.Preferences;
import java.util.zip.*;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class JWiiLoad {
	static Socket socket;
	private static Preferences prefs = Preferences.userRoot().node("/com/vgmoose/jwiiload");

	// host and port of receiving wii (use 4299 and your own ip) also the .dol
	//private static final int    port = 4299;
	//	private static final String host = "192.168.1.105";
	static File filename; // = new File("/Users/Ricky/Downloads/wiimod_v3_0/card/app/wiimod/boot.dol");
	static File compressed;
	//private static String arguments ="";

	static GUI framey;

	static String host;
	static String ip;

	static String lastip = prefs.get("host", "0.0.0.0");
	static boolean autosend = prefs.getBoolean("auto", true);
	static int port = prefs.getInt("port",4299);
	static String arguments = prefs.get("args","");

	static boolean stopscan = false;

	static boolean cli = false;

	public static void main(String[] args) 
	{
		if (args.length==1)
		{
			System.out.println("JWiiload 1.0\ncoded by VGMoose, based on wiiload by dhewg\n\nusage:\n\tjava -jar JWiiload.jar <address> <filename> <application arguments>\n\npass $WIILOAD as the first argument if your environment is set up that way.\n\npass \"AUTO\" as the first argument to try to automatically find the Wii.\n\npass \"PREV\" as the first argument to use the last known Wii IP that worked.\n");
			System.exit(27);
		}
		if (args.length!=0)
		{
			System.out.println("Welcome to JWiiload CLI!");
			cli = true;
			
			port = 4299;

			filename = new File(args[1]);
			if (!filename.exists())
			{
				System.out.println("File at "+filename.getAbsolutePath()+" not found!");
				System.exit(2);
			}

			if (args[0].startsWith("tcp:"))
				args[0]=args[0].substring(4);

			if (args[0].equalsIgnoreCase("PREV"))
			{
				if (lastip.equals("0.0.0.0"))
				{
					System.out.println("There is no known previous working IP stored in this machine!");
					args[0]="AUTO";
				}
				else
					args[0]=lastip;
			}

			if (args[0].equalsIgnoreCase("AUTO"))
			{	
				tripleScan();
				if (host==null)
				{
					System.out.println("Could not find Wii through auto-detection");
					System.exit(3);
				}
				else if (host.equals("rate"))
				{
					System.out.println("Too many wireless requests to auto-detect, try again later.");
					System.exit(4);
				}
				else
					args[0]=host;
			}


			System.out.println("\nIP: "+args[0]);
			host = args[0];

			System.out.println("File: "+filename.getName());

			if (args.length>2)
				for (int x=2;x<args.length;x++)
				{
					arguments+=args[x];
					if (x!=args.length-1)
						arguments+=" ";
				}

			System.out.print("Arguments: "+arguments);

			if (arguments.length()==0)
				System.out.print("none");

			System.out.println("\n");

			if (socket==null)
				if (!connects())
					System.exit(1);

		}
		else
		{
			framey = new GUI();		// Create the JFrame GUI

			if (autosend)
			{
				filename = framey.chooseFile();

				if (JWiiLoad.filename!=null)
					GUI.filename.setText(JWiiLoad.filename.getName());
			}
		}

		if (filename!=null)
		{
			//button5.setEnabled(true);
			if (!cli)
				framey.setButton(true);

			compressData();	
		}


		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

				// Write preferences
				prefs.put("host",lastip);
				prefs.putBoolean("auto", autosend);
				if (!cli) prefs.putInt("port",port);
				prefs.put("args",arguments);
				
				if (compressed!=filename)
					compressed.delete();
			}
		}));

		if (args.length==0 && autosend && filename!=null)
			tripleScan();

		if (filename!=null && (autosend || cli))
			wiisend();

	}

	public static boolean connects()
	{
		System.out.println("Connecting to "+host+"...");

		try{
			socket = new Socket(host, port);
			System.out.println("Connection successful!\n");
			return true;
		} catch (Exception e) {
			System.out.println("Connection failed.");
			return false;
		}

	}

	public static void compressData()
	{
		try
		{
			// Compress the file to send it faster
			if (!cli) framey.setText("Compressing data...");
			System.out.println("Compressing data...");
			compressed = compressFile(filename);
			if (!cli) framey.setText("Data compressed!");
			System.out.println("Compression successful! ("+(int)(100*((compressed.length()+0.0)/filename.length()))+"% smaller)\n");

		} catch(Exception e){
			// Fall back in case compressed file can't be written
			System.out.println("Compression failed! Not going to send it compressed.\n");
			compressed = filename;
		}
	}

	public static void tripleScan()
	{
		stopscan = false;
		for (int x=0; x<3; x++)
		{
			scan(x);
			if (host!=null)
				break;
		}
	}

	public static void wiisend()
	{

		try
		{
			// Open socket to wii with host and port and setup output stream
			if (cli) System.out.println("Greeting the Wii...");

			if (host==null)
				socket = new Socket(host, port);

			if (!cli) framey.setText("Talking to Wii...");

			OutputStream os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);

			if (!cli) framey.setText("Preparing data...");
			System.out.println("Preparing local data...");

			byte max = 0;
			byte min = 5;

			short argslength = (short) (filename.getName().length()+arguments.length()+1);

			int clength = (int) (compressed.length());  // compressed filesize
			int ulength = (int) (filename.length());	// uncompressed filesize

			// Setup input stream for sending bytes later of compressed file
			InputStream is = new FileInputStream(compressed);
			BufferedInputStream bis = new BufferedInputStream(is);

			byte b[]=new byte[128*1024];
			int numRead=0;

			if (!cli) framey.setText("Talking to Wii...");
			System.out.println("Perparing remote data...");

			dos.writeBytes("HAXX");

			dos.writeByte(max);
			dos.writeByte(min);

			dos.writeShort(argslength);

			dos.writeInt(clength);	// writeLong() sends 8 bytes, writeInt() sends 4
			dos.writeInt(ulength);

			//dos.size();	// Number of bytes sent so far, should be 16

			if (!cli) framey.setText("Sending "+filename.getName());
			System.out.println("Sending "+filename.getName()+"...");
			dos.flush();

			while ( ( numRead=bis.read(b)) > 0) {
				dos.write(b,0,numRead);
				System.out.println(dos.size());
				dos.flush();
			}
			dos.flush();

			if (!cli) framey.setText("Talking to Wii...");
			if (arguments.length()!=0)
				System.out.println("Sending arguments...");
			else
				System.out.println("Finishing up...");

			dos.writeBytes(filename.getName()+"\0");

			String[] argue = arguments.split(" ");

			for (String x : argue)
				dos.writeBytes(x+"\0");

			if (!cli) framey.setText("All done!");
			System.out.println("\nFile transfer successful!");

			lastip = host;
			
			if (compressed!=filename)
				compressed.delete();

		}
		catch (Exception ce)
		{
			if (!cli) framey.setText("No Wii found");
			int a=0;

			if (host==null)
				host="";

			System.out.println("No Wii found at "+host+"!");

			ce.printStackTrace();
			if (!cli)
			{
				if (host.equals("rate"))
					a = framey.showRate();
				else
					a= framey.showLost();
			}
			else
			{
				System.exit(1);
			}

			if (a==0)
			{
				tripleScan();
				wiisend();
			}

		}
	}

	static void scan(int t)
	{			
		host=null;

		if (!cli) framey.setText("Finding Wii...");
		System.out.println("Searching for a Wii...");
		String output = null;

		InetAddress localhost=null;

		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			System.out.println("Auto-locate not supported on this system.");
			if (!cli) framey.setText("Auto-locate not supported.");
			else System.exit(8);

			e1.printStackTrace();
		}

		// this code assumes IPv4 is used
		byte[] ip = localhost.getAddress();

		for (int i = 1; i <= 254; i++)
		{
			try
			{
				ip[3] = (byte)i; 
				InetAddress address = InetAddress.getByAddress(ip);

				if (address.isReachable(10*t))
				{
					output = address.toString().substring(1);
					System.out.print(output + " is on the network");

					// Attempt to open a socket
					try
					{
						socket = new Socket(output,port);
						System.out.println("and is potentially a Wii!");
						if (!cli) 
							{
							framey.setText("Wii found!");
							GUI.wiiip.setText(output);
							}
						
						host=output;
						return;
					} catch (Exception e) {
						System.out.println();
						//e.printStackTrace();
					}

				}
			} catch (ConnectException e) {
				if (!cli) framey.setText("Rate limited");
				host="rate";
				e.printStackTrace();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			if (stopscan)
			{
				if (!cli) framey.setText("Scan aborted");
				System.out.println("Scan aborted");
				break;
			}
		} 

		return;

	}

	public static File compressFile(File raw) throws IOException
	{
		File compressed = new File(filename+".wiiload.gz");
		InputStream in = new FileInputStream(raw);
		OutputStream out =
			new DeflaterOutputStream(new FileOutputStream(compressed));
		byte[] buffer = new byte[1000];
		int len;
		while((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
		return compressed;
	}

}