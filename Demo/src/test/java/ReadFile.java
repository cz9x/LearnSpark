import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tony on 2017/3/20.
 */
public class ReadFile {
    /**
     * NIO 内存映射读大文件
     * @param path
     */
    public static void readFile3(String path) {
        long start = System.currentTimeMillis();//开始时间
        long fileLength = 0;
        final int BUFFER_SIZE = 0x300000;// 3M的缓冲
        File file = new File(path);
        fileLength = file.length();
        String regEx = "(2[0-4]\\\\d|25[0-5]|[01]\\\\d\\\\d|\\\\d\\\\d|\\\\d)\\\\\" +\n" +
                "      \".(2[0-4]\\\\d|25[0-5]|[01]\\\\d\\\\d|\\\\d\\\\d|\\\\d)\\\\\" +\n" +
                "      \".(2[0-4]\\\\d|25[0-5]|[01]\\\\d\\\\d|\\\\d\\\\d|\\\\d)\\\\\" +\n" +
                "      \".(2[0-4]\\\\d|25[0-5]|[01]\\\\d\\\\d|\\\\d\\\\d|\\\\d)";
        Pattern pattern = Pattern.compile(regEx);
        try {
            MappedByteBuffer inputBuffer = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileLength);// 读取大文件

            byte[] dst = new byte[BUFFER_SIZE];// 每次读出3M的内容

            for (int offset = 0; offset < fileLength; offset += BUFFER_SIZE) {
                if (fileLength - offset >= BUFFER_SIZE) {
                    for (int i = 0; i < BUFFER_SIZE; i++)
                        dst[i] = inputBuffer.get(offset + i);
                } else {
                    for (int i = 0; i < fileLength - offset; i++)
                        dst[i] = inputBuffer.get(offset + i);
                }
                // 将得到的3M内容给Scanner，这里的XXX是指Scanner解析的分隔符
                Scanner scan = new Scanner(new ByteArrayInputStream(dst)).useDelimiter(" ");
                while (scan.hasNext()) {
                    // 这里为对读取文本解析的方法
                    System.out.print(scan.next() + " ");
//                    scan.next();
                    Matcher matcher = pattern.matcher(scan.next());
                    if(matcher.find()){
                        System.out.println(matcher.group());
                    }
                }
                scan.close();
            }
            System.out.println();
            long end = System.currentTimeMillis();//结束时间
            System.out.println("NIO 内存映射读大文件，总共耗时："+(end - start)+"ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        String path = "/Users/tony/Downloads/nirvana.log3";

        readFile3(path);
    }
}
