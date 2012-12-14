package com.thoughtcomplex.starlight.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Indicates that a class can be written directly to an OutputStream.
 * 
 * <p>By contract, implementing classes of Streamable have a public, zero-argument "anonymous" constructor, and are mutable. Although
 * the internal representation of a reconstituted object may be different, such as a different choice of capacity, hash codes, cached
 * items, or tree depth, an object reconstituted from streamFrom must be indistinguishable from the object streamTo was called on. So,
 * for instance, an object with double precision cannot write floats, unless its <i>own</i> contract forbids numbers outside the
 * floating point range and below the precision limit.
 * 
 * <p>The procedure for writing a Streamable to disk is simply to open the stream and call Streamable.streamTo(stream). The process
 * for reading a Streamable from the stream is to create the object with its zero-argument constructor and call
 * Streamable.streamFrom(stream).
 * 
 * <p>There are two ways to write out a composite object: One is to use a ByteArrayOutputStream, turn it into a byte[], and write the
 * byte[]. The other is to use Streamable on the component objects as well, and to write them out with streamTo. The latter is less
 * time and resource intensive, and so it is preferred for situations when length information is not required.
 * 
 * @author Isaac Ellingson
 */
public interface Streamable {
	/**
	 * Write this object to the OutputStream provided. The data written is guaranteed to be sufficient to reconstruct an object
	 * indistinguishable from this one, although the internal representation may change. Only the data is written, however, and not
	 * information about what class the data comes from. Generally, the smallest possible representation that meets these requirements
	 * is preferable.
	 * @param out				The OutputStream to write this object to
	 * @throws IOException		Thrown if any problem is encountered writing to the stream.
	 */
	public void streamTo(OutputStream out) throws IOException;
	/**
	 * Read an object of this class from the InputStream provided, and cause this object to "become" an object indistinguishable from
	 * the original. Undefined behavior will occur if the data in the stream is an object of a different class or not an object at all.
	 * This method does not guarantee any data validation, as it would be impossible without writing class information to the file to
	 * know, for certain, that it is information of a particular type.
	 * @param in		The InputStream to read the object from
	 * @throws IOException		Thrown if any problem is encountered reading from the stream.
	 */
	public void streamFrom(InputStream in) throws IOException;
	
	
	/**
	 * Helper class for streaming out primitive and complex data types.
	 * @author Isaac Ellingson
	 */
	public class Helper {
		/**
		 * Reads a Streamable from the stream. Due to type erasure, this method cannot ever work with Generic types. In those cases it
		 * is necessary to manually instance the object and call StreamFrom.
		 * @param classToStream
		 * @param in
		 * @return
		 */
		public static <T extends Streamable> T streamFrom(Class<T> classToStream, InputStream in) {
			try {
				T result = classToStream.newInstance();
				result.streamFrom(in);
				return result;
			} catch (Exception ex) {
				return null;
			}
		}
	
		//## METHODS FOR WRITING DOWN TO THE STREAM ##
		
		public static void byteToStream(int value, OutputStream out) throws IOException {
			out.write(value & 0xFF);
		}
		
		public static void shortToStream(int value, OutputStream out) throws IOException {
			out.write((value >> 8) & 0xFF);
			out.write(value & 0xFF);
		}
		
		public static void intToStream(int value, OutputStream out) throws IOException {
			out.write((value >> 24) & 0xFF);
			out.write((value >> 16) & 0xFF);
			out.write((value >> 8) & 0xFF);
			out.write(value & 0xFF);
		}
		
		public static void longToStream(long value, OutputStream out) throws IOException {
			out.write((int)((value >> 56) & 0xFF));
			out.write((int)((value >> 48) & 0xFF));
			out.write((int)((value >> 40) & 0xFF));
			out.write((int)((value >> 32) & 0xFF));
			out.write((int)((value >> 24) & 0xFF));
			out.write((int)((value >> 16) & 0xFF));
			out.write((int)((value >> 8) & 0xFF));
			out.write((int)(value & 0xFF));
		}
		
