package model;

public class Termo {
    private String palavra;
    private String definicao;

    public Termo(String palavra, String definicao) {
        this.palavra = palavra;
        this.definicao = definicao;
    }

    public String getPalavra() {
        return palavra;
    }

    public String getDefinicao() {
        return definicao;
    }

    public void setDefinicao(String definicao) {
        this.definicao = definicao;
    }
}
