package com.example.dt7syntaxcheck.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.example.dt7syntaxcheck.share.RequestPayload;
import com.example.dt7syntaxcheck.share.ResponsePayload;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ClientUIFrame extends JFrame {

    private JComboBox<String> cbLanguage;
    private RSyntaxTextArea codeEditor;
    private JTextArea consoleOutput;
    private JButton btnUpload, btnCheck, btnClear, btnThemeToggle;

    private boolean isDarkMode = true;

    // Gọi lớp xử lý mạng và mã hóa
    private ClientService clientService = new ClientService();

    public ClientUIFrame() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        setTitle("Ứng Dụng Kiểm Tra & Thực Thi Code - Đề Tài 7");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
    }

    private void initComponents() {
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        String[] languages = {"Python", "Java", "C++", "JavaScript", "C#"};
        cbLanguage = new JComboBox<>(languages);

        btnUpload = new JButton("📁 Upload File");
        btnCheck = new JButton("▶ Check & Run");
        btnCheck.setBackground(new Color(40, 167, 69));
        btnCheck.setForeground(Color.WHITE);
        btnClear = new JButton("🗑 Clear");
        btnThemeToggle = new JButton("☀️ Light Mode");

        pnlToolbar.add(new JLabel("Ngôn ngữ:"));
        pnlToolbar.add(cbLanguage);
        pnlToolbar.add(btnUpload);
        pnlToolbar.add(Box.createHorizontalStrut(10));
        pnlToolbar.add(btnCheck);
        pnlToolbar.add(btnClear);
        pnlToolbar.add(Box.createHorizontalStrut(30));
        pnlToolbar.add(btnThemeToggle);

        add(pnlToolbar, BorderLayout.NORTH);

        codeEditor = new RSyntaxTextArea(20, 60);
        codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        codeEditor.setCodeFoldingEnabled(true);
        codeEditor.setFont(new Font("Consolas", Font.PLAIN, 16));

        RTextScrollPane spCode = new RTextScrollPane(codeEditor);

        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 14));
        consoleOutput.setForeground(new Color(200, 200, 200));
        consoleOutput.setBackground(new Color(30, 30, 30));

        JScrollPane spConsole = new JScrollPane(consoleOutput);
        spConsole.setBorder(BorderFactory.createTitledBorder("Kết quả / Lỗi"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spCode, spConsole);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // =========================================================
        // GẮN SỰ KIỆN CHO CÁC NÚT BẤM (ĐÂY LÀ PHẦN ĐÃ KHẮC PHỤC LỖI)
        // =========================================================
        btnUpload.addActionListener(e -> handleUploadFile());
        btnClear.addActionListener(e -> handleClear());
        btnCheck.addActionListener(e -> handleCheckCode());
        btnThemeToggle.addActionListener(e -> toggleTheme());

        cbLanguage.addActionListener(e -> {
            String selectedLang = (String) cbLanguage.getSelectedItem();
            switch (selectedLang) {
                case "Python":
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                    break;
                case "Java":
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                    break;
                case "C++":
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                    break;
                case "JavaScript":
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                    break;
                case "C#":
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
                    break;
                default:
                    codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            }
        });
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---
    private void handleClear() {
        codeEditor.setText("");
        consoleOutput.setText("");
    }

    private void handleUploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file mã nguồn");

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToUpload = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(fileToUpload.toPath());
                codeEditor.setText(content);
                consoleOutput.setText("Đã tải file: " + fileToUpload.getName() + " thành công.\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCheckCode() {
        String code = codeEditor.getText();
        if (code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập code hoặc upload file trước khi kiểm tra!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return; // Dừng lại, không gửi lên Server
        }

        String selectedLang = (String) cbLanguage.getSelectedItem();
        int langId = getLanguageId(selectedLang);

        consoleOutput.setText("Đang mã hóa và gửi code (" + selectedLang + ") lên Server...\n");
        btnCheck.setEnabled(false);

        RequestPayload payload = new RequestPayload(code, langId);

        SwingWorker<ResponsePayload, Void> worker = new SwingWorker<>() {
            @Override
            protected ResponsePayload doInBackground() throws Exception {
                return clientService.sendCodeToServer(payload);
            }

            @Override
            protected void done() {
                btnCheck.setEnabled(true);
                try {
                    ResponsePayload response = get();

                    if (response.isSuccess()) {
                        consoleOutput.append("\n=== KẾT QUẢ CHẠY THÀNH CÔNG ===\n");
                        consoleOutput.append(response.getOutput());

                        String formatted = response.getFormattedCode();
                        if (formatted != null && !formatted.isEmpty()) {
                            codeEditor.setText(formatted);
                            consoleOutput.append("\n\n[Hệ thống đã tự động Format lại code của bạn theo chuẩn]");
                        }
                    } else {
                        consoleOutput.append("\n=== PHÁT HIỆN LỖI CÚ PHÁP ===\n");
                        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                            for (var error : response.getErrors()) {
                                consoleOutput.append("-> Dòng " + error.getLine() + ": " + error.getMessage() + "\n");
                            }
                        } else {
                            consoleOutput.append(response.getOutput());
                        }
                    }
                } catch (Exception e) {
                    consoleOutput.append("\n[LỖI KẾT NỐI]: " + e.getMessage());
                    consoleOutput.append("\nHãy đảm bảo Server đang chạy ở port 5000!");
                }
            }
        };
        worker.execute();
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnThemeToggle.setText("☀️ Light Mode");
                consoleOutput.setForeground(new Color(200, 200, 200));
                consoleOutput.setBackground(new Color(30, 30, 30));
                applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnThemeToggle.setText("🌙 Dark Mode");
                consoleOutput.setForeground(Color.BLACK);
                consoleOutput.setBackground(new Color(245, 245, 245));
                applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml");
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi chuyển đổi giao diện!");
        }
    }

    private void applyEditorTheme(String themePath) {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(themePath));
            theme.apply(codeEditor);
        } catch (IOException ioe) {
            System.err.println("Không thể load theme: " + themePath);
        }
    }

    private int getLanguageId(String langName) {
        switch (langName) {
            case "C#":
                return 51;
            case "C++":
                return 54;
            case "Java":
                return 62;
            case "JavaScript":
                return 63;
            case "Python":
                return 71;
            default:
                return 71;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientUIFrame().setVisible(true);
        });
    }
}
