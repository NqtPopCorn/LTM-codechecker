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
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ClientUIFrame extends JFrame {

    private JComboBox<String> cbLanguage;
    private RSyntaxTextArea codeEditor;
    private JTextArea consoleOutput;
    private JButton btnUpload, btnCheck, btnClear, btnThemeToggle;
    
    private boolean isDarkMode = true; // Mặc định mở lên là Dark Mode

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
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientUIFrame().setVisible(true);
        });
    }
}