		public static void floatToStream(float value, OutputStream out) throws IOException {
			intToStream(Float.floatToIntBits(value),out);
		}
		
		public static void doubleToStream(double value, OutputStream out) throws IOException {
			longToStream(Double.doubleToLongBits(value),out);
		}
		
		public static void shortStringToStream(String value, OutputStream out) throws IOException {
			shortToStream(value.length(),out);
			out.write(value.getBytes(Charset.forName("UTF-8")));
		}
		
		/**
		 * Write the String out to the stream in as close to a native representation as possible. Internally, a String's length is
		 * bounded by an int value and stored in UTF-16, so that's what we use here.
		 * @param value		The String to put on the wire
		 * @param out		The wire to put the String on
		 * @throws IOException
		 */
		public static void stringToStream(String value, OutputStream out) throws IOException {
			intToStream(value.length(),out);
			byte[] stringBytes = value.getBytes(Charset.forName("UTF-16BE"));
			out.write(stringBytes);
			//System.out.println("Writing "+value.length()+" characters ("+stringBytes.length+" bytes)");
			//System.out.println("Extra Two Bytes: "+(stringBytes[0]<<8+stringBytes[1]));
			assert(stringBytes.length == 2*value.length()) :
				"Assumed (16 bits == 2 bytes) for this String... but it wasn't!";
		}
		
		//## METHODS FOR READING UP FROM THE STREAM ##
		/** Included for completeness. Identical to InputStream.read() **/
		public static int byteFromStream(InputStream in) throws IOException {
			return in.read();
		}
		
		/**
		 * Grabs a short from an InputStream. The underlying data must be two bytes and big-endian. This implementation treats the
		 * value as if it's unsigned, but due to the magic of sign extension, signed data (such as java's primitive type 'short')
		 * should store just fine, even across a direct cast from the integer returned from this function.
		 * @param in
		 * @return
		 * @throws IOException
		 */
		public static int shortFromStream(InputStream in) throws IOException {
			int resultHigh = in.read(); if (resultHigh==-1) return -1;
			int resultLow = in.read(); if (resultLow==-1) return -1;
			return resultLow | (resultHigh << 8);
		}
		
		public static int intFromStream(InputStream in) throws IOException {
			//Did I just make a generic "read X bytes from stream" method? I think I did!
			int total = 0;
			for(int i=3; i>=0; i--) {
				int curByte = in.read();
				if (curByte==-1) return -1; //no reward given for partial data.
				total |= curByte << (i*8);
			}
			return total;
		}
		
		public static long longFromStream(InputStream in) throws IOException {
			//Did I just make a generic "read X bytes from stream" method? I think I did!
			long total = 0;
			for(int i=7; i>=0; i--) {
				long curByte = in.read();
				if (curByte==-1) return -1; //no reward given for partial data.
				total |= curByte << (i*8);
			}
			return total;
		}
		
		public static float floatFromStream(InputStream in) throws IOException {
			return Float.intBitsToFloat(intFromStream(in));
		}
		
		public static double doubleFromStream(InputStream in) throws IOException {
			return Double.longBitsToDouble(longFromStream(in));
		}
		
		public static String shortStringFromStream(InputStream in) throws IOException {
			int len = shortFromStream(in);
			if (len<=0) return "";
			byte[] raw = new byte[len];
			int bytesReturned = in.read(raw);
			if (bytesReturned<len) return ""; //incomplete data is ignored
			return new String(raw,Charset.forName("UTF-8")); //We're in trouble if UTF-8 isn't supported.
		}
		
		public static String stringFromStream(InputStream in) throws IOException {
			int len = intFromStream(in);
			if (len<=0) return "";
			byte[] raw = new byte[len*2];
			int bytesReturned = in.read(raw);
			if (bytesReturned<len*2) return ""; //incomplete data is ignored
			return new String(raw,Charset.forName("UTF-16BE")); //We're in trouble if UTF-8 isn't supported.
		}
	}
}
