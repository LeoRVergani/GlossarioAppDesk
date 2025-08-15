package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import model.Termo;
import repository.GlossarioRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlossarioApp extends JFrame {

    private final GlossarioRepository repo;
    private List<Termo> glossarioData;
    private List<Termo> glossarioFiltrado;

    private JTextField buscaField;
    private JTable tabela;
    private TermoTableModel tableModel;

    public GlossarioApp() {
        repo = new GlossarioRepository();
        glossarioData = repo.carregarGlossario();
        glossarioFiltrado = new ArrayList<>(glossarioData);

        initUI();
        initEvents();
        filtrarTabela();
    }

    private void initUI() {
        setTitle("Glossário AppDesk - Service Desk");
        setSize(850, 650);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Theme.BACKGROUND);

        JLabel bannerLabel = new JLabel("Service Desk", SwingConstants.CENTER);
        bannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        bannerLabel.setForeground(Theme.TEXT_LIGHT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(Theme.PANEL);
        JLabel searchLabel = new JLabel("Buscar:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        searchLabel.setForeground(Theme.TEXT_LIGHT);
        buscaField = new JTextField(40);
        buscaField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        buscaField.setBorder(new EmptyBorder(15, 15, 15, 15));
        searchPanel.add(searchLabel);
        searchPanel.add(buscaField);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.PANEL);
        topPanel.add(bannerLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        tableModel = new TermoTableModel();
        tabela = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Theme.TABLE_ROW1 : Theme.TABLE_ROW2);
                    c.setForeground(Theme.TEXT_LIGHT);
                }
                return c;
            }

            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row > -1) {
                    return tableModel.getTermoAt(row).getDefinicao();
                }
                return null;
            }
        };
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tabela.setRowHeight(30);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        tabela.getTableHeader().setBackground(Theme.PANEL);
        tabela.getTableHeader().setForeground(Theme.TEXT_LIGHT);

        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        buttonPanel.setBackground(Theme.BACKGROUND);

        JButton addButton = createStyledButton("Adicionar");
        JButton editButton = createStyledButton("Editar");
        JButton removeButton = createStyledButton("Remover");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        addButton.addActionListener(e -> adicionarPalavra());
        editButton.addActionListener(e -> editarPalavra());
        removeButton.addActionListener(e -> removerPalavra());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void initEvents() {
        buscaField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrarTabela(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrarTabela(); }
        });

        tabela.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    mostrarJanelaLeitura();
                }
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Theme.BUTTON);
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(0, 55));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Theme.BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Theme.BUTTON);
            }
        });
        return btn;
    }

    private void filtrarTabela() {
        String texto = buscaField.getText().toLowerCase();
        if (texto.isEmpty()) {
            glossarioFiltrado = new ArrayList<>(glossarioData);
        } else {
            glossarioFiltrado = glossarioData.stream()
                    .filter(t -> t.getPalavra().toLowerCase().contains(texto)
                            || t.getDefinicao().toLowerCase().contains(texto))
                    .collect(Collectors.toList());
        }
        tableModel.fireTableDataChanged();
    }

    private void adicionarPalavra() {
        String palavra = JOptionPane.showInputDialog(this, "Digite a nova palavra:");
        if (palavra != null && !palavra.trim().isEmpty()) {
            String definicao = JOptionPane.showInputDialog(this, "Digite a definição:");
            if (definicao != null && !definicao.trim().isEmpty()) {
                glossarioData.add(new Termo(palavra.trim(), definicao.trim()));
                repo.salvarGlossario(glossarioData);
                filtrarTabela();
            }
        }
    }

    private void editarPalavra() {
        if (!verificarSenha()) return;

        int row = tabela.getSelectedRow();
        if (row == -1) return;

        Termo termo = tableModel.getTermoAt(row);
        String novaDef = JOptionPane.showInputDialog(this, "Nova definição:", termo.getDefinicao());
        if (novaDef != null && !novaDef.trim().isEmpty()) {
            termo.setDefinicao(novaDef.trim());
            repo.salvarGlossario(glossarioData);
            filtrarTabela();
        }
    }

    private void removerPalavra() {
        if (!verificarSenha()) return;

        int row = tabela.getSelectedRow();
        if (row == -1) return;

        Termo termo = tableModel.getTermoAt(row);
        int confirm = JOptionPane.showConfirmDialog(this, "Remover '" + termo.getPalavra() + "'?");
        if (confirm == JOptionPane.YES_OPTION) {
            glossarioData.remove(termo);
            repo.salvarGlossario(glossarioData);
            filtrarTabela();
        }
    }


    private void mostrarJanelaLeitura() {
        int row = tabela.getSelectedRow();
        if (row == -1) return;
        Termo termo = tableModel.getTermoAt(row);

        JDialog dialog = new JDialog(this, "Definição: " + termo.getPalavra(), true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(termo.getDefinicao());
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(Theme.BACKGROUND);
        area.setForeground(Theme.TEXT_LIGHT);
        area.setBorder(new EmptyBorder(10,10,10,10));

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    class TermoTableModel extends AbstractTableModel {
        private final String[] colunas = {"Palavra", "Definição"};

        @Override public int getRowCount() { return glossarioFiltrado.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int col) { return colunas[col]; }
        @Override public Object getValueAt(int row, int col) {
            Termo t = glossarioFiltrado.get(row);
            return col == 0 ? t.getPalavra() :
                    (t.getDefinicao().length() > 100
                            ? t.getDefinicao().substring(0, 100) + "..." : t.getDefinicao());
        }
        public Termo getTermoAt(int row) { return glossarioFiltrado.get(row); }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new GlossarioApp().setVisible(true));
    }

    private boolean verificarSenha() {
        // Criar diálogo modal
        JDialog dialog = new JDialog(this, "Senha necessária", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Label de instrução
        JLabel label = new JLabel("Digite a senha para continuar:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setBorder(new EmptyBorder(10, 10, 0, 10));
        dialog.add(label, BorderLayout.NORTH);

        // Campo de senha
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setBorder(new EmptyBorder(5, 10, 5, 10));
        dialog.add(passwordField, BorderLayout.CENTER);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final boolean[] senhaCorreta = {false};

        // Ação botão OK
        okButton.addActionListener(e -> {
            String senha = new String(passwordField.getPassword());
            if ("#.admin@sd123".equals(senha)) {
                senhaCorreta[0] = true;
            } else {
                JOptionPane.showMessageDialog(dialog, "Senha incorreta!", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            }
            dialog.dispose();
        });

        // Ação botão Cancelar
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
        return senhaCorreta[0];
    }

}

