package repository;

import model.Termo;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GlossarioRepository {
    private static final String FILE_PATH = "C:/Glossario/glossario.csv";

    public List<Termo> carregarGlossario() {
        List<Termo> termos = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            salvarGlossario(termos); // cria vazio
            return termos;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            br.readLine(); // cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] values = line.split(",", 2);
                    if (values.length == 2) {
                        termos.add(new Termo(unquote(values[0]), unquote(values[1])));
                    }
                }
            }
            termos.sort(Comparator.comparing(Termo::getPalavra, String.CASE_INSENSITIVE_ORDER));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        return termos;
    }

    public void salvarGlossario(List<Termo> termos) {
        termos.sort(Comparator.comparing(Termo::getPalavra, String.CASE_INSENSITIVE_ORDER));
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8))) {
            pw.println("Palavra,Definicao");
            for (Termo termo : termos) {
                pw.printf("\"%s\",\"%s\"\n", escape(termo.getPalavra()), escape(termo.getDefinicao()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escape(String s) {
        return s.replace("\"", "\"\"");
    }
    private String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }
}
