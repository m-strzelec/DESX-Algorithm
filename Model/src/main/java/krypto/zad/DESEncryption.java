package krypto.zad;

public class DESEncryption {

    private final Permutations v;
    private byte[] key, msg;
    private byte[][] subKeys;
    private byte[] leftSide, rightSide;

    DESEncryption() {
        this.v = new Permutations();
        makeMsgAndKey();
        generateSubkeys();
        leftSide = null;
        rightSide = null;
    }

    public byte[] getMsg(){
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public byte[] getKey(){
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public void run(boolean ifEncrypt) {
        // Permutacja poczatkowa
        msg = bitShuffle(msg, v.startPermutation);
        // Podzialenie bloku danych na czesc prawa i lewa
        divideMsg();
        proceedIterations(ifEncrypt);
    }

//
//     Glowna petla algorytmu. Przeksztalca prawy blok wiadomosci funkcja f,
//     a nastepnie laczy sie z lewym blokiem operacja XOR.
//     Petla wykonuje sie 16 razy po czym blok koncowy poddawany jest permtacji koncowej.
//     Funkcja f zawiera w sobie:
//      1. Permutacje rozszerzajaca - prawy blok danych z 32 bitow ma ich teraz 48
//      2. Operacja XOR z kluczem wygenerowanym dla danej iteracji
//      3. Kazde kolejne 6 bitow jest adresem do wartosci w SBoxach.
//         Wartosci te sa odczytywane i zamieniane na zapis dwojkowy
//      4. Wynik z SBoxow poddaje sie permutacji P (PBox).
//     Ostatecznie laczone zostaja polowy wiadomosci, ktore nastepnie poddawane sa permutacji koncowej
//     @param ifEncrypt true oznacza szyfrowanie wiadomosci, false jej odszyfrowanie
//
    private void proceedIterations(boolean ifEncrypt) {
        int iteration = subKeys.length;
        for (int i = 0; i < iteration; i++) {
            byte[] oldRightSide = rightSide;
            rightSide = bitShuffle(rightSide, v.extendedPermutation);
            if (ifEncrypt)
                rightSide = byteXOR(rightSide, subKeys[i]);
            else
                rightSide = byteXOR(rightSide, subKeys[iteration - i - 1]);
            rightSide = doSBox(rightSide); // 3.
            rightSide = bitShuffle(rightSide, v.pBox); // 4.
            rightSide = byteXOR(leftSide, rightSide);
            leftSide = oldRightSide;
        }
        // Laczenie czesci lewej i prawej wiadmosci w jedna calosc
        msg = byteConcat(rightSide, v.startPermutation.length/2, leftSide, v.startPermutation.length/2);
        //permutacja koncowa
        msg = bitShuffle(msg, v.endPermutation);
    }

//
//     Kazdy 6-bitowy fragment jest przeksztalcany przez jeden z osmiu S-BOXow.
//     W kazdym bajcie ignorowane sa dwa ostatnie bity (LSB)
//     Pierwszy i ostatni bit danych okresla wiersz, a pozostale bity kolumne S-BOXa.
//     Po odczytaniu dwoch kolejnych wartosci, liczby te sa zamieniane na ciag bitow i scalane.
//
    private byte[] doSBox(byte[] input) {
        input = byteSplit86(input);
        byte[] output = new byte[input.length / 2];

        for (int i = 0, firstSBoxValue = 0; i < input.length; i++) {
            byte sixBitsFragment = input[i];
            int rowNumb = 2 * (sixBitsFragment >> 7 & 0x0001) + (sixBitsFragment >> 2 & 0x0001);
            int columnNumb = sixBitsFragment >> 3 & 0x000F;
            int secondSBoxValue = v.sBox[64 * i + 16 * rowNumb + columnNumb];
            if (i % 2 == 0)
                firstSBoxValue = secondSBoxValue;
            else
                output[i / 2] = createByteFromSBoxValues(firstSBoxValue, secondSBoxValue);
        }
        return output;
    }


    // Po przekszalceniu liczb na system binarny scala ze soba te wartosci
    private byte createByteFromSBoxValues(int firstSBoxValue, int secondSBoxValue) {
        return (byte) (16 * firstSBoxValue + secondSBoxValue);
    }

    // operacje na byte

//
//     Tworzy 8 bajtowa tablice z 6 bajtowej.
//     Dwa ostatnie bity w kazdym bajcie sa bezuzyteczne.
//     Ma to na celu odseparowanie od siebie grup szesciobitywych potrzebnych przy operacjach z SBoxami
//
    private byte[] byteSplit86(byte[] input) {
        int bytesNumber = 8;
        boolean val;
        byte[] output = new byte[bytesNumber];
        for (int i = 0; i < bytesNumber; i++) {
            for (int j = 0; j < 6; j++) {
                val = bitCheck(input, (6 * i) + j);
                bitSet(output, (8 * i) + j, val);
            }
        }
        return output;
    }

    byte[] byteSplit(byte[] input, int index, int length) {
        boolean bit;
        byte[] output = prepareOutput(length);
        for (int i = 0; i < length; i++) {
            bit = bitCheck(input, index + i);
            bitSet(output, i, bit);
        }
        return output;
    }

    // Operacja XOR
    byte[] byteXOR(byte[] a, byte[] b) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    // laczy ze soba dwa byte w jedna calosc

    byte[] byteConcat(byte[] a, int aLength, byte[] b, int bLength) {
        boolean bit;
        byte[] output = prepareOutput(aLength + bLength);
        int i = 0;
        for (; i < aLength; i++) {
            bit = bitCheck(a, i);
            bitSet(output, i, bit);
        }
        for (int j = 0; j < bLength; j++, i++) {
            bit = bitCheck(b, j);
            bitSet(output, i, bit);
        }
        return output;
    }

    // Dzielenie wiadomosci na 2 czesci po 32 bity kazda

    private void divideMsg() {
        int bitNumber = (msg.length * 8) / 2;
        leftSide = byteSplit(msg, 0, bitNumber);
        rightSide = byteSplit(msg, bitNumber, bitNumber);
    }

    // Domyslny klucz oraz wiadmosc (po 64 bity)

    private void makeMsgAndKey() {
        msg = "aMessage".getBytes();
        key = "12345678".getBytes();
    }

//
//      Generowanie podkluczy:
//       1. Permutacja PC1 na kluczu pierwotnym.
//       2. Dane dzielone sa na dwa bloki - c i d.
//       3. Kazdy z blokow przesuwany jest w lewo.
//          Ilosc przesuniecia okreslona jest w tabeli.
//       4. Bloki sa ze soba z powrotem laczone i poddane permutacji PC2.
//       5. Wynikiem kazdej iteracji petli jest podklucz. Jego wartosc zostaje zapisana do tablicy subKeys.
//
    private void generateSubkeys(){
        byte[] keyPC1 = bitShuffle(key,v.PC1);
        byte[] c = byteSplit(keyPC1, 0, 28);
        byte[] d = byteSplit(keyPC1, 28, 28);
        byte[] cd;
        subKeys = new byte[v.shifts.length][];
        for (int i = 0; i < v.shifts.length; i++) {
            c = leftShift(c, v.shifts[i]);
            d = leftShift(d, v.shifts[i]);
            cd = byteConcat(c, 28, d, 28);
            subKeys[i] = bitShuffle(cd, v.PC2);
        }
    }

    // przesuniecie w lewo bitow o jedna lub dwie pozycje

    private byte[] leftShift(byte[] input, int shiftNumb) {
        byte[] out = new byte[4];
        int halfKeySize = 28;
        boolean bit;
        for (int i = 0; i < halfKeySize; i++) {
            bit = bitCheck(input, (i + shiftNumb) % halfKeySize);
            bitSet(out, i, bit);
        }
        return out;
    }

    // tworzenie byte[] o odpowiedniej dlugosci

    private byte[] prepareOutput(int length) {
        int bytesNumb = ((length - 1) / 8) + 1;
        return new byte[bytesNumb];
    }

    // operacje dla bitow

    private byte[] bitShuffle(byte[] input, int[] permTable) {
        byte[] output = prepareOutput(permTable.length);
        boolean bit;
        for (int i = 0; i < permTable.length; i++) {
            bit = bitCheck(input, permTable[i] - 1);
            bitSet(output, i, bit);
        }
        return output;
    }

    // sprawdzenie czy dany bit w tabilcy bajtow zostal juz ustawiony

    private boolean bitCheck(byte[] data, int index) {
        int Byte = index / 8;
        int Bit = index % 8;
        return (data[Byte] >> (8 - (Bit + 1)) & 1) == 1;
    }

    private void bitSet(byte[] data, int index, boolean bit) {
        int Byte = index / 8;
        int Bit = index % 8;
        if(bit) {
            data[Byte] |= 0x80 >> Bit;
        } else {
            data[Byte] &= ~(0x80 >> Bit);
        }
    }
}
