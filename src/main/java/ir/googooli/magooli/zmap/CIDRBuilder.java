package ir.googooli.magooli.zmap;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CIDRBuilder {
    public static void main(String[] args) throws IOException {
        List<String> list = Files.readAllLines(Paths.get("D:\\rasoul\\scan\\ir.csv"));
        Set<String> ranges=new HashSet<>();
        for (String s : list) {
            String[] split = s.split(",");
            ranges.addAll(getCIDR(split[0], split[1]));
        }
        for (String range : ranges) {
            System.out.println(range);
        }

    }

    private static Set<String> getCIDR(String from, String to) {
        IPAddressString string1 = new IPAddressString(from);
        IPAddressString string2 = new IPAddressString(to);
        IPAddress one = string1.getAddress(), two = string2.getAddress();
        IPAddressSeqRange range = one.toSequentialRange(two);
        IPAddress[] blocks = range.spanWithPrefixBlocks();
        Set<String> res = new HashSet<>();
        for (IPAddress block : blocks) {
            res.add(block.toString());
        }
        return res;
    }
}
