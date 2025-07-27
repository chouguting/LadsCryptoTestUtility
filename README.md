# LaDS自動化密碼學檢測軟體  LaDS CryptoTest Utility
==============================
<br>
## 簡介
這是一款使用Compose Multiplatform開發的一套自動化密碼學硬體測試軟體。利用序列通訊埠和硬體溝通，就能驗證各種密碼硬體的運算結果。支援的演算法包括 AES、SHA、DRBG、RSA、ECDSA 以及 Kyber 和 Dilithium 等後量子演算法。
<img width="2596" height="849" alt="image" src="https://github.com/user-attachments/assets/9cb493cd-a47e-4d9b-8c6f-17d7c6b0eb5a" />


## 功能介紹
### 密碼學答案產生器
輸入一些題目檔(例如明文和密鑰)，再利用現成的Java/C++模組去做加解密，確保產生出來的答案(例如密文)是正確的，可以用來作為驗證硬體的Golden Reference
<img width="1897" height="1061" alt="image" src="https://github.com/user-attachments/assets/897fbcb3-a0df-4d41-990b-adee74a013b8" />



### 自動化硬體驗證工具
自動地解析題目檔，將題目檔的內容利用Serial Port送到硬體(FPGA或MCU)中去計算，再將硬體計算的結果與Golden Reference去比對，若有計算錯誤，系統會告訴使用者錯在哪。
<img width="3611" height="1238" alt="image" src="https://github.com/user-attachments/assets/9bc7629a-3844-41f7-b264-f0544ec05ba3" />

### 後量子密碼學實驗室
針對比較新的後量自子密碼學演算法(例如Kyber)，我們提供一個可以可以直接做加解密的GUI介面，讓使用者能測試不同設定下的加解密(或簽章)結果
<img width="1897" height="1060" alt="image" src="https://github.com/user-attachments/assets/a051a260-47f9-4f4b-9676-fa43cad0298a" />


## 支援的演算法
* AES
* SHA 2
* SHA 3
* SHAKE
* DRBG 
* ECDSA
* RSA
* Kyber



