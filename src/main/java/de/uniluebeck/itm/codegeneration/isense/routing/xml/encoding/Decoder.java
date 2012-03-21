package de.uniluebeck.itm.codegeneration.isense.routing.xml.encoding;

/**
 * A Decoder deserializes binary data in form of a byte array. Typically, this is a plain old java object (POJO) that
 * holds the content of the deserialized object.
 *
 * @param <T>
 * 		the type of the deserialized object
 */
public interface Decoder<T> {

	/**
	 * Deserializes the byte array to an in-memory representation of type T.
	 *
	 * @param bytes
	 * 		the serialized object representation
	 *
	 * @return the deserialized object
	 *
	 * @throws DecodingException
	 * 		if any Exception occurs during the decoding process or the data to be decoded doesn't
	 * 		conform to the data type specification
	 */
	T decode(byte[] bytes) throws DecodingException;

}
