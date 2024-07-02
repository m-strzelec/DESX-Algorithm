package krypto.zad;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static DESXEncryption desx;

    public static void main(String[] args) {
        desx = new DESXEncryption();
        run();
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);
        int option = 4;
        while(option != 0){
            System.out.println(
                    """
                            [1] Encrypt message
                            [2] Decrypt message
                            [3] Enter message
                            [4] Show message as array and string
                            [5] Show DES key
                            [6] Show DESX keys
                            [0] EXIT
                            """
            );
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    desx.run(true);
                    System.out.print("Message as array: ");
                    arrayShow(desx.getMsg());
                    System.out.print("Message as string: ");
                    stringShow(desx.getMsg());
                    break;
                case 2:
                    desx.run(false);
                    System.out.print("Message as array: ");
                    arrayShow(desx.getMsg());
                    System.out.print("Message as string: ");
                    stringShow(desx.getMsg());
                    break;
                case 3:
                    System.out.print("New message: ");
                    scanner.nextLine();
                    String msg = scanner.nextLine();
                    desx.setMsg(msg.getBytes());
                    break;
                case 4:
                    System.out.print("Message as array: ");
                    arrayShow(desx.getMsg());
                    System.out.print("Message as string: ");
                    stringShow(desx.getMsg());
                    break;
                case 5:
                    System.out.print("Key: ");
                    stringShow(desx.getKey());
                    break;
                case 6:
                    System.out.print("First key: ");
                    stringShow(desx.getFirstKey());
                    System.out.print("Second key: ");
                    stringShow(desx.getSecondKey());
                    break;
                case 0:
                    break;
                default:
                    System.out.println("WRONG OPTION");
            }
        }
    }

    private static void arrayShow(byte[] input){
        System.out.println(Arrays.toString(input));
    }

    private static void stringShow(byte[] input){
        System.out.println(new String(input));
    }
}
