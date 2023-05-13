package ca.ubc.cs.cs317.dnslookup;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class DNSMessage {
    public static final int MAX_DNS_MESSAGE_LENGTH = 512;

    // The offset into the message where the header ends and the data begins.
    public final static int DataOffset = 12;

    // Opcode for a standard query
    public final static int QUERY = 0;

    /**
     * TODO:  You will add additional constants and fields
     */
    private final ByteBuffer buffer;

    private final HashMap<Integer, String> posToStr = new HashMap<>();



    /**
     * Initializes an empty DNSMessage with the given id.
     *
     * @param id The id of the message.
     */
    public DNSMessage(short id) {
        this.buffer = ByteBuffer.allocate(MAX_DNS_MESSAGE_LENGTH);
        // TODO: Complete this method
        buffer.putShort(0, id);
        buffer.position(DataOffset);
    }

    /**
     * Initializes a DNSMessage with the first length bytes of the given byte array.
     *
     * @param recvd The byte array containing the received message
     * @param length The length of the data in the array
     */
    public DNSMessage(byte[] recvd, int length) {
        buffer = ByteBuffer.wrap(recvd, 0, length);
        // TODO: Complete this method
        for (int i = 0; i < length; i++) {
            buffer.put(recvd[i]);
        }
        buffer.position(DataOffset);
    }

    /**
     * Getters and setters for the various fixed size and fixed location fields of a DNSMessage
     * TODO:  They are all to be completed
     */
    public int getID() {
        return 0x0000ffff & buffer.getShort(0);
    }

    public void setID(int id) {
        buffer.putShort(0, (short) id);
    }

    public boolean getQR() {
        byte b = buffer.get(2);
        return (b & 0x80) >> 7 == 1;
    }

    public void setQR(boolean qr) {
        byte b = buffer.get(2);
        if (qr) {
            b = (byte) (b | 0x80);
        } else {
            b = (byte) (b & 0x7F);
        }
        buffer.put(2, b);
    }

    public boolean getAA() {
        byte b = buffer.get(2);
        return (b & 0x04) >> 2 == 1;
    }

    public void setAA(boolean aa) {
        byte b = buffer.get(2);
        if (aa) {
            b = (byte) (b | 0x04);
        } else {
            b = (byte) (b & 0xfb);
        }
        buffer.put(2, b);
    }

    public int getOpcode() {
        byte b = buffer.get(2);
        return ((b & 0x78) >> 3);
    }

    public void setOpcode(int opcode) {
        byte b = buffer.get(2);
        byte clearOldCode = (byte) (b & 0x87);
        buffer.put(2, (byte) (clearOldCode | ((byte) opcode) << 3));

    }

    public boolean getTC() {
        byte b = buffer.get(2);
        return (b & 0x02) >> 1 == 1;
    }

    public void setTC(boolean tc) {
        byte b = buffer.get(2);
        if (tc) {
            b = (byte) (b | 0x02);
        } else {
            b = (byte) (b & 0xfd);
        }
        buffer.put(2, b);
    }

    public boolean getRD() {
        byte b = buffer.get(2);
        return (b & 0x01)  == 1;
    }

    public void setRD(boolean rd) {
        byte b = buffer.get(2);
        if (rd) {
            b = (byte) (b | 0x01);
        } else {
            b = (byte) (b & 0xfe);
        }
        buffer.put(2, b);
    }

    public boolean getRA() {
        byte b = buffer.get(3);
        return (b & 0x80) >> 7 == 1;
    }

    public void setRA(boolean ra) {
        byte b = buffer.get(3);
        if (ra) {
            b = (byte) (b | 0x80);
        } else {
            b = (byte) (b & 0x7F);
        }
        buffer.put(3, b);
    }

    public int getRcode() {
        byte b = buffer.get(3);
        return (b & 0x0f);
    }

    public void setRcode(int rcode) {
        byte b = buffer.get(3);
        byte clearOldCode = (byte) (b & 0xf0);
        buffer.put(3, (byte) (clearOldCode | (byte) rcode));
    }

    public int getQDCount() {
        return 0x0000ffff & buffer.getShort(4);
    }

    public void setQDCount(int count) {
        buffer.putShort(4,(short) count);
    }

    public int getANCount() {
        return 0x0000ffff & buffer.getShort(6);
    }

    public int getNSCount() {
        return 0x0000ffff & buffer.getShort(8);
    }

    public int getARCount() {
        return 0x0000ffff & buffer.getShort(10);
    }

    public void setANCount(int count) {
        buffer.putShort(6,(short) count);
    }

    public void setNSCount(int count) {
        buffer.putShort(8,(short) count);
    }

    public void setARCount(int count) {
        buffer.putShort(10,(short) count);
    }

    /**
     * Return the name at the current position() of the buffer.
     *
     * The encoding of names in DNS messages is a bit tricky.
     * You should read section 4.1.4 of RFC 1035 very, very carefully.  Then you should draw a picture of
     * how some domain names might be encoded.  Once you have the data structure firmly in your mind, then
     * design the code to read names.
     *
     * @return The decoded name
     */
    public String getName() {
        // TODO: Complete this method
        int currPos = buffer.position();
        byte numOfBytes = buffer.get();
        String suffix = "";
        // Name Ends when numOfBytes == 0
        if (numOfBytes == 0)
            return "";
        // Starts with pointer
        if ((numOfBytes & 0xc0) == 0xc0) {
            buffer.position(buffer.position() - 1);
            int pointedPos = buffer.getShort() & 0x3fff;
            suffix = posToStr.get(pointedPos);
            assert suffix != null;
            posToStr.put(currPos, suffix);
            return suffix;
        }
        // Starts with numOfBytes
        byte[] bytes = new byte[numOfBytes];
        buffer.get(bytes);
        String str = new String(bytes, StandardCharsets.UTF_8);
        suffix = getName();
        String result = suffix.isEmpty() ? str : str + "." + suffix;
        posToStr.put(currPos, result);
        return result;
    }

    /**
     * The standard toString method that displays everything in a message.
     * @return The string representation of the message
     */
    public String toString() {
        // Remember the current position of the buffer so we can put it back
        // Since toString() can be called by the debugger, we want to be careful to not change
        // the position in the buffer.  We remember what it was and put it back when we are done.
        int end = buffer.position();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(getID()).append(' ');
            sb.append("QR: ").append(getQR() ? "Response" : "Query").append(' ');
            sb.append("OP: ").append(getOpcode()).append(' ');
            sb.append("AA: ").append(getAA()).append('\n');
            sb.append("TC: ").append(getTC()).append(' ');
            sb.append("RD: ").append(getRD()).append(' ');
            sb.append("RA: ").append(getRA()).append(' ');
            sb.append("RCODE: ").append(getRcode()).append(' ')
                    .append(dnsErrorMessage(getRcode())).append('\n');
            sb.append("QDCount: ").append(getQDCount()).append(' ');
            sb.append("ANCount: ").append(getANCount()).append(' ');
            sb.append("NSCount: ").append(getNSCount()).append(' ');
            sb.append("ARCount: ").append(getARCount()).append('\n');
            buffer.position(DataOffset);
            showQuestions(getQDCount(), sb);
            showRRs("Authoritative", getANCount(), sb);
            showRRs("Name servers", getNSCount(), sb);
            showRRs("Additional", getARCount(), sb);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "toString failed on DNSMessage";
        }
        finally {
            buffer.position(end);
        }
    }

    /**
     * Add the text representation of all the questions (there are nq of them) to the StringBuilder sb.
     *
     * @param nq Number of questions
     * @param sb Collects the string representations
     */
    private void showQuestions(int nq, StringBuilder sb) {
        sb.append("Question [").append(nq).append("]\n");
        for (int i = 0; i < nq; i++) {
            DNSQuestion question = getQuestion();
            sb.append('[').append(i).append(']').append(' ').append(question).append('\n');
        }
    }

    /**
     * Add the text representation of all the resource records (there are nrrs of them) to the StringBuilder sb.
     *
     * @param kind Label used to kind of resource record (which section are we looking at)
     * @param nrrs Number of resource records
     * @param sb Collects the string representations
     */
    private void showRRs(String kind, int nrrs, StringBuilder sb) {
        sb.append(kind).append(" [").append(nrrs).append("]\n");
        for (int i = 0; i < nrrs; i++) {
            ResourceRecord rr = getRR();
            sb.append('[').append(i).append(']').append(' ').append(rr).append('\n');
        }
    }

    /**
     * Decode and return the question that appears next in the message.  The current position in the
     * buffer indicates where the question starts.
     *
     * @return The decoded question
     */
    public DNSQuestion getQuestion() {
        // TODO: Complete this method
        String hostName = getName();
        RecordType qType =  RecordType.getByCode(buffer.getShort());
        RecordClass qClass = RecordClass.getByCode(buffer.getShort());
        return new DNSQuestion(hostName, qType, qClass);
    }

    /**
     * Decode and return the resource record that appears next in the message.  The current
     * position in the buffer indicates where the resource record starts.
     *
     * @return The decoded resource record
     */
    public ResourceRecord getRR() {
        // TODO: Complete this method
        // Get DNSQuestion
        String name = getName();
        RecordType rType = RecordType.getByCode(buffer.getShort());
        RecordClass rClass = RecordClass.getByCode(buffer.getShort());
        DNSQuestion question = new DNSQuestion(name, rType, rClass);
        // Get Time to Live
        int ttl = buffer.getInt();
        // Get Length
        int length = buffer.getShort() & 0x0000ffff;
        String address = "";
        switch (rType) {
            case A:
            case AAAA:
                try {
                    byte[] addressBytes = getAddress(length);
                    // Get IP and return
                    return new ResourceRecord(question, ttl, InetAddress.getByAddress(addressBytes));
                } catch (Exception e) {
                    System.out.println("getRR error");
                    e.printStackTrace();
                }
                break;
            case MX:
                // skip preference
                buffer.getShort();
                address = getName();
                return new ResourceRecord(question, ttl, address);
            // case CNAME and NS and other cases
            default:
                address = getName();
                return new ResourceRecord(question, ttl, address);
        }
        return null;
    }

    public byte[] getAddress(int length) {
        // Get an array of byte from buffer given length
        byte[] resultInBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            resultInBytes[i] = buffer.get();
        }
        return resultInBytes;
    }

    /**
     * Helper function that returns a hex string representation of a byte array. May be used to represent the result of
     * records that are returned by a server but are not supported by the application (e.g., SOA records).
     *
     * @param data a byte array containing the record data.
     * @return A string containing the hex value of every byte in the data.
     */
    public static String byteArrayToHexString(byte[] data) {
        return IntStream.range(0, data.length).mapToObj(i -> String.format("%02x", data[i])).reduce("", String::concat);
    }
    /**
     * Helper function that returns a byte array from a hex string representation. May be used to represent the result of
     * records that are returned by a server but are not supported by the application (e.g., SOA records).
     *
     * @param hexString a string containing the hex value of every byte in the data.
     * @return data a byte array containing the record data.
     */
    public static byte[] hexStringToByteArray(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            String s = hexString.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte)Integer.parseInt(s, 16);
        }
        return bytes;
    }

    /**
     * Add an encoded name to the message. It is added at the current position and uses compression
     * as much as possible.  Make sure you understand the compressed data format of DNS names.
     *
     * @param name The name to be added
     */
    public void addName(String name) {
        // TODO: Complete this method
        // Get all the values (Names) for HashMap
        Collection<String> values = posToStr.values();
        // Get all the keys (Offset) for HashMap
        Set<Integer> keys = posToStr.keySet();
        // Split the name by "."
        String[] nameSplit = name.split("[.]");
        int strLength = nameSplit.length;
        int segments = strLength;
        // If pointer is used in this process set to true
        boolean pointer = false;
        int pointerBytes = 0;
        // Current Pos
        int offsetPos = buffer.position();
        while (segments != 0) {
            // Get the candidate string for search
            String candidateStr = "";
            for (int i = 0; i < segments; i++) {
                if (i == 0) {
                    candidateStr = nameSplit[strLength - 1];
                } else {
                    candidateStr = nameSplit[strLength - i - 1] + "." + candidateStr;
                }
            }
            // search for candidate string
            if (values.contains(candidateStr)) {
                for (Integer k : keys) {
                    if (posToStr.get(k).equals(candidateStr)) {
                       pointerBytes = (k | 0xc000);
                       // Jump out loop if found
                        break;
                    }
                }
                pointer = true;
                break;
            }
            // Put candidate string in map
            posToStr.put(offsetPos, candidateStr);
            // Put strings in buffer
            byte segLen = (byte) nameSplit[strLength - segments].length();
            buffer.put(segLen);
            byte[] bytesArray = convertStrToByte(nameSplit[strLength - segments]);
            for (int j = 0; j < bytesArray.length; j++) {
                buffer.put(bytesArray[j]);
            }
            offsetPos = buffer.position();
            segments--;
        }
        // If not ends with pointer, put 0 at the ned
        if (pointer == false) {
            buffer.put((byte) 0);
        } else {
            // If ends with pointer, put pointer at the ned
            buffer.putShort((short) pointerBytes);
        }
    }

    /**
     * Add an encoded question to the message at the current position.
     * @param question The question to be added
     */
    public void addQuestion(DNSQuestion question) {
        // TODO: Complete this method
        String qName = question.getHostName();
        addName(qName);
        addQType(question.getRecordType());
        addQClass(question.getRecordClass());
        setQDCount(getQDCount() + 1);
    }

    // Add question in Resource Record
    public void addRRDNSQ(DNSQuestion question) {
        String qName = question.getHostName();
        addName(qName);
        addQType(question.getRecordType());
        addQClass(question.getRecordClass());
    }

    public void addTTL(long ttl) {
        buffer.put((byte) ((ttl >> 24) & 0xff));
        buffer.put((byte) ((ttl >> 16) & 0xff));
        buffer.put((byte) ((ttl >> 8) & 0xff));
        buffer.put((byte) (ttl & 0xff));
    }


    /**
     * Add an encoded resource record to the message at the current position.
     * The record is added to the additional records section.
     * @param rr The resource record to be added
     */
    public void addResourceRecord(ResourceRecord rr) {
        addResourceRecord(rr, "additional");
    }

    /**
     * Add an encoded resource record to the message at the current position.
     *
     * @param rr The resource record to be added
     * @param section Indicates the section to which the resource record is added.
     *                It is one of "answer", "nameserver", or "additional".
     */
    public void addResourceRecord(ResourceRecord rr, String section) {
        // TODO: Complete this method
        addRRDNSQ(rr.getQuestion());
        addTTL(rr.getRemainingTTL());
        RecordType rType = rr.getRecordType();
        int length;
        switch (rType) {
            case A:
            case AAAA:
                length = rr.getInetResult().getAddress().length;
                buffer.putShort((short) (length & 0x0000ffff));
                buffer.put(rr.getInetResult().getAddress());
                break;
            case CNAME:
            case NS:
                length = rr.getTextResult().length();
                buffer.putShort((short) (length & 0x0000ffff));
                addName(rr.getTextResult());
                break;
            case MX:
                length = rr.getTextResult().length();
                // Add length of preference
                length += 2;
                buffer.putShort((short) (length & 0x0000ffff));
                buffer.putShort((short) 0);
                addName(rr.getTextResult());
                break;
            default:
                System.out.println("Invalid RecordType");
        }
        // Add count
        switch (section) {
            case "answer":
                setANCount(getANCount() + 1);
                break;
            case "nameserver":
                setNSCount(getNSCount() + 1);
                break;
            case "additional":
                setARCount(getARCount() + 1);
                break;
            default:
                System.out.println("Invalid Section Name");
        }
    }

    /**
     * Add an encoded type to the message at the current position.
     * @param recordType The type to be added
     */
    private void addQType(RecordType recordType) {
        // TODO: Complete this method
        buffer.putShort((short) recordType.getCode());
    }

    /**
     * Add an encoded class to the message at the current position.
     * @param recordClass The class to be added
     */
    private void addQClass(RecordClass recordClass) {
        // TODO: Complete this method
        buffer.putShort((short) recordClass.getCode());
    }

    /**
     * Return a byte array that contains all the data comprising this message.  The length of the
     * array will be exactly the same as the current position in the buffer.
     * @return A byte array containing this message's data
     */
    public byte[] getUsed() {
        // TODO: Complete this method
        int pos = buffer.position();
        byte[] used = new byte[buffer.position()];
        for (int i = 0; i < pos; i++) {
            used[i] = buffer.get(i);
        }
        buffer.position(pos);
        return used;
    }

    /**
     * Returns a string representation of a DNS error code.
     *
     * @param error The error code received from the server.
     * @return A string representation of the error code.
     */
    public static String dnsErrorMessage(int error) {
        final String[] errors = new String[]{
                "No error", // 0
                "Format error", // 1
                "Server failure", // 2
                "Name error (name does not exist)", // 3
                "Not implemented (parameters not supported)", // 4
                "Refused" // 5
        };
        if (error >= 0 && error < errors.length)
            return errors[error];
        return "Invalid error message";
    }

    // Convert String to an array of bytes
    public static byte[] convertStrToByte(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            String charHex = Integer.toHexString(c);
            stringBuilder.append(charHex);
        }
        return hexStringToByteArray(stringBuilder.toString());
    }

}
