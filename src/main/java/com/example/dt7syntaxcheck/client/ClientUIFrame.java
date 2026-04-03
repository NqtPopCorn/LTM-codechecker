package com.example.dt7syntaxcheck.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ClientUIFrame extends JFrame {
    private ClientService clientService;

    // Các thành phần UI
    private JComboBox<String> cbLanguage;
    private JButton btnUpload;
    private JButton btnCheck;
    private JButton btnClear;
    private JTextArea txtCodeInput;
    private JTextArea txtResult;
    private JLabel lblHeader;

    public ClientUIFrame() {
        initComponents();
        
        // Khởi tạo ClientService và bắt sự kiện nhận tin nhắn
        clientService = new ClientService(new CryptoManager() {
            @Override
            public void onMessageReceived(String message) {
                // Đẩy tác vụ cập nhật UI vào Event Dispatch Thread để tránh lỗi treo giao diện
                SwingUtilities.invokeLater(() -> {
                    txtResult.append(message + "\n");
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    txtResult.append("[-] Mất kết nối tới server.\n");
                });
            }
        });

        // Kết nối đến server socket
        clientService.connect("localhost", 5000);
    }

    private void initComponents() {
        setTitle("Code Syntax Checker Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ==========================================
        // 1. TOP PANEL: Chứa Tiêu đề, Chọn ngôn ngữ và Nút Upload
        // ==========================================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        lblHeader = new JLabel("Code Syntax Checker", SwingConstants.LEFT);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(lblHeader, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.add(new JLabel("Ngôn ngữ:"));
        
        String[] languages = {"Python", "Java", "C++", "JavaScript", "C#"};
        cbLanguage = new JComboBox<>(languages);
        controlsPanel.add(cbLanguage);

        btnUpload = new JButton("Upload File");
        btnUpload.addActionListener(this::btnUploadActionPerformed);
        controlsPanel.add(btnUpload);

        topPanel.add(controlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. MAIN PANEL: Chứa 3 vùng chính
        // ==========================================
        
        // 2.1 Code Input Area (Bên trái)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Nhập mã nguồn (Source Code)"));
        txtCodeInput = new JTextArea();
        txtCodeInput.setFont(new Font("Consolas", Font.PLAIN, 15));
        txtCodeInput.setTabSize(4);
        JScrollPane scrollInput = new JScrollPane(txtCodeInput);
        inputPanel.add(scrollInput, BorderLayout.CENTER);

        // 2.2 Execution Result Area (Bên phải)
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Kết quả thực thi (Execution Result)"));
        txtResult = new JTextArea();
        txtResult.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtResult.setEditable(false);
        txtResult.setBackground(new Color(245, 245, 245));
        JScrollPane scrollResult = new JScrollPane(txtResult);
        resultPanel.add(scrollResult, BorderLayout.CENTER);

        // Tách 2 vùng trái-phải
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, resultPanel);
        horizontalSplit.setResizeWeight(0.5); // Chia đều 50-50

        // 2.3 Error Log Area (Bên dưới)
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createTitledBorder("Lỗi cú pháp (Syntax Errors)"));
        errorPanel.setPreferredSize(new Dimension(0, 150)); // Chiều cao ban đầu
        JTextArea txtErrorLog = new JTextArea();
        txtErrorLog.setFont(new Font("Consolas", Font.BOLD, 14));
        txtErrorLog.setEditable(false);
        txtErrorLog.setForeground(Color.RED); // Chữ màu đỏ cho dễ thấy lỗi
        txtErrorLog.setBackground(new Color(255, 240, 240));
        JScrollPane scrollError = new JScrollPane(txtErrorLog);
        errorPanel.add(scrollError, BorderLayout.CENTER);

        // Tách phần trên (trái-phải) và phần dưới (lỗi)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, errorPanel);
        mainSplit.setResizeWeight(0.75); // 75% cho code/result, 25% cho error

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        centerWrapper.add(mainSplit, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // ==========================================
        // 3. BOTTOM PANEL: Chứa các nút chức năng
        // ==========================================
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        btnCheck = new JButton("Check Code");
        btnCheck.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheck.addActionListener(this::btnCheckActionPerformed);
        
        btnClear = new JButton("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        actionPanel.add(btnClear);
        actionPanel.add(btnCheck);
        
        add(actionPanel, BorderLayout.SOUTH);
        actionPanel.add(btnClear);

        add(actionPanel, BorderLayout.EAST);
    }

    // Xử lý sự kiện khi ấn nút Upload File
    private void btnUploadActionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Đọc toàn bộ nội dung file và nhét vào txtCodeInput
                String content = new String(Files.readAllBytes(selectedFile.toPath()));
                txtCodeInput.setText(content);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Xử lý sự kiện khi ấn nút Check Code
    private void btnCheckActionPerformed(ActionEvent evt) {
        String code = txtCodeInput.getText().trim();
        String language = (String) cbLanguage.getSelectedItem();
        
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập code hoặc upload file!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        txtResult.setText("Đang xử lý...\n");
        
        // TODO GIAI ĐOẠN SAU: 
        // Tạo đối tượng RequestPayload, gán language và code vào.
        // Dùng class CryptoManager mã hóa đối tượng này thành JSON/Byte array.
        // Gửi qua hàm clientService.send()
        
        // Tạm thời gửi một chuỗi string test để kiểm tra kết nối Socket:
        clientService.send("LANG:" + language + "\n" + code);
    }

    // Xử lý sự kiện khi ấn nút Clear
    private void btnClearActionPerformed(ActionEvent evt) {
        txtCodeInput.setText("");
        txtResult.setText("");
    }

    public static void main(String args[]) {
        // Thiết lập giao diện theo phong cách của Hệ điều hành đang dùng (Windows/macOS)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new ClientUIFrame().setVisible(true);
        });
    }
}