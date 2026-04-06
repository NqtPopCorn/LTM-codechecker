package com.example.dt7syntaxcheck.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

    private boolean isDarkMode = true; // Mặc định mở lên là Dark Mode

    // Khởi tạo service xử lý mạng và mã hóa
    private ClientService clientService = new ClientService();

    public ClientUIFrame() {
        // Thiết lập giao diện mặc định là Dark Mode khi khởi động
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

        // Load theme tối cho khung code editor lần đầu tiên
        applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");
    }

    private void initComponents() {
        // --- 1. THANH CÔNG CỤ (TOP) ---
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        String[] languages = {"Python", "Java", "C++", "JavaScript", "C#"};
        cbLanguage = new JComboBox<>(languages);

        //Set tô màu cho từng ngôn ngữ:))
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
        btnUpload = new JButton("📁 Upload File");

        btnCheck = new JButton("▶ Check & Run");
        btnCheck.setBackground(new Color(40, 167, 69));
        btnCheck.setForeground(Color.WHITE);

        btnClear = new JButton("🗑 Clear");

        // Nút chuyển đổi Dark/Light mode
        btnThemeToggle = new JButton("☀️ Light Mode");
        btnThemeToggle.addActionListener(e -> toggleTheme());

        pnlToolbar.add(new JLabel("Ngôn ngữ:"));
        pnlToolbar.add(cbLanguage);
        pnlToolbar.add(btnUpload);
        pnlToolbar.add(Box.createHorizontalStrut(10));
        pnlToolbar.add(btnCheck);
        pnlToolbar.add(btnClear);
        pnlToolbar.add(Box.createHorizontalStrut(30)); // Đẩy nút theme sang phải một chút
        pnlToolbar.add(btnThemeToggle);

        add(pnlToolbar, BorderLayout.NORTH);

        // --- 2. KHU VỰC NHẬP CODE (EDITOR) ---
        codeEditor = new RSyntaxTextArea(20, 60);
        //
        codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        codeEditor.setCodeFoldingEnabled(true);
        codeEditor.setFont(new Font("Consolas", Font.PLAIN, 16));

        RTextScrollPane spCode = new RTextScrollPane(codeEditor);

        // --- 3. KHU VỰC KẾT QUẢ (CONSOLE LOG) ---
        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 14));
        // Đặt màu mặc định cho console khi ở chế độ Dark Mode
        consoleOutput.setForeground(new Color(200, 200, 200));
        consoleOutput.setBackground(new Color(30, 30, 30));

        JScrollPane spConsole = new JScrollPane(consoleOutput);
        spConsole.setBorder(BorderFactory.createTitledBorder("Kết quả / Lỗi"));

        // --- 4. GỘP VÀO SPLIT PANE ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spCode, spConsole);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- HÀM XỬ LÝ ĐỔI THEME ---
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                // Chuyển sang giao diện Tối
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnThemeToggle.setText("☀️ Light Mode");

                // Đổi màu Console
                consoleOutput.setForeground(new Color(200, 200, 200));
                consoleOutput.setBackground(new Color(30, 30, 30));

                // Đổi màu Code Editor sang Dark (Monokai)
                applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml");

            } else {
                // Chuyển sang giao diện Sáng
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnThemeToggle.setText("🌙 Dark Mode");

                // Đổi màu Console
                consoleOutput.setForeground(Color.BLACK);
                consoleOutput.setBackground(new Color(245, 245, 245));

                // Đổi màu Code Editor sang Light (Eclipse)
                applyEditorTheme("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml");
            }

            // Lệnh này bắt buộc phải có để Swing vẽ lại toàn bộ giao diện sau khi đổi LookAndFeel
            SwingUtilities.updateComponentTreeUI(this);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi chuyển đổi giao diện!");
        }
    }

    // Hàm load file XML theme của thư viện RSyntaxTextArea
    private void applyEditorTheme(String themePath) {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(themePath));
            theme.apply(codeEditor);
        } catch (IOException ioe) {
            System.err.println("Không thể load theme cho code editor: " + themePath);
        }
    }

    private void handleCheckCode() {
        // 1. Lấy code từ khung Editor
        String code = codeEditor.getText();
        if (code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập code hoặc upload file trước khi kiểm tra!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Lấy ngôn ngữ đang chọn và map sang ID
        String selectedLang = (String) cbLanguage.getSelectedItem();
        int langId = getLanguageId(selectedLang);

        consoleOutput.setText("Đang mã hóa và gửi code (" + selectedLang + ") lên Server...\n");
        btnCheck.setEnabled(false); // Khóa nút bấm để tránh người dùng spam click

        // 3. Đóng gói code vào RequestPayload
        RequestPayload payload = new RequestPayload(code, langId);

        // 4. Sử dụng SwingWorker để gửi qua Socket ở luồng nền (tránh đơ UI)
        SwingWorker<ResponsePayload, Void> worker = new SwingWorker<>() {
            @Override
            protected ResponsePayload doInBackground() throws Exception {
                // Gọi ClientService: Hàm này sẽ tự động mã hóa và gửi qua TCP Socket
                return clientService.sendCodeToServer(payload);
            }

            @Override
            protected void done() {
                btnCheck.setEnabled(true); // Mở khóa nút bấm
                try {
                    // Lấy kết quả trả về từ Server (đã được giải mã)
                    ResponsePayload response = get();

                    if (response.isSuccess()) {
                        // TRƯỜNG HỢP 1: CODE ĐÚNG SYNTAX
                        consoleOutput.append("\n=== KẾT QUẢ CHẠY THÀNH CÔNG ===\n");
                        consoleOutput.append(response.getOutput());

                        // Đề bài yêu cầu: format đúng chuẩn nếu code đúng syntax
                        String formatted = response.getFormattedCode();
                        if (formatted != null && !formatted.isEmpty()) {
                            codeEditor.setText(formatted);
                            consoleOutput.append("\n\n[Hệ thống đã tự động Format lại code của bạn theo chuẩn]");
                        }
                    } else {
                        // TRƯỜNG HỢP 2: CODE SAI SYNTAX
                        consoleOutput.append("\n=== PHÁT HIỆN LỖI CÚ PHÁP ===\n");
                        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                            for (var error : response.getErrors()) {
                                consoleOutput.append("-> Dòng " + error.getLine() + ": " + error.getMessage() + "\n");
                                // Tương lai: Có thể nối thêm "Gợi ý sửa lỗi" từ Server vào đây
                            }
                        } else {
                            // Nếu không parse được list lỗi, in luôn chuỗi lỗi gốc
                            consoleOutput.append(response.getOutput());
                        }
                    }
                } catch (Exception e) {
                    // Bắt lỗi mất kết nối hoặc Server chưa mở
                    consoleOutput.append("\n[LỖI KẾT NỐI]: " + e.getMessage());
                    consoleOutput.append("\nHãy đảm bảo Server đang chạy ở port 5000!");
                }
            }
        };

        worker.execute(); // Bắt đầu chạy luồng nền
    }

    // Map tên ngôn ngữ sang ID (Giả sử dùng chuẩn ID của Judge0 API)
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
                return 71; // Mặc định là Python
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientUIFrame().setVisible(true);
        });
    }
}
