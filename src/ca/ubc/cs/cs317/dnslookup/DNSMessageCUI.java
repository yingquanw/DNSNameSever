package ca.ubc.cs.cs317.dnslookup;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DNSMessageCUI {
    private DNSMessage message;
    /**
     * Main function, called when program is first invoked.
     *
     * @param args list of arguments specified in the command line.
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            System.err.println("Invalid call. Usage:");
            System.err.println("\tjava -jar DNSMessage.jar");
            System.exit(1);
        }
        new DNSMessageCUI().interact();
    }

    private void interact() {
        message = new DNSMessage((short) 23);

        Scanner in = new Scanner(System.in);
        Console console = System.console();
        do {
            // Use console if one is available, or standard input if not.
            String commandLine;
            if (console != null) {
                System.out.print("DNSMESSAGE> ");
                commandLine = console.readLine();
            } else {
                try {
                    commandLine = in.nextLine();
                } catch (NoSuchElementException ex) {
                    break;
                }
            }

            // If reached end-of-file, leave
            if (commandLine == null) break;

            // Ignore leading/trailing spaces and anything beyond a comment character
            commandLine = commandLine.split("#", 2)[0].trim();

            // If no command shown, skip to next command
            if (commandLine.isEmpty()) continue;

            String[] commandArgs = commandLine.split(" ");

            if (commandArgs[0].equalsIgnoreCase("quit") ||
                    commandArgs[0].equalsIgnoreCase("exit") ||
                    commandArgs[0].equalsIgnoreCase("q")) {
                break;
            } else if (commandArgs[0].equalsIgnoreCase("read")) {
                // Read a message from a file
                if (commandArgs.length == 2) {
                    try {
                        message = readFromFile(commandArgs[1]);
                    } catch (IOException e) {
                        System.err.println("Can't read file \"" + commandArgs[1] + "\"");
                    }
                    System.out.println("File  \"" + commandArgs[1] + "\" contains:");
                    System.out.println(message);
                } else {
                    System.out.println("Invalid call. Format:\n" +
                            "\tread <filename>");
                }
            } else if (commandArgs[0].equalsIgnoreCase("write")) {
                // Write a message to a file
                if (commandArgs.length == 2) {
                    try {
                        writeToFile(commandArgs[1]);
                    } catch (IOException e) {
                        System.err.println("Can't write file \"" + commandArgs[1] + "\"");
                    }
                } else {
                    System.out.println("Invalid call. Format:\n" +
                            "\twrite <filename>");
                }
            } else if (commandArgs[0].equalsIgnoreCase("new")) {
                // VERBOSE: Turn verbose setting on or off
                short id;
                if (commandArgs.length == 2) {
                    try {
                        id = Short.parseShort(commandArgs[1]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Invalid call. Format:\n\tnew [id]");
                        continue;
                    }
                } else {
                    id = 23;
                }
                message = new DNSMessage(id);
                System.out.println("Allocated new empty message with id: " + id);
            } else if (commandArgs[0].equalsIgnoreCase("add")) {
                // Add something to the message.
                if (doAdd(commandArgs)) {
                    System.out.println("Message is now: ");
                    System.out.println(message);
                }
            } else if (commandArgs[0].equalsIgnoreCase("show") ||
                    commandArgs[0].equalsIgnoreCase("print")) {
                // SHOW: Print the message
                System.out.println("Message is now: ");
                System.out.println(message);

            } else {
                System.err.println("Invalid command. Valid commands are:");
                System.err.println("\tread <filename>");
                System.err.println("\twrite <filename>");
                System.err.println("\tnew [id]");
                System.err.println("\tadd question name type");
                System.err.println("\tadd resource name type value");
                System.err.println("\tshow or print");
                System.err.println("\tquit");
            }
        } while (true);

        System.out.println("Goodbye!");
    }

    private DNSMessage readFromFile(String filename) throws IOException {
        byte[] buf = new byte[512];
        int len;
        try (FileInputStream f = new FileInputStream(filename)) {
            len = f.read(buf);
            return new DNSMessage(buf, len);
        }
    }

    private void writeToFile(String filename) throws IOException {
        byte[] buf = message.getUsed();
        try (FileOutputStream f = new FileOutputStream(filename)) {
            f.write(buf, 0, buf.length);
            System.out.println("Wrote " + buf.length + " bytes to \"" + filename + "\"");
        }
    }

    private boolean doAdd(String[] commandArgs) {
        String thing = commandArgs[1];
        switch (thing) {
            case "question":
            case "q":
                if (commandArgs.length != 4) {
                    System.err.println("Invalid call. Format:\n\tadd question name type");
                    return false;
                }
                String name = commandArgs[2];
                String typestr = commandArgs[3];
                RecordType type = RecordType.valueOf(typestr.toUpperCase());
                DNSQuestion question = new DNSQuestion(name, type, RecordClass.IN);
                if (message.getARCount() > 0) {
                    System.err.println("Can't add questions after resource records");
                    return false;
                }
                message.addQuestion(question);
                break;
            case "rr":
            case "resource":
            case "resourcerecord":
                try {
                    if (commandArgs.length != 5) {
                        System.err.println("Invalid call. Format:\n\tadd resource name type value");
                        return false;
                    }
                    name = commandArgs[2];
                    typestr = commandArgs[3];
                    String valuestr = commandArgs[4];
                    type = RecordType.valueOf(typestr.toUpperCase());
                    question = new DNSQuestion(name, type, RecordClass.IN);
                    ResourceRecord rr;
                    if (type == RecordType.A || type == RecordType.AAAA) {
                        InetAddress address = inetAddress(valuestr);
                        rr = new ResourceRecord(question, 3600, address);
                    } else {
                        rr = new ResourceRecord(question, 3600, valuestr);
                    }
                    message.addResourceRecord(rr);
                } catch (Exception e) {
                    System.err.println("Bad argument");
                    return false;
                }
                break;
            default:
                System.err.println("Bad argument to add \"" + thing + "\"");
                return false;
        }
        return true;
    }

    private InetAddress inetAddress(String valuestr) throws UnknownHostException {
        return InetAddress.getByName(valuestr);
    }
}
