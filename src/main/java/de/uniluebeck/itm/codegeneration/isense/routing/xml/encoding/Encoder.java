package de.uniluebeck.itm.codegeneration.isense.routing.xml.encoding;

/**
 * An Encoder serializes an object to binary data in form of a byte array. The object is typically a plain old Java
 * object (POJO) that holds the content to be serialized.
 *
 * @param <T>
 * 		the type of the deserialized object
 */
public interface Encoder<T> {

	/**
	 * Serializes the in-memory representation of type T to a byte array.
	 *
	 * @param object
	 * 		the object to be serialized
	 *
	 * @return a byte array containing the serialized object
	 *
	 * @throws EncodingException
	 * 		if any Exception occurs during the encoding process or the data to be encoded doesn't conform to the data
	 * 		type specification
	 */
	byte[] encode(T object) throws EncodingException;

}
