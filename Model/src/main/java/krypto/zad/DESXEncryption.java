package krypto.zad;

public class DESXEncryption extends DESEncryption {
    private final DESEncryption des;
    private byte[] firstKey, secondKey, msg;
    private byte[][] msgArray;

    private int counter;

    public byte[] getFirstKey() {
        return firstKey;
    }

    public byte[] getSecondKey() {
        return secondKey;
    }

    public void setFirstKey(byte[] firstKey) {
        this.firstKey = firstKey;
    }

    public void setSecondKey(byte[] secondKey) {
        this.secondKey = secondKey;
    }

    public byte[] getMsg(){
        return msg;
    }

    public void setCounter(boolean check) {
        if (check)
            this.counter = 8 - (msg.length % 8);
    }

    public DESXEncryption() {
        des = new DESEncryption();
        msg = des.getMsg();
        generateDefaultKeys();
    }

    // Zapisywanie nowego msg na 2 sposoby

    public void setMsg(byte[] msg){
        des.setMsg(msg);
        this.msg = des.getMsg();
    }

    // wywolanie szyfrowania / deszyfrowania zaleznie od wartosci ifEncrypt

    public void run(boolean ifEncrypt) {
        setCounter(ifEncrypt);
        msgDivide(ifEncrypt);
        if (ifEncrypt) {
            encrypt();
        } else {
            decrypt();
        }
        msgConcat();
    }

    // Dzielimy wiadomosc na bloki po 64 bity

    private void msgDivide(boolean ifEncrypt) {
        if (ifEncrypt) {
            byte[] tab = new byte[msg.length + 8 - (msg.length % 8)];
            System.arraycopy(msg, 0, tab, 0, msg.length);
            for (int i = 0; i < counter; i++)
                tab[msg.length + i] = 0;
            setMsg(tab);
        }
        int partsNumber = ((msg.length-1) / 8) + 1;
        msgArray = new byte[partsNumber][];
        for (int i = 0; i < partsNumber; i++) {
            msgArray[i] = des.byteSplit(msg, i*8*8,64);
        }
    }

    public void deleteZeros() {
        byte[] tab = new byte[msg.length - counter];
        System.arraycopy(msg, 0, tab, 0, msg.length - counter);
        setMsg(tab);
    }

    // Laczymy bloki w jedna wiadomosc

    private void msgConcat() {
        msg = msgArray[0];
        if(msgArray.length > 1){
            for (int i = 1; i < msgArray.length; i++) {
                msg = des.byteConcat(msg, msg.length*8, msgArray[i], msgArray[i].length*8);
            }
        }
    }

//
//     Dzialanie DESX:
//     1. Blok wiadomosci XOR-uje sie z pierwsza czescia klucza DESX-a.
//     2. Blok danych powstaly w pkt. 1 szyfruje sie za pomoca DES-a kluczem 56-bitowym.
//     3. Blok danych powstaly w pkt. 2 XOR-uje sie z druga czescia klucza DESX-a.
//

    private void encrypt() {
        for (int i = 0; i < msgArray.length; i++) {
            msgArray[i] = des.byteXOR(msgArray[i], firstKey); // 1.
            des.setMsg(msgArray[i]);
            des.run(true); // 2.
            msgArray[i] = des.getMsg();
            msgArray[i] = des.byteXOR(secondKey, msgArray[i]); // 3.
        }
    }

// Deszyfrowanie w odwrotnej kolejnosci jak szyfrowanie

    private void decrypt() {
        for (int i = 0; i < msgArray.length; i++) {
            msgArray[i] = des.byteXOR(msgArray[i], secondKey); // 3.
            des.setMsg(msgArray[i]);
            des.run(false); // 2.
            msgArray[i] = des.getMsg();
            msgArray[i] = des.byteXOR(firstKey, msgArray[i]); // 1.
        }
    }

    // podstawowe klucze dlugosci 64 bitow, mozliwa zmiana

    private void generateDefaultKeys() {
        firstKey = "qwertyui".getBytes();
        secondKey = "asdfghjk".getBytes();
    }
}
