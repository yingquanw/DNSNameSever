package ca.ubc.cs.cs317.dnslookup;

import org.junit.jupiter.api.Test;


import static ca.ubc.cs.cs317.dnslookup.DNSMessage.convertStrToByte;
import static org.junit.jupiter.api.Assertions.*;

public class DNSMessageTest {
    @Test
    public void testConstructor() {
        DNSMessage message = new DNSMessage((short)23);
        assertFalse(message.getQR());
        assertFalse(message.getRD());
        assertEquals(0, message.getQDCount());
        assertEquals(0, message.getANCount());
        assertEquals(0, message.getNSCount());
        assertEquals(0, message.getARCount());
        assertEquals(23, message.getID());
    }
    @Test
    public void testBasicFieldAccess() {
        DNSMessage message = new DNSMessage((short)23);
        message.setOpcode(DNSMessage.QUERY);
        assertEquals(DNSMessage.QUERY, message.getOpcode());
        message.setQR(true);
        assertTrue(message.getQR());
        message.setAA(true);
        assertTrue(message.getAA());
        message.setTC(true);
        assertTrue(message.getTC());
        message.setRA(true);
        assertTrue(message.getRA());
        message.setRD(true);
        assertTrue(message.getRD());
        message.setRcode(4);
        assertEquals(4, message.getRcode());
        message.setQDCount(1);
        assertEquals(1, message.getQDCount());
        message.setARCount(1);
        assertEquals(1, message.getARCount());
    }
    @Test
    public void testAddQuestion() {
        DNSMessage request = new DNSMessage((short)23);
        DNSQuestion question1 = new DNSQuestion("ubc.ca", RecordType.A, RecordClass.IN);
        DNSQuestion question2 = new DNSQuestion("cs.ubc.ca", RecordType.NS, RecordClass.IN);
        request.addQuestion(question1);
        request.addQuestion(question2);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        assertEquals(request.getID(), reply.getID());
        assertEquals(request.getQDCount(), reply.getQDCount());
        assertEquals(request.getANCount(), reply.getANCount());
        assertEquals(request.getNSCount(), reply.getNSCount());
        assertEquals(request.getARCount(), reply.getARCount());
        DNSQuestion replyQuestion1 = reply.getQuestion();
        assertEquals(question1, replyQuestion1);
        DNSQuestion replyQuestion2 = reply.getQuestion();
        assertEquals(question2, replyQuestion2);
    }
    @Test
    public void testAddResourceRecord() {
        DNSMessage request = new DNSMessage((short)23);
        DNSQuestion question = new DNSQuestion("norm.cs.ubc.ca", RecordType.NS, RecordClass.IN);
        ResourceRecord rr = new ResourceRecord(question, RecordType.NS.getCode(), "ns1.cs.ubc.ca");
        request.addResourceRecord(rr);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        assertEquals(request.getID(), reply.getID());
        assertEquals(request.getQDCount(), reply.getQDCount());
        assertEquals(request.getANCount(), reply.getANCount());
        assertEquals(request.getNSCount(), reply.getNSCount());
        assertEquals(request.getARCount(), reply.getARCount());
        ResourceRecord replyRR = reply.getRR();
        assertEquals(rr, replyRR);
    }

    @Test
    public void testAddResourceRecord2() {
        DNSMessage request = new DNSMessage((short)23);
        DNSQuestion question = new DNSQuestion("norm.cs.ubc.ca", RecordType.NS, RecordClass.IN);
        ResourceRecord rr = new ResourceRecord(question, RecordType.NS.getCode(), "mail.f");
        request.addResourceRecord(rr);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        assertEquals(request.getID(), reply.getID());
        assertEquals(request.getQDCount(), reply.getQDCount());
        assertEquals(request.getANCount(), reply.getANCount());
        assertEquals(request.getNSCount(), reply.getNSCount());
        assertEquals(request.getARCount(), reply.getARCount());
        ResourceRecord replyRR =  reply.getRR();
        assertEquals(rr, replyRR);
    }
    @Test
    public void testConvertStrToBinary() {
//        byte[] target = new byte[]{(byte) 01100001,(byte)01100010,(byte)01100011};
//        assertEquals(target, convertStrToBinary("abc"));
        for (byte b : convertStrToByte("abc")) {
            System.out.println(b);
        }
    }
    @Test
    public void testGetUsed() {
        DNSMessage request = new DNSMessage((short)1);
        byte[] content = request.getUsed();


    }
}
