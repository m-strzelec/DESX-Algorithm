package krypto.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import krypto.zad.DESXEncryption;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class Controller extends Component {
    @FXML
    private RadioButton rbFile;
    @FXML
    private TextArea taConsole;
    @FXML
    private TextArea taMessage;
    @FXML
    private TextArea taMessageCipher;
    private static final DESXEncryption desx = new DESXEncryption();
    private byte[] plainMsg;
    private byte[] encrypted;
    private byte[] decrypted;

    // funkcja tworzaca ciag alfanumeryczny
    public String getAlphaNumericString(int n)
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    @FXML
    public void generateText() {
        taMessage.setText(getAlphaNumericString(20));
    }

    // generowanie kluczy za pomoca funkcji alfanumerycznej
    @FXML
    public void generateKeys() {
        desx.setKey(getAlphaNumericString(8).getBytes());
        desx.setFirstKey(getAlphaNumericString(8).getBytes());
        desx.setSecondKey(getAlphaNumericString(8).getBytes());
        taConsole.setText("Wygenerowano klucze: \nDES key: " + new String(desx.getKey())
                + "\nFirst DESX key: " + new String(desx.getFirstKey())
                + "\nSecond DESX key: " + new String(desx.getSecondKey()));
    }

    // szyfrowanie wiadomosci z pola tekstowego
    @FXML
    public void cipherText() {
        if(taMessage.getText().isEmpty())
            taConsole.setText("Wiadomość jest pusta!");
        else {
            // ustawienie wiadomosci do szyfrowania na wartosc z pola tekstowego
            if (rbFile.isSelected()) {
                if (plainMsg == null)
                    plainMsg = taMessage.getText().getBytes();
                desx.setMsg(plainMsg);
            }
            else
                desx.setMsg(taMessage.getText().getBytes());

            desx.run(true);
            encrypted = bytesToHex(desx.getMsg()).getBytes();
            // zamiana byte na hex + wyswietlenie zaszyfrowaniej wiadomosci w 2 polu tekstowym
            taMessageCipher.setText(new String(encrypted));
            taConsole.setText("Zaszyfrowano wiadomość!");
        }
    }

    // deszyfrowanie wiadomosci z pola tekstowego
    @FXML
    public void decipherText() {
        if(taMessageCipher.getText().isEmpty())
            taConsole.setText("Wiadomość jest pusta!");
        else {
            // zamiana hex na byte 2 pola tekstowego i ustawienie jako wiadomosc
            if (rbFile.isSelected())
                desx.setMsg(hexToBytes(new String(encrypted)));
            else
                desx.setMsg(hexToBytes(taMessageCipher.getText()));
            desx.run(false);
            desx.deleteZeros();
            plainMsg = desx.getMsg();
            // wypisanie rozszyfrowanej wiadomosci w 1 polu tekstowym
            taMessage.setText(new String(plainMsg));
            taConsole.setText("Zdeszyfrowano wiadomość!");
        }
    }

    // wczytanie pliku jawnego
    @FXML
    public void loadFromFile() throws IOException {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            FileInputStream fis = new FileInputStream(file);
            int fileSize = fis.available();
            byte[] fileContent = new byte[fileSize];
            // odczyt zawartosci z pliku do fileContent + przypisanie do 1 pola tekstowego
            fis.read(fileContent);
            fis.close();
            plainMsg = fileContent;
            taMessage.setText(new String(plainMsg));
            taConsole.setText("Udało się wczytać tekst jawny z pliku: "+jfc.getSelectedFile().getAbsolutePath());
        }
    }

    // wczytanie kryptoramu z pliku
    @FXML
    public void loadEncryptedFile() throws IOException {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            FileInputStream fis = new FileInputStream(file);
            int fileSize = fis.available();
            byte[] fileContent = new byte[fileSize];
            // odczyt zawartosci z pliku do fileContent + przypisanie do 2 pola tekstowego
            fis.read(fileContent);
            fis.close();
            encrypted = fileContent;
            taMessageCipher.setText(new String(encrypted));
            taConsole.setText("Udało się wczytać kryptogram z pliku: "+jfc.getSelectedFile().getAbsolutePath());
        }
    }

    // zapis tekstu jawnego do pliku
    @FXML
    public void savePlainMessage() throws IOException {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            FileOutputStream fos = new FileOutputStream(file);
            // zapis zawartosci 1 pola tekstowego do pliku
            if (rbFile.isSelected())
                fos.write(plainMsg);
            else
                fos.write(taMessage.getText().getBytes());
            fos.close();
            taConsole.setText("Udało się zapisać tekst jawny do pliku: "+Path.of(file.getPath()));
        }
    }

    // zapis kryptogramu do pliku
    @FXML
    public void saveEncryptedMessage() throws IOException {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            FileOutputStream fos = new FileOutputStream(file);
            // zapis zawartosci 2 pola tekstowego do pliku
            //fos.write(encrypted);
            if (rbFile.isSelected())
                fos.write(encrypted);
            else
                fos.write(taMessageCipher.getText().getBytes());
            fos.close();
            taConsole.setText("Udało się zapisać kryptogram do pliku: "+Path.of(file.getPath()));
        }
    }

    //konwertuje tablicę bajtów na ciąg znaków w systemie heksadecymalnym
    public static String bytesToHex(byte[] bytes)
    {
        StringBuilder hexText = new StringBuilder();
        String initialHex;
        int initHexLength;
        for (int i = 0; i < bytes.length; i++)
        {
            // zamiana byte na hex
            int positiveValue = bytes[i] & 0x000000FF;
            initialHex = Integer.toHexString(positiveValue);
            initHexLength = initialHex.length();
            while (initHexLength++ < 2)
            {
                hexText.append("0");
            }
            hexText.append(initialHex);
        }
        return hexText.toString().toUpperCase();
    }

    //konwertuje ciąg znaków w systemie heksadecymalnym na tablicę bajtów
    public static byte[] hexToBytes(String tekst)
    {
        // sprawdzenie czy wczytana zawartosc jest hex
        if (tekst == null) {
            return null;
        }
        else if (tekst.length() < 2) {
            return null;
        }
        else {
            // konwersja na byte
            if (tekst.length()%2!=0) {
                tekst+='0';
            }
            int dl = tekst.length() / 2;
            byte[] wynik = new byte[dl];
            for (int i = 0; i < dl; i++)
            { try{
                wynik[i] = (byte) Integer.parseInt(tekst.substring(i * 2, i * 2 + 2), 16);
            }catch(NumberFormatException e){JOptionPane.showMessageDialog(null,
                    "Problem z przekonwertowaniem HEX->BYTE.\n Sprawdź wprowadzone dane.",
                    "Problem z przekonwertowaniem HEX->BYTE", JOptionPane.ERROR_MESSAGE); }
            }
            return wynik;
        }
    }
}
