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
        
        // Danh sách 5 ngôn ngữ hỗ trợ
        String[] languages = {"Python", "Java", "C++", "JavaScript", "C#"};
        cbLanguage = new JComboBox<>(languages);
        controlsPanel.add(cbLanguage);

        // Nút Upload File
        btnUpload = new JButton("Upload File");
        btnUpload.addActionListener(this::btnUploadActionPerformed);
        controlsPanel.add(btnUpload);

        topPanel.add(controlsPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER PANEL: Chứa vùng nhập code và vùng hiển thị kết quả
        // ==========================================
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6); // Chia tỷ lệ: 60% màn hình trên cho input, 40% dưới cho output

        // 2.1 Code Input Area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Nhập mã nguồn (Source Code)"));
        txtCodeInput = new JTextArea();
        txtCodeInput.setFont(new Font("Consolas", Font.PLAIN, 15)); // Font chữ dễ nhìn cho code
        // Cho phép ấn Tab ra khoảng trắng
        txtCodeInput.setTabSize(4);
        JScrollPane scrollInput = new JScrollPane(txtCodeInput);
        inputPanel.add(scrollInput, BorderLayout.CENTER);
        splitPane.setTopComponent(inputPanel);

        // 2.2 Result Area
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Kết quả (Output / Lỗi)"));
        txtResult = new JTextArea();
        txtResult.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtResult.setEditable(false); // Không cho người dùng gõ vào ô kết quả
        txtResult.setBackground(new Color(245, 245, 245));
        JScrollPane scrollResult = new JScrollPane(txtResult);
        resultPanel.add(scrollResult, BorderLayout.CENTER);
        splitPane.setBottomComponent(resultPanel);

        // Bọc splitPane lại để có khoảng cách (margin) đẹp hơn
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centerWrapper.add(splitPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // ==========================================
        // 3. RIGHT PANEL: Chứa các nút chức năng chính (Check, Clear)
        // ==========================================
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        btnCheck = new JButton("Check Code");
        btnCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheck.addActionListener(this::btnCheckActionPerformed);
        
        btnClear = new JButton("Clear");
        btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClear.addActionListener(this::btnClearActionPerformed);

        actionPanel.add(btnCheck);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Khoảng cách giữa 2 nút
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