import cipers.AESEngine;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class MainFrame extends JFrame implements ActionListener {

    JPanel buttonPanel;
    JButton loadFileButton;
    JButton loadDirButton;
    JButton saveToDirButton;

    JButton convertToJsonButton;
    JButton doAESToJsonButton;

    JButton doAESToRspButton;


    JList<String> loadedFileJList = new JList<>(new DefaultListModel());
    JList<String> convertedFileJList = new JList<>(new DefaultListModel());


    ArrayList<String> currentSelectedFileList = new ArrayList<>();
    HashMap<String, String> loadedFilesAndItsPath = new HashMap<>();

    ArrayList<String> currentConvertedFileList = new ArrayList<>();


    String saveToThisDirectory;
    JLabel saveToDirectoryLabel;


    public MainFrame() throws HeadlessException {
        //this代表這個JFrame物件
//        this.setSize(500,500);//設定框架的寬和高
        this.setTitle("CAVP TEST");//設定框架左上角的標題
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//離開就關掉
        this.setResizable(true);  //框架縮放

//        ImageIcon image=new ImageIcon("src/L01_frames/images/smile.png"); //創建一個ICON
//        this.setIconImage(image.getImage()); //改變框架左上角的ICON (LOGO)
        this.setPreferredSize(new Dimension(1280, 720));
        this.setLocationRelativeTo(null);  //設定出現在畫面正中央

        ImageIcon squareLogoImage = null;
        BufferedImage logoImage = null;
        try {
            squareLogoImage = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("images/LaDS_logo_square.png")));
            logoImage = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("resources/images/LaDS_logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setIconImage(squareLogoImage.getImage());


        JPanel topBar = new JPanel();
        topBar.setLayout(new GridLayout(1, 2));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

        //buttons
        loadDirButton = new JButton("LOAD dir");
        loadDirButton.setToolTipText("load directory");
        loadDirButton.setPreferredSize(new Dimension(80, 40));
        loadDirButton.setMargin(new Insets(0, 0, 0, 0));
        loadDirButton.setFocusPainted(false);
        loadDirButton.addActionListener(this);
        buttonPanel.add(loadDirButton);

        loadFileButton = new JButton("LOAD files");
        loadFileButton.setToolTipText("load file");
        loadFileButton.setPreferredSize(new Dimension(80, 40));
        loadFileButton.setMargin(new Insets(0, 0, 0, 0));
        loadFileButton.setFocusPainted(false);
        loadFileButton.addActionListener(this);
        buttonPanel.add(loadFileButton);

        saveToDirButton = new JButton("SAVE to");
        saveToDirButton.setToolTipText("save file(s) to this directory");
        saveToDirButton.setPreferredSize(new Dimension(80, 40));
        saveToDirButton.setMargin(new Insets(0, 0, 0, 0));
        saveToDirButton.setFocusPainted(false);
        saveToDirButton.addActionListener(this);
        buttonPanel.add(saveToDirButton);

        try {
            saveToThisDirectory = new File(".").getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveToDirectoryLabel = new JLabel();
        saveToDirectoryLabel.setText(saveToThisDirectory);

        buttonPanel.add(saveToDirectoryLabel);


        BufferedImage finalLogoImage = logoImage;
        topBar.add(new JPanel() {  //paint logo
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(finalLogoImage, 0, 0, this);
            }
        });
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
        topBar.add(buttonPanel);
        topBar.setPreferredSize(new Dimension(0, 120));
        this.add(topBar, BorderLayout.NORTH);


        //middle content
        JPanel middleSection = new JPanel();
        middleSection.setLayout(new GridLayout(1, 3, 10, 10));

        //middle left : lists
        loadedFileJList.setFont(new Font("Consolas", Font.BOLD, 18));
        JScrollPane inputFileScrollPane = new JScrollPane(); //add scroll support
        inputFileScrollPane.setViewportView(loadedFileJList);
        inputFileScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel middleLeftPart = new JPanel(new BorderLayout());
        middleLeftPart.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
        middleLeftPart.add(inputFileScrollPane, BorderLayout.CENTER);

        //middle center: buttons
        JPanel middleCenterPart = new JPanel();

        middleCenterPart.setLayout(new FlowLayout(FlowLayout.CENTER));
        middleCenterPart.setBorder(BorderFactory.createEmptyBorder(200, 30, 30, 0));
        convertToJsonButton = new JButton("JSON");
        convertToJsonButton.setToolTipText("Convert to JSON");
        convertToJsonButton.setPreferredSize(new Dimension(80, 40));
        convertToJsonButton.setFocusPainted(false);
        convertToJsonButton.addActionListener(this);
        middleCenterPart.add(convertToJsonButton);

        doAESToJsonButton = new JButton("AES(to JSON)");
        doAESToJsonButton.setToolTipText("do AES task(Enc or Dec) and save to JSON");
        doAESToJsonButton.setPreferredSize(new Dimension(120, 40));
        doAESToJsonButton.setFocusPainted(false);
        doAESToJsonButton.addActionListener(this);
        middleCenterPart.add(doAESToJsonButton);


        doAESToRspButton = new JButton("AES(to Rsp)");
        doAESToRspButton.setToolTipText("do AES task(Enc or Dec) and save to Rsp");
        doAESToRspButton.setPreferredSize(new Dimension(120, 40));
        doAESToRspButton.setFocusPainted(false);
        doAESToRspButton.addActionListener(this);
        middleCenterPart.add(doAESToRspButton);

//        middleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//        middleCenterPart.add(middleLabel);

//        middleCenterPart.add(new Button("AAA"));


        //middle right: lists
        convertedFileJList.setFont(new Font("Consolas", Font.BOLD, 18));
        JScrollPane convertedFileScrollPane = new JScrollPane(); //add scroll support
        convertedFileScrollPane.setViewportView(convertedFileJList);
        convertedFileScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel middleRightPart = new JPanel(new BorderLayout());
        middleRightPart.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 30));
        middleRightPart.add(convertedFileScrollPane, BorderLayout.CENTER);


        middleSection.add(middleLeftPart);
        middleSection.add(middleCenterPart);
        middleSection.add(middleRightPart);

        this.add(middleSection, BorderLayout.CENTER);


        this.pack();
        this.setVisible(true);//讓frame可以被看見
        this.setLocationRelativeTo(null);  //設定出現在畫面正中央

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == loadDirButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("Choose a directory");
            fileChooser.setAcceptAllFileFilterUsed(false);  //disable all file type option
            int userSelection = fileChooser.showOpenDialog(new JFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String selectedDir = fileChooser.getSelectedFile().toString();
                currentSelectedFileList = Utils.findSupportedFilesInDir(selectedDir);
                for (String filename : currentSelectedFileList) {
                    loadedFilesAndItsPath.put(filename, selectedDir + "\\" + filename);
                }
                loadedFileJList.setListData(currentSelectedFileList.toArray(new String[currentSelectedFileList.size()]));
            }
        }

        if (e.getSource() == loadFileButton) {
//            fileChooser.setFileFilter(new FileNameExtensionFilter("request file (*.req)", "req"));
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "*.req, *.json", "req","json");

            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Choose files");
            int userSelection = fileChooser.showOpenDialog(new JFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                ArrayList<String> selectedFilePath = new ArrayList<>();
                for (File file : selectedFiles) {
                    if (loadedFilesAndItsPath.containsKey(file.getName())) {
                        JOptionPane.showMessageDialog(this, file.getName() + " already exists", "Duplicate File(s)", JOptionPane.ERROR_MESSAGE);
                    } else {
                        currentSelectedFileList.add(file.getName());
                        loadedFilesAndItsPath.put(file.getName(), Path.of(file.getAbsolutePath()).toString());
                    }

                }

                loadedFileJList.setListData(currentSelectedFileList.toArray(new String[currentSelectedFileList.size()]));
//                middleLabel.setText("HHHH");

            }
        }

        if (e.getSource() == saveToDirButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("Choose a directory");
            fileChooser.setAcceptAllFileFilterUsed(false);  //disable all file type option
            int userSelection = fileChooser.showOpenDialog(new JFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String selectedDir = fileChooser.getSelectedFile().toString();
                saveToThisDirectory = selectedDir;
                saveToDirectoryLabel.setText(saveToThisDirectory);
            }

        }

        if (e.getSource() == convertToJsonButton) {
            int[] selectedIndex = loadedFileJList.getSelectedIndices();
            currentConvertedFileList = new ArrayList<>();
            for (int index : selectedIndex) {

                String filename = currentSelectedFileList.get(index);
                String filenameWithoutFileExtension = filename.trim().split("\\.", 2)[0];
                String newFilename = filenameWithoutFileExtension + ".json";
                if (filename.trim().endsWith(".req")) {
                    String filepath = loadedFilesAndItsPath.get(filename);
                    JSONObject jsonobject = JsonUtils.reqFileToJson(filepath);
                    JsonUtils.saveJsonToFile(saveToThisDirectory, newFilename, jsonobject);
                    currentConvertedFileList.add(newFilename);
                } else if (filename.trim().endsWith(".json")) {
                    String filepath = loadedFilesAndItsPath.get(filename);
                    //if the file is json, just copy it
                    try {
                        String content = Files.readString(Path.of(filepath), StandardCharsets.UTF_8);
                        FileWriter fileWriter = new FileWriter(saveToThisDirectory+"\\"+newFilename);
                        fileWriter.write(content);
                        fileWriter.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    currentConvertedFileList.add(newFilename);
                }
            }
            convertedFileJList.setListData(currentConvertedFileList.toArray(new String[currentConvertedFileList.size()]));
        }

        if(e.getSource()==doAESToJsonButton || e.getSource()==doAESToRspButton){
            System.out.println("do AES");
            int[] selectedIndex = loadedFileJList.getSelectedIndices(); //get selected index
            currentConvertedFileList = new ArrayList<>(); //clear current converted file list
            for (int index : selectedIndex) {
                String filename = currentSelectedFileList.get(index); //get filename
                String filenameWithoutFileExtension = filename.trim().split("\\.", 2)[0]; //get filename without extension
                JSONObject jsonobject = null;
                if (filename.trim().endsWith(".req")) {
                    String filepath = loadedFilesAndItsPath.get(filename);
                    jsonobject = JsonUtils.reqFileToJson(filepath);
                } else if (filename.trim().endsWith(".json")) {
                    String filepath = loadedFilesAndItsPath.get(filename);
                    try {
                        String content = Files.readString(Path.of(filepath), StandardCharsets.UTF_8);
                        jsonobject = new JSONObject(content);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                }
                JSONObject resultJson = AESEngine.runAESWithJson(jsonobject); //run AES
                String newFilename = "";
                if(e.getSource()==doAESToJsonButton){
                    newFilename = filenameWithoutFileExtension+ "_result" + ".json"; //new file extension is json
                    JsonUtils.saveJsonToFile(saveToThisDirectory, newFilename, resultJson); //save to file
                }else if(e.getSource()==doAESToRspButton){
                    newFilename = filenameWithoutFileExtension + ".rsp"; //new file extension is rsp
                    JsonUtils.saveJsonToRspFile(saveToThisDirectory, newFilename, resultJson); //save to rsp file
                }
                //update converted file list
                currentConvertedFileList.add(newFilename);
                convertedFileJList.setListData(currentConvertedFileList.toArray(new String[currentConvertedFileList.size()]));

            }

        }
    }
}
