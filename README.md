# DESX Algorithm

## Introduction
DESX is an extended version of the Data Encryption Standard (DES) algorithm, designed to enhance its security by introducing additional key operations. The DES algorithm itself has been susceptible to brute-force attacks due to its relatively short key length (56 bits), so DESX was developed to add an extra layer of protection by using XOR operations before and after the standard DES encryption. This README explains the steps involved in the DESX encryption and decryption process.

## Steps in DESX Encryption

### 1. XOR Plaintext with First Key
- The plaintext is XORed with the first key (64-bit).
  
### 2. DES Encryption
- The XORed result is then encrypted using the DES algorithm with the second key (56-bit).
- The DES algorithm involves the following substeps:

#### 2.1 Divide Data into 64-bit Blocks
- The data is divided into blocks, each consisting of 64 bits.

#### 2.2 Initial Permutation (IP)
- Each block undergoes an initial permutation.

#### 2.3 Split into Two 32-bit Halves
- The block is then split into two halves: the left part and the right part, each 32 bits long.

#### 2.4 Perform 16 Cycles of Operations
- The following operations are performed for 16 rounds:
  - **Key Scheduling**: The key is shifted and 48 bits are selected from the 56-bit key for this round.
  - **Expansion**: The right half of the data is expanded from 32 bits to 48 bits using an expansion permutation.
  - **XOR with Key**: The expanded right half is XORed with the selected key bits.
  - **S-Box Substitution**: The result is divided into 8 blocks of 6 bits, which are then passed through S-boxes (substitution boxes). Each S-box maps the 6-bit input to a 4-bit output based on specific rules.
    - The first and last bits determine the row of the S-box.
    - The middle 4 bits determine the column of the S-box.
    - The value retrieved from the S-box is converted to binary.
  - **P-Box Permutation**: The resulting 32-bit output from the S-boxes is permuted using a P-box.
  - **XOR with Left Half**: The permuted result is XORed with the left half of the data.
  - **Swap**: The right half becomes the new left half, and the XORed result becomes the new right half.

#### 2.5 Combine the Halves
- After 16 rounds, the left and right halves are combined to form a 64-bit block.

#### 2.6 Final Permutation (FP)
- The combined block undergoes a final permutation to produce the DES-encrypted result.

### 3. XOR with Third Key
- Finally, the result is XORed with the third key (64-bit), yielding the DESX-encrypted data.

## DESX Decryption
Decryption in DESX is the reverse of the encryption process:
1. XOR the ciphertext with the third key.
2. Decrypt the result using the standard DES decryption algorithm with the second key.
3. XOR the resulting plaintext with the first key to retrieve the original message.

## Requirements
- Plaintext: 64-bit blocks.
- Keys:
  - First key: 64-bit.
  - Second key: 56-bit (DES key).
  - Third key: 64-bit.

## Benefits of DESX
- **Enhanced Security**: By adding XOR operations before and after the DES encryption, DESX provides extra protection against certain types of attacks, such as brute-force and differential cryptanalysis.
- **Key Extension**: DESX uses 184 bits of key material (64 + 56 + 64 bits), making it more secure than standard DES.
